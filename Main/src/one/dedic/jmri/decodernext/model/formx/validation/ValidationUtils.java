/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import one.dedic.jmri.decodernext.model.formx.model.DataInputException;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import one.dedic.jmri.decodernext.model.formx.model.InputContext;
import one.dedic.jmri.decodernext.model.formx.model.InputContextAware;
import one.dedic.jmri.decodernext.model.formx.spi.ValidatorSetup;
import one.dedic.jmri.decodernext.model.formx.validation.LongValidator;

/**
 *
 * @author sdedic
 */
public class ValidationUtils {
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
                ica.useInputContext(context);
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
                    ica.useInputContext(null);
                }
            }
        }
        return r;
    }
    
    public static CompletableFuture<ValidationResult> performLongValidation(String id, ValueModel vm, Collection<Validator> validators, 
            InputContext context, DataItem item) {
        return performLongValidation(id, vm, validators, context, item.getDescriptor());
    }
    
    public static CompletableFuture<ValidationResult> performLongValidation(String id, ValueModel vm, Collection<Validator> validators, 
            InputContext context, FeatureDescriptor desc) {
        ValidationResult r = null;
        InputContext.callUseInputContext(context, validators);
        List<CompletableFuture<ValidationResult>> longValidators = new ArrayList<>();
        for (Validator v : validators) {
            if (v instanceof ValidatorSetup) {
                setupValidator((ValidatorSetup)v, context, desc);
            }
            InputContextAware ica = null;
            if (context != null && v instanceof InputContextAware) {
                ica = (InputContextAware)v;
                ica.useInputContext(context);
            }
            ValidationResult res = null;
            try {
                Object value = vm.getValue();
                if (v instanceof LongValidator) {
                    CompletableFuture<ValidationResult> fr = ((LongValidator)v).validateAsync(value);
                    res = fr.getNow(null);
                    if (res == null) {
                        longValidators.add(fr);
                    }
                } else {
                    res = v.validate(value);
                }
                if (r == null) {
                    r = res;
                } else if (res != null) {
                    r.addAllFrom(res);
                }
            } catch (DataInputException ex) {
                res = ex.getErrResult();
                if (res == null) {
                    res = new ValidationResult().
                        addError(ex.getLocalizedMessage(), id);
                }
            } finally {
                if (ica != null) {
                    ica.useInputContext(null);
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
        return cc.thenApply((v) -> {
            return fr;
        });
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
