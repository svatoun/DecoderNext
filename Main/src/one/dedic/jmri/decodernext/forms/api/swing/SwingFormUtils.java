/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.swing;

import one.dedic.jmri.decodernext.model.formx.swing.*;
import javax.swing.JComponent;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public final class SwingFormUtils {
    private SwingFormUtils() {
    }
    
    public static final String CLIENTPROP_COMPONENT_LOOKUP = "forms.lookup"; // NOI18N

    public static final String PROPERTY_VALIDATED = "forms.validated";
    public static final String PROPERTY_FORM_PART = "forms.part";
    public static final String PROPERTY_EDITABLE_MODEL = "forms.editable";
    
    public static Lookup getComponentLookup(JComponent c) {
        if (c instanceof Lookup.Provider) {
            return ((Lookup.Provider)c).getLookup();
        }
        Object o = c.getClientProperty(CLIENTPROP_COMPONENT_LOOKUP);
        if (o == null) {
            return null;
        }
        if (o instanceof Lookup) {
            return ((Lookup)o);
        } else if (o instanceof Lookup.Provider) {
            return ((Lookup.Provider)o).getLookup();
        }
        return null;
    }
}
