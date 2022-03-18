/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.validation;

import com.jgoodies.validation.ValidationResult;
import java.util.EventObject;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author sdedic
 */
public final class ValidationEvent extends EventObject {
    private final ValidationResult result;
    private final CompletableFuture<ValidationResult> pending;

    public ValidationEvent(Object source, CompletableFuture<ValidationResult> p) {
        super(source);
        result = null;
        pending = p;
    }

    public ValidationEvent(ValidationResult result, Object source) {
        super(source);
        this.result = result;
        this.pending = null;
    }

    public ValidationResult getResult() {
        return result;
    }

    public boolean isPending() {
        return pending != null;
    }

    public CompletableFuture<ValidationResult> getPending() {
        return pending;
    }
}
