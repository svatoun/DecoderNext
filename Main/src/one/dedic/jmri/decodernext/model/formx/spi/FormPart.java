/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.spi;

import java.util.List;
import one.dedic.jmri.decodernext.model.formx.model.EntryDescriptor;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import one.dedic.jmri.decodernext.model.formx.model.ExComponentModel;
import one.dedic.jmri.decodernext.model.formx.validation.Validated;

/**
 * Describes a part fo the form or panel. A part has some metadata ({@link #getDescriptor()},
 * its UI can be controlled ({@link #getComponentModel()}) and may offer validation results 
 * ({@link #getValidated}).
 * 
 * @author sdedic
 */
public interface FormPart {
    
    /**
     * Descriptor of the form part.
     * @return 
     */
    public EntryDescriptor getDescriptor();
    
    /**
     * UI model for the form part. 
     * @return 
     */
    public ExComponentModel getComponentModel();
    
    /**
     * @return validation status and event reporter for this form part.
     */
    public Validated getValidated();
    
    /**
     * Extension of a FormPart that forms a hierarchy. Note that a {@link FormPart} can be a member
     * of multiple Containers (theoretically): gives a possibility to regroup or customize.
     */
    interface Container extends FormPart {
        List<FormPart>  children();
    }
    
    interface Entry extends FormPart {
        public DataItem  getData();
    }
}
