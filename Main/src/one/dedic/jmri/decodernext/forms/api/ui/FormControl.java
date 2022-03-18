/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.ui;

import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public interface FormControl extends Lookup.Provider {
    
    public Feedback getFeedback();
    
    /**
     * Access either presentation or value model extensions. By default, the lookup
     * is empty; implementations may add services to it.
     * @return service lookup for the model.
     */
    public default Lookup getLookup() {
        return Lookup.EMPTY;
    }
    
    public ExComponentModel getComponent();
}
