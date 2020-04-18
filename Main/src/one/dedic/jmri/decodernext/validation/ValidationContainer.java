/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

import javax.swing.JComponent;

/**
 * Collects validation related services. The Container is implemented for
 * {@link ValidatorService} and {@link ValidationFeedback}. The interface should
 * be implemented on parent components, or attached to them, so a child, when
 * added dynamically to the hierarchy can connect to the validation 
 * and feedback services.
 * 
 * @author sdedic
 */
public interface ValidationContainer<T extends SwingAttached> {
    /**
     * Attaches a child to the parent service. If the service is already
     * active, it will call {@link SwingAttached#addNotify} back on the
     * child.
     * @param child child instance 
     */
    public void attachChild(JComponent ui, T child);

    /**
     * Detaches a child to from parent service. If the service is still
     * active, it will call back {@link SwingAttached#removeNotify()} on the child.
     * @param child child instance
     */
    public void dettachChild(JComponent ui, T child);
}
