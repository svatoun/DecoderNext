/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

/**
 *
 * @author sdedic
 */
public class DataInputException extends IllegalStateException {
    public DataInputException(String s) {
        super(s);
    }

    public DataInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
