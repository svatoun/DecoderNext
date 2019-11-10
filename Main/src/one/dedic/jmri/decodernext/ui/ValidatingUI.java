/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.ui;

import com.jgoodies.validation.ValidationResult;

/**
 *
 * @author sdedic
 */
public interface ValidatingUI {
    /**
     * Returns the most recent validation result. May return null, if
     * no validation was done.
     * @return 
     */
    public ValidationResult getValidationResult();
    
    /**
     * Requests validation. A new ValidationResult will be created.
     */
    public ValidationResult doValidation();
}
