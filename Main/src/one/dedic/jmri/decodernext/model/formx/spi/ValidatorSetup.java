/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.spi;

import com.jgoodies.validation.Validator;

/**
 * Interface for common setup for a validator. Most of validators may print generic messages, but they
 * finally need a display name (label) of the validated entry. They also need a key
 * for the produced messages to pair with the visual feedback component(s). 
 * 
 * @author sdedic
 */
public interface ValidatorSetup extends Validator {
    public void setLabel(String l);
    public void setMessageKey(Object k);
}
