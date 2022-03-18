/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.model;

import java.beans.PropertyChangeListener;

/**
 *
 * @author sdedic
 */
public interface HasEnable {
    /**
     * enabled property name
     */
    public static final String PROP_ENABLED = "enabled";
    public static final String PROP_VISIBLE = "visible";
    
    /**
     * @return true, if the model is enabled.
     */
    public boolean isEnabled();
    
    /**
     * Enables or disables the model for inputs
     * @param enabled enablement status
     */
    public void setEnabled(boolean enabled);
    
    public void addPropertyChangeListener(PropertyChangeListener l);
    public void removePropertyChangeListener(PropertyChangeListener l);
}
