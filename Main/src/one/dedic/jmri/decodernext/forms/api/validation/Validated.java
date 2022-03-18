/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.validation;

import com.jgoodies.validation.ValidationResult;
import java.util.concurrent.CompletableFuture;

/**
 * 
 * @author sdedic
 */
public interface Validated extends HasValidationResult {
    /**
     * Attaches the validated object to a global ValidationService.
     * @param service 
     */
    public void attachValidationService(ValidationService service);
    
    /**
     * Performs validation. The validation is not guaranteed to complete synchronously, but
     * eventually will produce a result.
     * <p>
     * If a validation is pending, returns handle to that pending validation and does not
     * start another, unless {@code force} is set to true. Force will cancel the pending
     * validation and start a new one.
     * <p>
     * If a validation was not finished and is restarted, the original Future will be completed with the same result and
     * success as the newly started one.
     * 
     * @return handle to a result of a validation.
     * @param force forces validation
     */
    public CompletableFuture<ValidationResult>  validate(boolean force);
}
