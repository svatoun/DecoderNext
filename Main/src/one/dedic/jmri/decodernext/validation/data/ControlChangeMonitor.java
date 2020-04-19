/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

/**
 * A service that monitors changes on UI elements. A change means that
 * the UI is edited or altered so the parent becomes "dirty" or the UI
 * may need a validation.
 * <p>
 * Implementations are specific for individual controls.
 * @author sdedic
 */
public interface ControlChangeMonitor {
    public void addControlChangeListener(ControlChangeListener l);
    public void removeControlChangeListener(ControlChangeListener l);
    public void resetChangeState();
    public boolean isDirty();
}
