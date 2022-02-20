/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import com.jgoodies.validation.ValidationResult;
import java.util.EventObject;

/**
 *
 * @author sdedic
 */
public final class ValidationEvent extends EventObject {
    private final ValidationResult result;
    private final boolean pending;

    public ValidationEvent(Object source) {
        super(source);
        result = null;
        pending = true;
    }

    public ValidationEvent(ValidationResult result, Object source) {
        super(source);
        this.result = result;
        this.pending = false;
    }

    public ValidationResult getResult() {
        return result;
    }

    public boolean isPending() {
        return pending;
    }
}
