/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.model;

import com.jgoodies.validation.Validator;

/**
 *
 * @author sdedic
 */
public interface DataItem extends AbstractItem {
    /**
     * Description of the entry. Specializes {@link AbstractItem#getDescriptor()} in that
     * it can provide entry ID.
     * @return 
     */
    @Override
    public EntryDescriptor      getDescriptor();
    
    /**
     * The data model. The data model can be used to get / set a value to the 
     * data storage. The model can be disabled: this will impact both UI and
     * potential validation.
     * @return 
     */
    public FormValueModel     getModel();
    
    /**
     * Mandatory validator used to inspect the data represented by this item.
     * @return 
     */
    public default Validator  getValidator() {
        return null;
    }
}
