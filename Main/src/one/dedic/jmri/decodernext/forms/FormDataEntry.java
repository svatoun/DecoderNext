/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms;

import com.jgoodies.binding.value.ValueModel;
import one.dedic.jmri.decodernext.forms.api.model.EntryDescriptor;
import one.dedic.jmri.decodernext.forms.api.model.FormEntry;
import one.dedic.jmri.decodernext.forms.api.ui.ExComponentModel;
import one.dedic.jmri.decodernext.forms.api.ui.Feedback;
import one.dedic.jmri.decodernext.forms.api.ui.FormControl;
import one.dedic.jmri.decodernext.forms.spi.ui.EditableItem;
import one.dedic.jmri.decodernext.model.formx.validation.Validated;

/**
 *
 * @author sdedic
 */
public class FormDataEntry<T> extends BasicInputModel<T> implements FormEntry, FormControl {
    private final EditableItem editableItem;
    private boolean enabled = true;
    
    public FormDataEntry(EditableItem editableItem) {
        super(editableItem.getEditedData().getModel());
        this.editableItem = editableItem;
    }

    @Override
    public boolean isEnabled() {
        return enabled && editableItem.getComponent().isVisible();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ValueModel getModel() {
        return super.getInputModel();
    }

    @Override
    public Validated getValidated() {
        return null;
    }

    @Override
    public EntryDescriptor getDescriptor() {
        return editableItem.getDescriptor();
    }

    @Override
    public Feedback getFeedback() {
        return editableItem.getFeedback();
    }

    @Override
    public ExComponentModel getComponent() {
        return editableItem.getComponent();
    }
}
