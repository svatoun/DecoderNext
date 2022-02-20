/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.swing;

/**
 *
 * @author sdedic
 */
public interface TreeCollectorListener<T> {
    public default void itemsAdded(TreeCollectorEvent<T> e) {}
    public default void itemsRemoved(TreeCollectorEvent<T> e) {}
    public default void itemsChanged(TreeCollectorEvent<T> e) {}
}
