/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.spi.ui;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.Validator;
import one.dedic.jmri.decodernext.forms.api.model.BufferedModel;
import one.dedic.jmri.decodernext.forms.api.model.DataItem;
import one.dedic.jmri.decodernext.forms.api.model.EntryDescriptor;
import one.dedic.jmri.decodernext.forms.api.ui.FormControl;

/**
 * API for editable part suppliers; describes editable item on the form. The editable item
 * has
 * <ul>
 * <li>a data (storage) model, described as {@link DataItem}. This model is used to store the value
 * and <b>should</b> be used to listen or modify the value.
 * <li>an input model, derived from the UI. Its implementation should extract data from the UI, and
 * possibly convert it to fit the data model.
 * <li>a Component model that controls visual appearance.
 * <li>can provide extended services, through Lookup.
 * </ul>
 * EditableModels can be virtual and its UI may not be created yet. The UI must be created when the component
 * is activated, but may be created at any time before that.
 * 
 * @author sdedic
 */
public interface EditableItem extends FormControl {
    /**
     * Returns metadata of this editable model.
     * @return descriptor.
     */
    public default EntryDescriptor  getDescriptor() {
        return getEditedData().getDescriptor();
    }
    
    /**
     * Represents data model for the form item. Access the data layer and possibly validates
     * the input.
     * @return 
     */
    public DataItem    getEditedData();
    
    /**
     * Represents the value model of the user input. The value model may implement extra
     * properties, like {@link BufferedModel}.
     * @return user input model.
     */
    public ValueModel   getInputModel();
    
    public Validator<?>    getValidator();
}
