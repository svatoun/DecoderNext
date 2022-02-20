/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import java.util.EventListener;

/**
 *
 * @author sdedic
 */
public interface ValidationListener extends EventListener {
    /**
     * Informs that validation has started.
     * @param e 
     */
    public default void validationPending(ValidationEvent e) {}
    
    /**
     * Informs that a validation has finished. The event holds
     * the computed result.
     * @param e 
     */
    public default void validationFinished(ValidationEvent e) {}
    
    /**
     * Informs that the validation result has changed.
     * @param e 
     */
    public default void validationResultChanged(ValidationEvent e) {}
}
