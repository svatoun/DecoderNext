/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.ui;

import com.jgoodies.binding.value.ComponentModel;
import java.util.concurrent.CompletableFuture;
import javax.swing.JComponent;
import one.dedic.jmri.decodernext.forms.api.input.InputContextAware;
import org.openide.util.Lookup;

/**
 * An extension of the JGoodies ComponentModel. A Component can be:
 * <ul>
 * <li>visible (default: true); an invisible component should be hidden from the user view. Implies enabled = false.
 * <li>enabled (default true); a disabled component can be shown, but must not take active part. Implies active = false
 * <li>editable (default: true); special sub-state of enabled. Non-editable components prevent users from free input, can only offer choices.
 * <li>active (default: false); the component can be active on screen. Inactive components are typically hidden (tabs, views, subdialogs).
 * </ul>
 * 
 * A Component may be requested as active. The request may be completed immediately, or in the future. The Future will
 * be always completed in EDT.
 * 
 * @author sdedic
 */
public interface ExComponentModel extends ComponentModel, /* InputContextAware, */Lookup.Provider {
    /**
     * The name of the property that holds component's active state.
     */
    public String PROPERTY_ACTIVE = "active";
    
    public String PROPERTY_DISPLAYED = "displayed";
    
    /**
     * Determines if the UI is active.
     * @return 
     */
    public boolean isActive();
    
    /**
     * @return true, if the component is displayed.
     */
    public boolean isDisplayed();
    
    /**
     * Reveals the component. During this call, the component and all its parents
     * will eventually materialize. If `requestActive` is true, the component will
     * become active. The returned Future completes when the component reveals and
     * activates.
     * 
     * @return Future that will complete after the component is revealed.
     */
    public CompletableFuture<JComponent> reveal(boolean requestActive);
}
