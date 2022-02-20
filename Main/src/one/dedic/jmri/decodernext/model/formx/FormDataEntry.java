/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx;

import one.dedic.jmri.decodernext.model.formx.model.EntryDescriptor;
import one.dedic.jmri.decodernext.model.formx.model.DataInputException;
import one.dedic.jmri.decodernext.model.formx.model.InputContext;
import com.jgoodies.binding.value.ComponentValueModel;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import one.dedic.jmri.decodernext.model.formx.model.BufferedModel;
import one.dedic.jmri.decodernext.model.formx.validation.ValidationController;
import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import com.jgoodies.validation.message.SimpleValidationMessage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import one.dedic.jmri.decodernext.model.formx.model.ExComponentModel;
import one.dedic.jmri.decodernext.model.formx.model.FormValueModel;
import one.dedic.jmri.decodernext.model.formx.spi.FormPart;
import one.dedic.jmri.decodernext.model.formx.support.BasicInputModel;
import one.dedic.jmri.decodernext.model.formx.validation.ValidationUtils;
import one.dedic.jmri.decodernext.model.formx.validation.Validated;
import one.dedic.jmri.decodernext.model.formx.validation.ValidationEvent;
import one.dedic.jmri.decodernext.model.formx.validation.ValidationListener;
import org.openide.util.Lookup;
import one.dedic.jmri.decodernext.model.formx.spi.EditableFormItem;

/**
 *
 * @author sdedic
 */
public final class FormDataEntry extends BasicInputModel<Object> implements FormPart.Entry, Validated, FormValueModel, BufferedModel {
    private static final Executor sharedValidationExecutor = Executors.newSingleThreadExecutor();
    
    private final EditableFormItem editable;
    private final InputContext context;
    private final Executor executor = sharedValidationExecutor;

    private CompletableFuture<ValidationResult> valHandle = CompletableFuture.completedFuture(ValidationResult.EMPTY);
    private List<ValidationListener> listeners = null;
    private ValidationResult lastResult = ValidationResult.EMPTY;
    private boolean enabled;
    private PropertyChangeListener dataWeakL;
    
    public FormDataEntry(EditableFormItem editable, InputContext ctx) {
        super(editable.getEditedData().getModel());
        this.editable = editable;
        this.context = ctx;
        setInputModel(editable.getInputModel());
    }
    
    @Override
    public DataItem getData() {
        return editable.getEditedData();
    }

    @Override
    public boolean isEnabled() {
        return enabled && getData().getModel().isEnabled() 
            && editable.getComponentModel().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        editable.getComponentModel().setEnabled(enabled);
    }
    
    @Override
    public ExComponentModel getComponentModel() {
        return editable.getComponentModel();
    }

    @Override
    public EntryDescriptor getDescriptor() {
        return editable.getEditedData().getDescriptor();
    }

    @Override
    public Validated getValidated() {
        return this;
    }
    
    public Lookup getLookup() {
        return editable.getLookup();
    }

    @Override
    public Object getValue() {
        try {
            return super.getValue();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            synchronized (this) {
                if (lastResult != null && lastResult.hasErrors()) {
                    throw new DataInputException(this.getData(), lastResult);
                } else {
                    ValidationResult r = new ValidationResult();
                    r.add(new SimpleValidationMessage(ex.getLocalizedMessage(), Severity.ERROR));
                    valHandle = CompletableFuture.completedFuture(r);
                    throw new DataInputException(this.getData(), r);
                }
            }
        }
    }

    @Override
    public CompletableFuture<ValidationResult> validate(boolean force) {
        synchronized (this) {
            if (valHandle.isDone()) {
                if (!force || (editable.getEditedData().getValidator() == null)) {
                    return valHandle;
                }
            }
            valHandle.cancel(true);
        }
        return doValidation();
    }
    
    private ValidationResult createThrowableResult(Throwable t) {
        ValidationResult r = new ValidationResult();
        r.add(new SimpleValidationMessage(t.getLocalizedMessage(), Severity.ERROR));
        return r;
    }
    
    private boolean setLastResult(CompletableFuture<ValidationResult> key, ValidationResult lastResult, ValidationResult value) {
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

    boolean pendingFired = false;

    private CompletableFuture<ValidationResult> doValidation() {
        Validator val = editable.getEditedData().getValidator();
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
        CompletableFuture<ValidationResult> res2;
        res2 = ValidationUtils.performLongValidation(
            editable.getEditedData().id(), 
            getInputModel(), 
            Collections.singletonList(val),
            context, getData());
        if (!res2.isDone()) {
            executor.execute(() -> fireValidationPending());
        }
        res2.exceptionally(
                (t) -> createThrowableResult(t)).
            thenAccept(
                (r) -> setLastResult(res, l, r));
        return res;
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
    protected void forwardDirtyStatus(PropertyChangeEvent evt) {
        synchronized (this) {
            if (!(isDirty() && !isDirtyStatus())) {
                return;
            }
        }
        super.forwardDirtyStatus(evt);
    }

    @Override
    protected Object forwardToData(Object prev, Object v) {
        CompletableFuture<ValidationResult> res = doValidation();
        CompletableFuture<Object> newVal = res.thenApply((r) -> forwardToData(prev, r, v));
        return newVal.getNow(SUPPRESS);
    }

    private Object forwardToData(Object prev, ValidationResult r, Object v) {
        if (r.hasErrors()) {
            setDirty(true);
            fireValueChange(prev, null);
            return SUPPRESS;
        }
        return super.forwardToData(prev, v);
    }

    @Override
    protected void forwardToInput(PropertyChangeEvent evt) {
        super.forwardToInput(evt);
        if (FormValueModel.PROP_ENABLED.equals(evt.getPropertyName())) {
            propagateDataEnabled();
        }
    }
    
    
    private void propagateDataEnabled() {
        editable.getComponentModel().setEnabled(
                enabled && getData().getModel().isEnabled()
        );
    }
    
    private void reflectVisibleAndEnabledUI() {
        boolean enabled = editable.getComponentModel().isEnabled() &&
                editable.getComponentModel().isVisible();
        
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        firePropertyChange(PROP_ENABLED, !enabled, enabled);
        if (isEnabled()) {
            validate(true);
        } else {
            valHandle = CompletableFuture.completedFuture(ValidationResult.EMPTY);
        }
    }
    
    private class CL implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String pn = evt.getPropertyName();
            if (pn == null) {
                reflectVisibleAndEnabledUI();
            } else {
                switch (pn) {
                    case ComponentValueModel.PROPERTY_ENABLED:
                    case ComponentValueModel.PROPERTY_VISIBLE:
                        reflectVisibleAndEnabledUI();
                }
            }
        }
    }
}
