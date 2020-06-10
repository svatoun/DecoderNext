/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import javax.swing.JComponent;
import org.openide.util.Lookup;

/**
 * An extensible factory to create change monitors for various
 * types of control.
 * @author sdedic
 */
public interface ControlChangeProvider {
    public ControlChangeMonitor  create(JComponent control, Lookup context);
}
