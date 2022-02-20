/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.support;

import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.JLabel;
import one.dedic.jmri.decodernext.model.formx.model.EntryDescriptor;
import one.dedic.jmri.decodernext.ui.UiUtils;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;

/**
 * Generates descriptors out of live UI component instances.
 * @author sdedic
 */
public class DescriptorGenerator {
    
    public static final String PROPERTY_ID = "form.desc.name";
    public static final String PROPERTY_LABEL = "form.desc.displayName";
    public static final String PROPERTY_DESC_PREFIX = "form.desc.";
    
    public static final String PROPERTY_DATA_ID = "data.desc.id";
    public static final String PROPERTY_DATA_LABEL = "data.desc.displayName";
    
    private final JComponent component;

    private Container parentContainer;
    
    private EntryDescriptor entryDescriptor;
    
    private String id;
    private String dataId;
    private String displayName;
    private String dataDisplayName;

    public DescriptorGenerator(JComponent component) {
        this.component = component;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDataDisplayName(String dataDisplayName) {
        this.dataDisplayName = dataDisplayName;
    }
    
    private void completeDataProps() {
        Object o;
        
        if (dataId == null) {
            o = component.getClientProperty(PROPERTY_DATA_ID);
            if (o instanceof String) {
                dataId = o.toString();
            }
        }
        if (dataDisplayName == null) {
            o = component.getClientProperty(PROPERTY_DATA_LABEL);
            if (o instanceof String) {
                dataDisplayName = o.toString();
            }
        }
        if (dataId == null) {
            // attempt to load from the default text, if specified.
        }
    }
    
    private void completeUIProps() {
        Object o;
        if (displayName == null) {
            o = component.getClientProperty(PROPERTY_LABEL);
            if (o == null) {
                o = component.getClientProperty("labeledBy");
            }
            if (o instanceof String) {
                displayName = o.toString();
            } else if (o instanceof JLabel) {
                String s = ((JLabel)o).getText();
                int m = Mnemonics.findMnemonicAmpersand(s);
                if (m > -1) {
                    s = s.substring(0, m) + s.substring(m + 1);
                }
                displayName = s;
            }
        }
        
        // try to find an associated label and get the displayName from it:
        if (displayName == null) {
            JLabel l = UiUtils.findDescriptiveLabel(component, parentContainer);
            if (l != null) {
                displayName = Actions.cutAmpersand(l.getText());
            }
        }
    }
}
