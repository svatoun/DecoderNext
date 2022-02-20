/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

import com.jgoodies.binding.value.ValueModel;

/**
 * Data abstraction. Describes the data and allows get/set access to it.
 * The implementation may e.g. distribute integer bits into various places
 * etc.
 * 
 * @author sdedic
 */
public interface DataEntry extends AbstractEntry {
    /**
     * Description of the entry.
     * @return 
     */
    public EntryDescriptor      getDescriptor();
    
    /**
     * The data model. 
     * @return 
     */
    public ValueModel   getModel();
}
