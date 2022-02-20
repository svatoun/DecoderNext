/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

import com.jgoodies.binding.value.ValueModel;
import java.beans.PropertyChangeListener;

/**
 *
 * @author sdedic
 */
public interface BufferedModel extends ValueModel {
    public static final String PROP_DIRTY = "dirty";
    
    public void addPropertyChangeListener(PropertyChangeListener l);
    public void addPropertyChangeListener(String propName, PropertyChangeListener l);
    
    public void removePropertyChangeListener(PropertyChangeListener l);
    public void removePropertyChangeListener(String propName, PropertyChangeListener l);
    
    /**
     * The model is dirty, if it has not been written to underlying
     * data model. For example, if an integer edit box has been changed,
     * but contains input not representable as a number.
     * 
     * @return true, if the model is dirty 
     */
    public default boolean isDirty() {
        return false;
    }
    
    /**
     * Returns the pending value. If a value is being changed, returns the
     * pending value. If the value is settled already in the data model, returns
     * that value.
     * <p>
     * Note that pending value may be invalid, so the method may throw
     * an exception.
     * @return pending or settled value.
     */
    public default Object getPendingValue() {
        return getValue();
    }
}
