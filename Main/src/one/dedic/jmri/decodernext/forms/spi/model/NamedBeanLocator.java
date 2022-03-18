/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.spi.model;

import one.dedic.jmri.decodernext.forms.api.input.InputContext;

/**
 * Allows 
 * @author sdedic
 */
public interface NamedBeanLocator {
    public Object findNamedBean(String beanId, InputContext ctx);
}
