/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.input;

/**
 *
 * @author sdedic
 */
public interface InputContextAware<T> {
    /**
     * Sets up the input context. Should be called before the first functional method,
     * ideally once. If called multiple times, must be called with the same ctx instance.
     * Rebinding InputContext is not supported.
     * 
     * @param ctx the input context
     */
    public T withInputContext(InputContext ctx);
}
