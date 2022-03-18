/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.validation;

import com.jgoodies.validation.ValidationResult;

/**
 *
 * @author sdedic
 */
public interface HasValidationResult {
    public static final ValidationResult PENDING = ValidationResult.unmodifiableResult(new ValidationResult());
    
    /**
     * Attaches a validation state listener.
     * @param l 
     */
    public void addValidationListener(ValidationListener l);
    
    /**
     * Removes a validation state listener
     * @param l 
     */
    public void removeValidationListener(ValidationListener l);
    
    /**
     * Returns the current validation result. Does not run a validation, but returns
     * the result of last one. Must not return {@code null}. Returns immediately.
     * @return last validation result.
     */
    public ValidationResult getValidation();
}
