/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import com.jgoodies.validation.ValidationResult;
import java.util.concurrent.CompletableFuture;

/**
 * A mixin interface for {@link Validator}s that support asynchronous long-running
 * validations. The Validator should return 
 * @author sdedic
 */
public interface LongValidator<T> {
    public CompletableFuture<ValidationResult>  validateAsync(T data);
}
