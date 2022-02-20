/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

import com.jgoodies.validation.ValidationResult;
import javax.swing.JComponent;

/**
 *
 * @author sdedic
 */
public interface FormItem {
    public DataEntry        getModel();
    
    public EditableModel    getInputModel();
    
    public ValidationResult getValidationResult();
    
    public void             requestFocus();
    
    public JComponent       getComponent();
}
