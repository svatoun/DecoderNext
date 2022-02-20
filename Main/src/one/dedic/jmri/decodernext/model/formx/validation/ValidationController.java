/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import one.dedic.jmri.decodernext.jgoodies.ValueWeakListener;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import one.dedic.jmri.decodernext.model.formx.model.InputContext;
import one.dedic.jmri.decodernext.model.formx.model.BufferedModel;
import one.dedic.jmri.decodernext.model.formx.validation.Validated;
import one.dedic.jmri.decodernext.model.formx.validation.ValidationEvent;
import one.dedic.jmri.decodernext.model.formx.validation.ValidationListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Represents the controller, that coordinates validation. The controller reports
 * whether the user input is overall OK: for dialogs it may mean that if the input is
 * not OK, the closing buttons may be disabled.
 * <o>
 * 
 * @author sdedic
 */
public class ValidationController implements Validated {
    public static final ValidationResult PENDING = ValidationResult.unmodifiableResult(new ValidationResult());
    
    private static final RequestProcessor VALIDATION_RP = new RequestProcessor(ValidationController.class);
    
    /**
     * Delay after last VALUE and before the validation is done. This delay is that the data should settle
     * after the last VALUE change in models.
     */
    private static final int DELAY_VALUE = 50;
    
    private InputContext inputContext;
    
    // @GuardedBy(this)
    private Map<String, R>  dataModels = new HashMap<>();

    // @GuardedBy(this)
    private List<Validated> validatedItems = new ArrayList<>();
    
    /**
     * Name-bound validators. 
     */
    // @GuardedBy(this)
    private Map<String, List<Validator>> externalValidators = new HashMap<>();

    // @GuardedBy(this)
    private Set<String> dirtyItems;
    
    // @GuardedBy(this)
    private List<ValidationListener> listeners = new ArrayList<>();
    
    // @GuardedBy(this)
    private Map<String, ValidationResult> results = new HashMap<>();

    // @GuardedBy(this)
    private ValidationResult completeResult = ValidationResult.EMPTY;
    
    /**
     * Validation that is scheduled (after a small delay) after DIRTY property is fired and the
     * period is somewhat extended after VALUE is fired. The Controller will report validation pending immediately.
     */
    private RequestProcessor.Task   scheduledValidation;
    
    private CompletableFuture<ValidationResult> validationHandle = CompletableFuture.completedFuture(ValidationResult.EMPTY);
    
    public void addDataModel(DataItem m) {
        synchronized (this) {
            R c = dataModels.get(m.id());
            if (c == null) {
                c = new R(m);
                dataModels.put(m.id(), c);
            }
        }
    }
    
    public void addDataModels(DataItem... models) {
        for (DataItem m : models) {
            addDataModel(m);
        }
    }
    
    /**
     * Adds a validator triggered by certain data.
     * @param v
     * @param triggeredBy 
     */
    public void addValidator(Validator v, String... triggeredBy) {
        for (String id : triggeredBy) {
            externalValidators.computeIfAbsent(id, (i) -> new ArrayList<>()).add(v);
        }
    }
    
    private static ValidationResult addResult(String k, ValidationResult vr, ValidationResult nr, Map<String, ValidationResult> itemResults) {
        if (nr == null) {
            return vr;
        }
        vr.addAllFrom(nr);
        itemResults.merge(k, vr, (oldVr, newVr) -> {
            oldVr.addAllFrom(newVr);
            return oldVr;
        });
        return vr;
    }
    
    @NbBundle.Messages({
        "# {0} - item name",
        "# {0} - error message",
        "FMT_InvalidItemValue=The value is invalid."
    })
    CompletableFuture<ValidationResult> performValidation(boolean force) {
        CompletableFuture<ValidationResult> r;
        synchronized (this) {
            scheduledValidation = null;
            r = this.validationHandle;
            if (r.isDone()) {
                r = validationHandle = new CompletableFuture<>();
            }
        }
        
        CompletableFuture<ValidationResult> comp = CompletableFuture.completedFuture(new ValidationResult());
        List<CompletableFuture<Void>> parts = new ArrayList<>();
        
        ValidationResult vr = new ValidationResult();
        for (String id : dirtyItems) {
            R reg;
            // now perform data-level validation:
            reg = dataModels.get(id);
            if (reg.item == null) {
                continue;
            }
            Validator dataValidator = reg.item.getValidator();
            // and finally process the external validators.
            List<Validator> vals = externalValidators.getOrDefault(id, Collections.emptyList());
            if (!vals.isEmpty() && dataValidator != null) {
                vals = new ArrayList<>(vals);
                vals.add(0, dataValidator);
            }
            CompletableFuture<ValidationResult> vrlong = 
                    ValidationUtils.performLongValidation(
                        reg.item.id(),
                        reg.item.getModel(), 
                        vals, 
                        inputContext, reg.item);
            parts.add(
                vrlong.thenAccept(vr::addAllFrom)
            );
        }
        List<Validated> copy;
        synchronized (this) {
            copy = new ArrayList<>(validatedItems);
        }
        for (Validated v : copy) {
            parts.add(
                v.validate(false).thenAccept(vr::addAllFrom)
            );
        }
        CompletableFuture<ValidationResult> fr = r;
        
        CompletableFuture[] all = parts.toArray(new CompletableFuture[parts.size()]);
        CompletableFuture.allOf(all).thenAccept((v) -> {
            completeAndSetResult(fr, vr);
        });
        return fr;
    }
    
