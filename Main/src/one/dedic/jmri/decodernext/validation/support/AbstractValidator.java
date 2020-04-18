/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.support;

import one.dedic.jmri.decodernext.validation.data.ValidatorSetup;

/**
 * Base class for Validator implementations. The class supports 'label' and
 * 'messageKey' properties setup.
 * 
 * @author sdedic
 */
public abstract class AbstractValidator implements ValidatorSetup {
    private String label;
    private Object messageKey;
    
    @Override
    public void setLabel(String l) {
        this.label = l;
    }

    @Override
    public void setMessageKey(Object k) {
        this.messageKey = k;
    }

    public String getLabel() {
        return label;
    }

    public Object getMessageKey() {
        return messageKey;
    }
}
