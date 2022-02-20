/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import com.jgoodies.validation.ValidationResult;
import java.util.concurrent.CompletableFuture;

/**
 * 
 * @author sdedic
 */
public interface Validated extends HasValidationResult {
    /**
     * Performs validation. The validation is not guaranteed to complete synchronously, but
     * eventually will produce a result.
     * <p>
     * If a validation is pending, returns handle to that pending validation and does not
     * start another, unless {@code force} is set to true. Force will cancel the pending
     * validation and start a new one.
     * 
     * @return handle to a result of a validation.
     * @param force forces validation
     */
    public CompletableFuture<ValidationResult>  validate(boolean force);
}