    void completeAndSetResult(CompletableFuture<ValidationResult> fr, ValidationResult vr) {
        Map<String, ValidationResult> messages = new HashMap<>();
        vr.forEach(m -> messages.computeIfAbsent(m.key().toString(), (s) -> new ValidationResult()).add(m));
        
        boolean changed;
        
        synchronized (this) {
            changed = !messages.equals(results);
            results = messages;
        }
        fr.complete(vr);
        ValidationListener[] ll = null;
        synchronized (this) {
            if (!listeners.isEmpty()) {
                ll = listeners.toArray(new ValidationListener[listeners.size()]);
            }
        }
        if (ll != null) {
            boolean fChanged = changed;
            ValidationEvent e = new ValidationEvent(vr, this);
            for (ValidationListener l : ll) {
                l.validationFinished(e);
                if (fChanged) {
                    l.validationResultChanged(e);
                }
            }
        }
    }
    
    void notifyDirty(String id, DataItem item) {
        if (dirtyItems != null) {
            // just add
            dirtyItems.add(id);
            return;
        }
    }

    @Override
    public CompletableFuture<ValidationResult> validate(boolean force) {
        CompletableFuture<ValidationResult> v;
        synchronized (this) {
            v = validationHandle;
            if (v != null) {
                if (!v.isDone() || !force) {
                    return v;
                }
            }
        }
        return performValidation(force);
    }

    @Override
    public void addValidationListener(ValidationListener l) {
        synchronized (this) {
            listeners.add(l);
        }
    }

    @Override
    public void removeValidationListener(ValidationListener l) {
        synchronized (this) {
            listeners.remove(l);
        }
    }

    @Override
    public ValidationResult getValidation() {
        CompletableFuture<ValidationResult> cfvr;
        synchronized (this) {
            cfvr = validationHandle;
        }
        if  (cfvr == null) {
            cfvr = performValidation(false);
        }
        try {
            return cfvr.getNow(PENDING);
        } catch (CompletionException ex) {
            // exception, do not report again
            return ValidationResult.EMPTY;
        }
    }
    
    void rescheduleValidation(int delay) {
        ValidationListener[] ll = null;
        synchronized (this) {
            if (scheduledValidation != null) {
                scheduledValidation.schedule(delay);
            } else {
                if (validationHandle != null && !validationHandle.isDone()) {
                    validationHandle = new CompletableFuture<>();
                    ll = listeners.toArray(new ValidationListener[listeners.size()]);
                }
                scheduledValidation = VALIDATION_RP.post(() -> performValidation(false), delay);
            }
        }
        if (ll == null) {
            return;
        }
        ValidationEvent ev = new ValidationEvent(this);
        for (ValidationListener l : ll) {
            l.validationPending(ev);
        }
    }
    
    void fireValidationPending() {
        ValidationListener[] ll = null;
        synchronized (this) {
            if (scheduledValidation != null) {
                return;
            }
            if (validationHandle != null && !validationHandle.isDone()) {
                return;
            }
            validationHandle = new CompletableFuture<>();
            ll = listeners.toArray(new ValidationListener[listeners.size()]);
        }
        ValidationEvent ev = new ValidationEvent(this);
        for (ValidationListener l : ll) {
            l.validationPending(ev);
        }
    }
    
    class V implements ValidationListener {
        @Override
        public void validationPending(ValidationEvent e) {
            fireValidationPending();
        }

        @Override
        public void validationFinished(ValidationEvent e) {
            rescheduleValidation(DELAY_VALUE);
        }
    }
    
    class R implements PropertyChangeListener {
        private final DataItem item;

        public R(DataItem item) {
            this.item = item;
            
            if (item != null) {
                item.getModel().addValueChangeListener(new ValueWeakListener(item.getModel(), this));
            }
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String pn = evt.getPropertyName();
            if (BufferedModel.PROP_DIRTY.equals(pn) ||
                ValueModel.PROPERTY_VALUE.equals(pn)) {
                notifyDirty(item.id(), item);
                rescheduleValidation(DELAY_VALUE);
            }
        }
    }
}
