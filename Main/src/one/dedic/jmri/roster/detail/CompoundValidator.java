/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.roster.detail;

import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public final class CompoundValidator implements Validator, ContextValidator {
    private final List<Validator> validators = new ArrayList<>();
    private Lookup context;
    
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
