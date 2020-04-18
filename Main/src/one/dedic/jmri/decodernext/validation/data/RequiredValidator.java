/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import com.jgoodies.validation.ValidationResult;
import one.dedic.jmri.decodernext.validation.support.AbstractValidator;
import org.openide.util.NbBundle;

/**
 * Validator that reports error, if the value is null or empty string.
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - name of the proeprty",
    "ValidationError_CannotBeEmpty={0} is required, must not be empty"
})
public final class RequiredValidator extends AbstractValidator {
    @Override
    public ValidationResult validate(Object validationTarget) {
        if (validationTarget != null) {
            String s = validationTarget.toString().trim();
            if (!s.isEmpty()) {
                return null;
            }
        }
        return new ValidationResult().addError(Bundle.ValidationError_CannotBeEmpty(getLabel()), 
                getMessageKey());
    }
}
