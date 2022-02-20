/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

import com.jgoodies.validation.Validator;
import javax.swing.JComponent;
import one.dedic.jmri.decodernext.validation.ValidationFeedback;

/**
 * Abstract one form or input item. Provides an unique ID for the item and
 * its display name (usually a label). The item interacts with the user by
 * its {@link #getInput} ValueModel and eventually updates the {@link #getProperty}
 * ValueModel, if validation of the item succeeds.
 * <p>
 * @author sdedic
 */
public interface InteractiveEntry<T> {
    /**
     * Access to description of the interactive entry.
     * @return 
     */
    public DataEntry      data();
    
    /**
     * Request that the UI for the entry activates.s
     */
    public void           requestActive();
    
    /**
     * User-facing model, associated with the UI.
     * @return 
     */
    public EditableModel   getInput();
    
    /**
     * Validator for the user input. This the 'first level' validator,
     * 
     * @return 
     */
    public Validator<T>    getValidator();
    
    /**
     * Provides access to feedback area.
     * @return 
     */
    public ValidationFeedback  getFeedback();

    /**
     * @return access to the component
     */
    public JComponent getComponent();
}
