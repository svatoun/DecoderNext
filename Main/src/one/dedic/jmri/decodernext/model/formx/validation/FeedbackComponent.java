/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import com.jgoodies.validation.ValidationResult;

/**
 *
 * @author sdedic
 */
public interface FeedbackComponent extends HasValidationResult {
    /**
     * Updates validation model with the overall validation info.
     * @param model updates from upstream validation.
     */
    public void updateValidation(ValidationResult model);
}
