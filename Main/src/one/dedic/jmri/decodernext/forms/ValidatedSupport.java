/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import com.jgoodies.validation.message.SimpleValidationMessage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import one.dedic.jmri.decodernext.forms.api.input.InputContext;
import one.dedic.jmri.decodernext.forms.api.model.BufferedModel;
import one.dedic.jmri.decodernext.forms.api.model.EntryDescriptor;
import one.dedic.jmri.decodernext.forms.api.model.HasEnable;
import one.dedic.jmri.decodernext.forms.api.validation.HasValidationResult;
import one.dedic.jmri.decodernext.forms.api.validation.Validated;
import one.dedic.jmri.decodernext.forms.api.validation.ValidationEvent;
import one.dedic.jmri.decodernext.forms.api.validation.ValidationListener;
import one.dedic.jmri.decodernext.forms.api.validation.ValidationService;
import one.dedic.jmri.decodernext.forms.api.validation.ValidationUtils;
import org.openide.util.WeakListeners;

/**
 * Support for validated inputs. The support reacts on input change, performs validation, firing
 * appropriate events (pending, start, change). It monitors <b>enabled status</b> of `enabler` and
 * if not enabled, resets result to ValidationResult.EMPTY so it will clear all errors for disabled
 * controls or data.
 * After validationFinished event, 
 * 
 * @author sdedic
 */
public class ValidatedSupport implements Validated {
    private static final Executor sharedValidationExecutor = Executors.newSingleThreadExecutor();

    private final Object identity;
    private final HasEnable enabler;
    private final List<Validator<?>> validators = new ArrayList<>();
    private final ValueModel inputModel;
    private final EntryDescriptor descriptor;
    private final InputContext context;
    
    private boolean enabled;
    private Executor executor = sharedValidationExecutor;
    private Executor dataExecutor;
    private List<ValidationListener> listeners = null;
    private CompletableFuture<ValidationResult> valHandle = NOT_VALIDATED;
    private ValidationResult lastResult = ValidationResult.EMPTY;
    
    private static final CompletableFuture<ValidationResult> NOT_VALIDATED = new CompletableFuture<ValidationResult>() {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean completeExceptionally(Throwable ex) {
            return false;
        }

        @Override
        public boolean complete(ValidationResult value) {
            return false;
        }
    };
    
    public ValidatedSupport(Object identity, HasEnable enabler, Validator<?> validator, ValueModel inputModel, EntryDescriptor descriptor, InputContext context) {
        this.identity = identity == null ? this : identity;
        this.enabler = enabler;
        this.inputModel = inputModel;
        this.descriptor = descriptor;
        this.context = context;
        
        executor = context.getValidationExecutor();
        if (enabler != null) {
            enabler.addPropertyChangeListener(WeakListeners.propertyChange(this::enablerChanged, enabler));
        }
        inputModel.addValueChangeListener(WeakListeners.propertyChange(this::inputChanged, inputModel));
        validators.add(validator);
    }
    
    public ValidatedSupport addValidator(Validator<?>... vals) {
        synchronized (this) {
            validators.addAll(Arrays.asList(vals));
            if (valHandle == NOT_VALIDATED) {
                return this;
            }
            invalidate();
        }
        validate(true);
        return this;
    }

    public ValueModel getInputModel() {
        return inputModel;
    }

    public EntryDescriptor getDescriptor() {
        return descriptor;
    }

    public InputContext getContext() {
        return context;
    }
    
    public void invalidate() {
        synchronized (this) {
            valHandle = NOT_VALIDATED;
        }
    }

