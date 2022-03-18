/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.validation;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import one.dedic.jmri.decodernext.forms.api.input.InputContext;
import one.dedic.jmri.decodernext.forms.api.input.InputContextAware;
import one.dedic.jmri.decodernext.forms.api.input.InputContextUtils;
import one.dedic.jmri.decodernext.forms.api.model.DataInputException;
import one.dedic.jmri.decodernext.forms.api.model.DataItem;
import one.dedic.jmri.decodernext.forms.api.model.ModelUtilities;

/**
 *
 * @author sdedic
 */
public class ValidationUtils {
    public static final ValidationResult PENDING = ValidationResult.unmodifiableResult(new ValidationResult());

    public static ValidationResult performValidation(Object value, Collection<Validator> validators, 
            InputContext context, DataItem item) {
        return performValidation(value, validators, context, item.getDescriptor());
    }
    
    public static ValidationResult performValidation(Object value, Collection<Validator> validators, 
            InputContext context, FeatureDescriptor desc) {
        ValidationResult r = null;
        
        for (Validator v : validators) {
            if (v instanceof ValidatorSetup) {
                setupValidator((ValidatorSetup)v, context, desc);
            }
            InputContextAware ica = null;
            if (context != null && v instanceof InputContextAware) {
                ica = (InputContextAware)v;
                ica.withInputContext(context);
            }
            try {
                ValidationResult res = v.validate(value);
                if (r == null) {
                    r = res;
                } else if (res != null) {
                    r.addAllFrom(res);
                }
            } finally {
                if (ica != null) {
                    ica.withInputContext(null);
                }
            }
        }
        return r;
    }
    
    public static CompletableFuture<ValidationResult> performLongValidation(String id, ValueModel vm, Collection<Validator<?>> validators, 
            InputContext context, DataItem item) {
        return performLongValidation(id, vm, validators, context, item.getDescriptor(), null);
    }
    
    private static final Object INCOMPLETE = new Object();
    
    public static CompletableFuture<ValidationResult> performLongValidation(String id, ValueModel vm, Collection<Validator<?>> validators, 
            InputContext context, FeatureDescriptor desc, Executor exec) {
        ValidationResult r = null;
        InputContextUtils.callUseInputContext(context, validators);
        List<CompletableFuture<ValidationResult>> longValidators = new ArrayList<>();
        for (Validator candidate : validators) {
            if (candidate instanceof ValidatorSetup) {
                setupValidator((ValidatorSetup)candidate, context, desc);
            }
            InputContextAware<Validator> ica = null;
            if (context != null && candidate instanceof InputContextAware) {
                ica = (InputContextAware)candidate;
                candidate = ica.withInputContext(context);
            }
            final Validator v = candidate;
            ValidationResult res = null;
            try {
                Object value;
                
                
                CompletableFuture<Object> fv = ModelUtilities.getTargetValue(vm, false);
                value = fv.getNow(INCOMPLETE);
                if (value == INCOMPLETE) {
                    CompletableFuture<ValidationResult> fr = fv.thenComposeAsync(val -> {
                        if (v instanceof LongValidator) {
                            return ((LongValidator)v).validateAsync(val);
                        } else {
                            return CompletableFuture.completedFuture(v.validate(val));
                        }
                    }, exec);
                    longValidators.add(fr);
                }
                if (value != INCOMPLETE) {
                    if (v instanceof LongValidator) {
                        CompletableFuture<ValidationResult> fr = ((LongValidator)v).validateAsync(value);
                        res = fr.getNow(PENDING);
                        if (res == PENDING) {
                            longValidators.add(fr);
                        }
                    } else {
                        res = v.validate(value);
                    }
                }
            } catch (DataInputException ex) {
                res = ex.getErrResult();
                if (res == null) {
                    res = new ValidationResult().
                        addError(ex.getLocalizedMessage(), id);
                }
            } catch (IllegalStateException ex) {
                res = new ValidationResult().
                    addError(ex.getLocalizedMessage(), id);
            } finally {
                if (r == null) {
                    r = res;
                } else if (res != null) {
                    r.addAllFrom(res);
                }
                if (ica != null) {
                    ica.withInputContext(null);
                }
            }
        }
        ValidationResult fr = r != null ? r : new ValidationResult();
        if (longValidators.isEmpty()) {
            return CompletableFuture.completedFuture(r);
        }
        
        List<CompletableFuture<Void>> ll = longValidators.stream().map(f -> f.thenAccept(v -> {
                fr.addAllFrom(v);
            })).collect(Collectors.toList());
        /*
        CompletableFuture[] val = longValidators.stream().map(f -> f.thenAccept(v -> {
            fr.addAllFrom(v);
            })).toArray(size -> new CompletableFuture[size]);
        */
        CompletableFuture<Void> cc = CompletableFuture.allOf(ll.toArray(new CompletableFuture[ll.size()]));
        // chain and return the finally combined result.
        return cc.thenApply((v) -> fr);
    }

    public Object messageTargetId(ValidationMessage msg) {
        Object o = msg.key();
        if (!(o instanceof String)) {
            return null;
        }
        String s = o.toString();
        if (s.length() <= 1) {
            return s;
        }
        switch (s.charAt(0)) {
            case '+':
                return s.substring(1);
            default:
                return s;
        }
    }
    
    private static void setupValidator(ValidatorSetup s, InputContext context, FeatureDescriptor desc) {
        String id = desc.getName();
        String dn = desc.getDisplayName();
        
        s.setMessageKey(id);
        s.setLabel(dn);
    }
}
