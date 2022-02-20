/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

import javax.swing.JComponent;

/**
 * Pluggable factory to create an {@link InteractiveEntry} descriptor out 
 * from a JComponent.
 * 
 * @author sdedic
 */
public interface ComponentEntryProvider {
    /**
     * Turns a JComponent instance info an InteractiveEntry.
     * @param c
     * @return 
     */
    public InteractiveEntry createEntry(DataEntry d, JComponent c);
}
