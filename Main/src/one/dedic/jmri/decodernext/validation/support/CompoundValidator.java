/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.support;

import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import java.util.Arrays;
import java.util.List;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class CompoundValidator implements Validator, ContextValidator {
    private final List<Validator> delegates;

    public CompoundValidator(List<Validator> delegates) {
        this.delegates = delegates;
    }

    public CompoundValidator(Validator... delegates) {
        this(Arrays.asList(delegates));
    }

    @Override
    public ValidationResult validate(Object validationTarget) {
        ValidationResult vr = new ValidationResult();
        delegates.forEach((d) -> {
            ValidationResult res = d.validate(validationTarget);
            if (res != null) {
                vr.addAllFrom(res);
            }
        });
        return vr;
    }

    @Override
    public void attach(Lookup context) {
        delegates.forEach((d) -> {
            if (d instanceof ContextValidator) {
                ((ContextValidator)d).attach(context);
            }
        });
    }
}
