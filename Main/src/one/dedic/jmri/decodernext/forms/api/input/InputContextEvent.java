/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.input;

import one.dedic.jmri.decodernext.model.formx.model.*;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import java.util.Collection;
import java.util.EventObject;

/**
 *
 * @author sdedic
 */
public final class InputContextEvent extends EventObject {
    private Collection<DataItem>    affectedItems;
    
    public InputContextEvent(DefaultInputContext source) {
        super(source);
    }

    public InputContextEvent(DefaultInputContext source, Collection<DataItem> items) {
        super(source);
        this.affectedItems = items;
    }

    public DefaultInputContext getSource() {
        return (DefaultInputContext)source;
    }

    public Collection<DataItem> getAffectedItems() {
        return affectedItems;
    }
}
