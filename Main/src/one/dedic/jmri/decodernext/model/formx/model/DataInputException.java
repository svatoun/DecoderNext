/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.model;

import one.dedic.jmri.decodernext.model.formx.model.AbstractItem;
import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.message.SimpleValidationMessage;

/**
 * Exception that represents invalid an input state. The input state cannot be
 * represented as data by the model, but the user input is changed and does
 * not match the last value that could be represented. The exception may
 * carry a {@link ValidationResult} instance that better represents the error.
 * 
 * @author sdedic
 */
public final class DataInputException extends IllegalStateException {
    private final AbstractItem inputSource;
    private final ValidationResult errResult;
    

    public DataInputException(AbstractItem inputSource, ValidationResult errResult) {
        super(extractMessage(errResult));
        this.inputSource = inputSource;
        this.errResult = errResult;
    }
    
    public DataInputException(String s, Throwable cause) {
        super(s, cause);
        this.inputSource = null;
        this.errResult = null;
    }

    public DataInputException(String s) {
        super(s);
        this.inputSource = null;
        this.errResult = null;
    }

    public DataInputException(AbstractItem inputSource, String message, Throwable cause) {
        super(message, cause);
        this.inputSource = inputSource;
        this.errResult = new ValidationResult();
        SimpleValidationMessage svm = new SimpleValidationMessage(message, Severity.ERROR);
    }

    public AbstractItem getInputSource() {
        return inputSource;
    }

    public ValidationResult getErrResult() {
        return errResult;
    }
    
    private static String extractMessage(ValidationResult r) {
        assert r.hasErrors();
        return r.getErrors().get(0).formattedText();
    }
}
