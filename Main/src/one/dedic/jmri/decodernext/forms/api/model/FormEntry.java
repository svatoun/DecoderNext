/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.model;

import com.jgoodies.binding.value.ValueModel;
import one.dedic.jmri.decodernext.model.formx.validation.Validated;

/**
 * Allows to enable or disable the model. When a model is disabled,
 * its validations are suppressed. The setEnabled should be seen as a
 * <b>request</b>, that, if satisfied, will fire relevant PROP_ENABLED
 * property change event.
 * <p>
 * If a ValueModel is <b>disabled</b> all associated UIs should become disabled 
 * as well.
 * 
 * @author sdedic
 */
public interface FormEntry extends AbstractItem, HasDescription, HasEnable {
    
    public ValueModel getModel();
    
    public Validated getValidated();
}
