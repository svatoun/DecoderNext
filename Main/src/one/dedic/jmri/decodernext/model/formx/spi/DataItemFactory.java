/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.spi;

import one.dedic.jmri.decodernext.model.formx.model.InputContext;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;

/**
 * Contributes {@link DataItem}s to the data model.
 * @author sdedic
 */
public interface DataItemFactory {
    /**
     * Creates data items for the given context. 
     * 
     * @param context
     * @return 
     */
    public DataItem createItem(String id, InputContext context);
}
