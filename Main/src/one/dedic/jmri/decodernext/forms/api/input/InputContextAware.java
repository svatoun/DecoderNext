/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.input;

import one.dedic.jmri.decodernext.model.formx.model.*;

/**
 *
 * @author sdedic
 */
public interface InputContextAware {
    /**
     * Sets up the input context. Should be called before the first functional method,
     * ideally once. Must be called with the same ctx instance.
     * 
     * @param ctx the input context
     */
    public void useInputContext(DefaultInputContext ctx);
}
