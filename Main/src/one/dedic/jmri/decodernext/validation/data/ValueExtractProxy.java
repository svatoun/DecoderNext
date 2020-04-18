/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import one.dedic.jmri.decodernext.validation.data.ValidatedValue;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import javax.swing.JComponent;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class ValueExtractProxy implements ValidatorSetup, ContextValidator {
    private ValidatedValue  provider;
    private Validator       validator;

    public ValueExtractProxy() {
    }

    public ValueExtractProxy(ValidatedValue provider, Validator validator) {
        this.provider = provider;
        this.validator = validator;
    }

    public ValidatedValue getProvider() {
        return provider;
    }

    public void setProvider(ValidatedValue provider) {
        this.provider = provider;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public void setLabel(String l) {
        if (validator instanceof ValidatorSetup) {
            ((ValidatorSetup)validator).setLabel(l);
        }
    }

    @Override
    public void setMessageKey(Object k) {
        if (validator instanceof ValidatorSetup) {
            ((ValidatorSetup)validator).setMessageKey(k);
        }
    }

    @Override
    public ValidationResult validate(Object validationTarget) {
        if (!provider.test(validationTarget)) {
            throw new IllegalArgumentException();
        }
        return validator.validate(
                provider.getValue((JComponent)validationTarget)
        );
    }

    @Override
    public void attach(Lookup context) {
        if (validator instanceof ContextValidator) {
            ((ContextValidator)validator).attach(context);
        }
    }
    
}
