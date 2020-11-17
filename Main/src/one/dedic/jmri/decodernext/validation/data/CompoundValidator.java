/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import one.dedic.jmri.decodernext.validation.data.ValidatorSetup;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public final class CompoundValidator implements Validator, ContextValidator, ValidatorSetup {
    private final List<Validator> validators = new ArrayList<>();
    private Lookup context;
    private Object key;
    private String label;
    
    private CompoundValidator(Validator... vals) {
        if (vals.length > 0) {
            validators.addAll(Arrays.asList(vals));
        }
    }
    
    public static CompoundValidator create() {
        return new CompoundValidator();
    }
    
    public static CompoundValidator create(Validator... vals) {
        return new CompoundValidator(vals);
    }
    
    public CompoundValidator addValidator(Validator v) {
        this.validators.add(v);
        return this;
    }
    
    private Validator context(Validator v) {
        if (v instanceof ContextValidator) {
            ((ContextValidator)v).attach(context);
        }
        return v;
    }

    @Override
    public void setLabel(String l) {
        this.label = l;
        for (Validator v : validators) {
            if (v instanceof ValidatorSetup) {
                ((ValidatorSetup)v).setLabel(l);
            }
        }
    }

    @Override
    public void setMessageKey(Object k) {
        this.key = k;
        for (Validator v : validators) {
            if (v instanceof ValidatorSetup) {
                ((ValidatorSetup)v).setMessageKey(k);
            }
        }
    }
    
    @Override
    public ValidationResult validate(Object validationTarget) {
        int sz = validators.size();
        if (sz == 0) {
            return ValidationResult.EMPTY;
        } else if (sz == 1) {
            return context(validators.get(0)).validate(validationTarget);
        }
        ValidationResult union = new ValidationResult();
        for (Validator v : validators) {
            union.addAllFrom(context(v).validate(validationTarget));
        }
        return union;
    }

    @Override
    public void attach(Lookup context) {
        this.context = context;
        for (Validator v : validators) {
            context(v);
        }
    }
}