    @Override
    public void addValidationListener(ValidationListener l) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ArrayList<>(2);
            }
            listeners.add(l);
        }
    }

    @Override
    public void removeValidationListener(ValidationListener l) {
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            listeners.remove(l);
        }
    }

    @Override
    public ValidationResult getValidation() {
        ValidationResult r = valHandle.getNow(lastResult);
        return r == null ? HasValidationResult.PENDING : r;
    }

    private boolean setLastResult(CompletableFuture<ValidationResult> key, ValidationResult lastResult, ValidationResult value) {
        synchronized (this) {
            if (valHandle == key) {
                this.lastResult = value;
                pendingFired = false;
            }
        }
        executor.execute(() -> {
            // complete pending validation in case some client is waiting
            key.complete(value);
            fireValidationFinished(value);
            if (!Objects.equals(lastResult, value)) {
                fireValidationChanged(value);
            }
            
        });
        return true;
    }

    boolean pendingFired = false;

    /**
     * A special Future that signals that 'dirty' status has been received
     * and dispatched as an event.
     */
    static class DirtyValidationFuture extends CompletableFuture<ValidationResult> {
    }

    @Override
    public CompletableFuture<ValidationResult> validate(boolean force) {
        CompletableFuture<ValidationResult> res;
        synchronized (this) {
            if (valHandle != NOT_VALIDATED && valHandle.isDone()) {
                if (!force || (validators.isEmpty())) {
                    return valHandle;
                }
            }
            res = valHandle;
            valHandle = NOT_VALIDATED;
        }
        // chain the dirty future after the real one.
        CompletableFuture<ValidationResult> toRet = doValidation();
        toRet.handle((v, ex) -> {
            if (ex != null) {
                toRet.completeExceptionally(ex);
            } else {
                toRet.complete(v);
            }
            return null;
        });
        return toRet;
    }
    
    private ValidationResult createThrowableResult(Throwable t) {
        ValidationResult r = new ValidationResult();
        r.add(new SimpleValidationMessage(t.getLocalizedMessage(), Severity.ERROR));
        return r;
    }
    
    public boolean isEnabled() {
        return enabler == null || enabler.isEnabled();
    }
    
    private CompletableFuture<ValidationResult> doValidation() {
        CompletableFuture<ValidationResult> r = new CompletableFuture<>();
        executor.execute(() -> doValidation0(r));
        return r;
    }
    
    private CompletableFuture<ValidationResult> doValidation0(CompletableFuture<ValidationResult> res) {
        List<Validator<?>> vals;
        ValidationResult l;
        boolean e = isEnabled();
        CompletableFuture<ValidationResult> oldRes;
        synchronized (this) {
            oldRes = valHandle;
            vals = new ArrayList<>(validators);
            if (vals.isEmpty()) {
                e = false;
            }
            valHandle = res;
            l = lastResult;
        }
        if (oldRes != NOT_VALIDATED && !oldRes.isDone()) {
            // res is still not completed
            res.whenComplete((v, ex) -> {
                if (ex != null) {
                    oldRes.completeExceptionally(ex);
                } else {
                    oldRes.complete(v);
                }
            });
        }
        if (!e) {
            setLastResult(res, l, ValidationResult.EMPTY);
            return res;
        }
        CompletableFuture<ValidationResult> res2;
        res2 = ValidationUtils.performLongValidation(
            descriptor.getName(),
            inputModel, 
            vals,
            context, descriptor, dataExecutor);
        if (!res2.isDone()) {
            executor.execute(() -> fireValidationPending(res));
        }
        res2.exceptionally(
                (t) -> createThrowableResult(t)).
            thenAccept(
                (nr) -> setLastResult(res, l, nr));
        return res;
    }
    
    private void fireBufferedValidation() {
        CompletableFuture<ValidationResult> toCancel;
        CompletableFuture<ValidationResult> r;
        
        synchronized (this) {
            if (valHandle instanceof DirtyValidationFuture) {
                return;
            }
            toCancel = valHandle;
            r = valHandle = new DirtyValidationFuture();
        }
        toCancel.cancel(true);
        fireValidationPending(r);
    }
    
    private void fireValidationPending(CompletableFuture<ValidationResult> r) {
        ValidationListener[] ll;
        synchronized (this) {
            if (pendingFired || valHandle.isDone()) {
                return;
            }
            pendingFired = true;
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            ll = listeners.toArray(new ValidationListener[listeners.size()]);
        }
        ValidationEvent e = new ValidationEvent(identity, r);
        for (ValidationListener l : ll) {
            l.validationPending(e);
        }
    }
    
    private void fireValidationFinished(ValidationResult r) {
        ValidationListener[] ll;
        synchronized (this) {
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            ll = listeners.toArray(new ValidationListener[listeners.size()]);
        }
        ValidationEvent e = new ValidationEvent(r, identity);
        for (ValidationListener l : ll) {
            l.validationFinished(e);
        }
    }
    
    private void fireValidationChanged(ValidationResult r) {
        ValidationListener[] ll;
        synchronized (this) {
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            ll = listeners.toArray(new ValidationListener[listeners.size()]);
        }
        ValidationEvent e = new ValidationEvent(r, identity);
        for (ValidationListener l : ll) {
            l.validationResultChanged(e);
        }
    }

    private void updateAfterEnabledChange() {
        boolean e = isEnabled();
        ValidationResult l;
        synchronized (this) {
            if (this.enabled == e) {
                return;
            }
            this.enabled = e;
            l = lastResult;
        }
        if (e) {
            // revalidate after enable
            executor.execute(() -> validate(true));
        } else {
            CompletableFuture<ValidationResult> f = CompletableFuture.completedFuture(ValidationResult.EMPTY);
            setLastResult(f, lastResult, ValidationResult.EMPTY);
        }
    }

    @Override
    public void attachValidationService(ValidationService service) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    void inputChanged(PropertyChangeEvent evt) {
        if (BufferedModel.PROP_DIRTY.equals(evt.getPropertyName())) {
            executor.execute(() -> fireBufferedValidation());
        } else if (ValueModel.PROPERTY_VALUE.equals(evt.getPropertyName())) {
            executor.execute(() -> validate(true));
        }
    }

    void enablerChanged(PropertyChangeEvent evt) {
        if (HasEnable.PROP_VISIBLE.equals(evt.getPropertyName())) {
            executor.execute(() -> updateAfterEnabledChange());
        }
        if (HasEnable.PROP_ENABLED.equals(evt.getPropertyName())) {
            executor.execute(() -> updateAfterEnabledChange());
        }
    }
}
