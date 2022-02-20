/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.model;

import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import java.util.Collection;
import java.util.EventObject;

/**
 *
 * @author sdedic
 */
public final class InputContextEvent extends EventObject {
    private Collection<DataItem>    affectedItems;
    
    public InputContextEvent(InputContext source) {
        super(source);
    }

    public InputContextEvent(InputContext source, Collection<DataItem> items) {
        super(source);
        this.affectedItems = items;
    }

    public InputContext getSource() {
        return (InputContext)source;
    }

    public Collection<DataItem> getAffectedItems() {
        return affectedItems;
    }
}
