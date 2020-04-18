/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

/**
 * Interface to inform about activation and deactivation. Implementors
 * may attach/detach listeners or perform delayed initialization on first attach.
 * <p/>
 * {@link #addNotify} may not be called repeatedly for a single attach. If {@link #addNotify}
 * is called, {@link #removeNotify} must be called before next call to {@link #addNotify}.
 * @author sdedic
 */
public interface SwingAttached {
    /**
     * Attaches and activates the implementation.
     */
    public void addNotify();
    
    /**
     * Detaches and deactivates the implementation.
     */
    public void removeNotify();
    
}
