/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import java.util.EventObject;
import javax.swing.JComponent;

/**
 * Information on the changed control.
 * @author sdedic
 */
public class ControlChangeEvent extends EventObject {
    private final JComponent  changedControl;
    
    public ControlChangeEvent(Object source, JComponent control) {
        super(source);
        this.changedControl = control;
    }

    public JComponent getChangedControl() {
        return changedControl;
    }
}
