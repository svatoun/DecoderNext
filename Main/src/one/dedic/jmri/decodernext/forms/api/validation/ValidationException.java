/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.validation;

import com.jgoodies.validation.ValidationResult;

/**
 *
 * @author sdedic
 */
public class ValidationException extends IllegalStateException {
    private final ValidationResult  result;
    
    public ValidationException(ValidationResult  result, Throwable cause) {
        super(result.getMessagesText(), cause);
        this.result = result;
    }

    public ValidationResult getResult() {
        return result;
    }
}
