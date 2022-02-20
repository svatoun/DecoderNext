/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import com.jgoodies.validation.message.SimpleValidationMessage;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import one.dedic.jmri.decodernext.model.formx.model.InputContext;
import one.dedic.jmri.decodernext.model.formx.model.BufferedModel;
import one.dedic.jmri.decodernext.model.formx.validation.Validated;
import one.dedic.jmri.decodernext.model.formx.validation.ValidationEvent;
import one.dedic.jmri.decodernext.model.formx.validation.ValidationListener;

/**
 *
 * @author sdedic
 */
public class ValidationSupport implements Validated {
    private InputContext context;
    private Executor executor;
    private String id;
    private FeatureDescriptor descriptor;
    private ValueModel validatedModel;
    
    // @GuardedBy(this)
    private L attachedL;
    
    // @GuardedBy(this)
    private Validator validator;
    
    // @GuardedBy(this)
    private CompletableFuture<ValidationResult> valHandle = CompletableFuture.completedFuture(ValidationResult.EMPTY);

    // @GuardedBy(this)
    private ValidationResult lastResult = ValidationResult.EMPTY;

    // @GuardedBy(this)
    boolean pendingFired = false;

    // @GuardedBy(this)
    private List<ValidationListener> listeners = null;
    
    public void attachListener() {
        synchronized (this) {
            if (attachedL != null) {
                return;
            }
            attachedL = new L();
        }
        ValueModel vm = getValidatedModel();
        if (vm == null) {
            return;
        }
        if (vm instanceof BufferedModel) {
            ((BufferedModel)vm).addPropertyChangeListener(attachedL);
        } else {
            vm.addValueChangeListener(attachedL);
        }
    }
    
    public void stopListener() {
        PropertyChangeListener l;
        synchronized (this) {
            if (attachedL == null) {
                return;
            }
            l = attachedL;
            attachedL = null;
        }
        ValueModel vm = getValidatedModel();
        if (vm == null) {
            return;
        }
        if (vm instanceof BufferedModel) {
            ((BufferedModel)vm).removePropertyChangeListener(l);
        } else {
            vm.removeValueChangeListener(l);
        }
    }
    
    public void setValidator(Validator v) {
        synchronized (this) {
            if (this.validator == v) {
                return;
            }
            this.validator = v;
        }
        validate(true);
    }

    public Validator getValidator() {
        synchronized (this) {
            return validator;
        }
    }
    
    public String getId() {
        return id;
    }

    public ValueModel getValidatedModel() {
        return validatedModel;
    }

    public FeatureDescriptor getDescriptor() {
        return descriptor;
    }
    
    private void fireValidationPending() {
        ValidationListener[] ll;
        synchronized (this) {
            if (pendingFired) {
                return;
            }
            pendingFired = true;
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            ll = listeners.toArray(new ValidationListener[listeners.size()]);
        }
        ValidationEvent e = new ValidationEvent(this);
        for (ValidationListener l : ll) {
            l.validationPending(e);
        }
    }
    
    private ValidationResult createThrowableResult(Throwable t) {
        ValidationResult r = new ValidationResult();
        r.add(new SimpleValidationMessage(t.getLocalizedMessage(), Severity.ERROR));
        return r;
    }
    
    protected CompletableFuture<ValidationResult> doValidation() {
        Validator val = getValidator();
        CompletableFuture<ValidationResult> res;
        ValidationResult l;
        
        synchronized (this) {
            if (val == null) {
                res = CompletableFuture.completedFuture(ValidationResult.EMPTY);
                valHandle = res;
                return res;
            }
            valHandle = res = new CompletableFuture<>();
            l = lastResult;
        }
        return performValidation(res, lastResult, val);
    }
    
    protected CompletableFuture<ValidationResult> performValidation(CompletableFuture<ValidationResult> publicResult, 
            ValidationResult lastResult, Validator val) {
        CompletableFuture<ValidationResult> res2;
        res2 = ValidationUtils.performLongValidation(
            getId(), getValidatedModel(), Collections.singletonList(val),
                context, getDescriptor());
        if (!res2.isDone()) {
            executor.execute(() -> fireValidationPending());
        }
        res2.exceptionally(
                (t) -> createThrowableResult(t)).
            thenAccept(
                (r) -> setLastResult(publicResult, lastResult, r));
        return publicResult;
    }
    
    protected boolean setLastResult(CompletableFuture<ValidationResult> key, ValidationResult lastResult, ValidationResult value) {
        synchronized (this) {
            if (valHandle == key) {
                this.lastResult = value;
                pendingFired = false;
            }
        }
        // complete pending validation in case some client is waiting
        key.complete(value);
        executor.execute(() -> {
            fireValidationFinished(value);
            if (!Objects.equals(lastResult, value)) {
                fireValidationChanged(value);
            }
            
        });
        return true;
    }

    private void fireValidationFinished(ValidationResult r) {
        ValidationListener[] ll;
        synchronized (this) {
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            ll = listeners.toArray(new ValidationListener[listeners.size()]);
        }
        ValidationEvent e = new ValidationEvent(r, this);
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
        ValidationEvent e = new ValidationEvent(r, this);
        for (ValidationListener l : ll) {
            l.validationResultChanged(e);
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
        return r == null ? ValidationController.PENDING : r;
    }

    @Override
    public CompletableFuture<ValidationResult> validate(boolean force) {
        synchronized (this) {
            if (valHandle.isDone()) {
                if (!force || (getValidator() == null)) {
                    return valHandle;
                }
            }
            valHandle.cancel(true);
        }
        return doValidation();
    }
    
    private class L implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String pn = evt.getPropertyName();
            if (pn == null) {
                validate(false);
                return;
            }
            switch (pn) {
                case BufferedModel.PROP_DIRTY:
                    fireValidationPending();
                    break;
                case ValueModel.PROPERTY_VALUE:
                    validate(true);
                    break;
            }
        }
    }
}
