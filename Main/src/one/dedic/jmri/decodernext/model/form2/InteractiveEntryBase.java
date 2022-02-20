/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

import one.dedic.jmri.decodernext.validation.ValidationFeedback;

/**
 *
 * @author sdedic
 */
public interface InteractiveEntryBase {
    /**
     * Request that the UI for the entry activates.s
     */
    public void           requestActive();
    
    /**
     * Provides access to feedback area.
     * @return 
     */
    public ValidationFeedback  getFeedback();

}
