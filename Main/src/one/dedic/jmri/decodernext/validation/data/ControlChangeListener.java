/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

/**
 * Can listen for control changes in an abstract way.
 * @author sdedic
 */
public interface ControlChangeListener {
    public void controlChanged(ControlChangeEvent e);
}
