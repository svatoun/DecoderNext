/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.model;

import com.jgoodies.binding.value.ComponentModel;
import java.util.concurrent.CompletableFuture;
import javax.swing.JComponent;

/**
 * An extension of the JGoodies ComponentModel. A Component can be:
 * <ul>
 * <li>visible (default: true); an invisible component should be hidden from the user view. Implies enabled = false.
 * <li>enabled (default true); a disabled component can be shown, but must not take active part. Implies active = false
 * <li>editable (default: true); special sub-state of enabled. Non-editable components prevent users from free input, can only offer choices.
 * <li>active (default: false); the component can be active on screen. Inactive components are typically hidden (tabs, views, subdialogs).
 * </ul>
 * 
 * A Component may be requested as active. The request may be completed immediately, or in the future.
 * 
 * @author sdedic
 */
public interface ExComponentModel extends ComponentModel {
    /**
     * The name of the property that holds component's active state.
     */
    public String PROPERTY_ACTIVE = "active";

    /**
     * Determines if the UI is active.
     * @return 
     */
    public boolean isActive();
    
    /**
     * Requests that a component becomes active. If the request completes successfully, a JComponent
     * instance will be provided.
     * @return 
     */
    public CompletableFuture<JComponent> requestDisplay(boolean requestActive);
}
