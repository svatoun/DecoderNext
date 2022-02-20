/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.model;

import java.beans.PropertyDescriptor;
import java.util.Collections;

/**
 *
 * @author sdedic
 */
public class ModelUtilities {
    public static EntryDescriptor fromProperty(String beanId, PropertyDescriptor pd) {
        EntryDescriptor d = new EntryDescriptor();
        d.setName(beanId + "." + pd.getName());
        d.setDisplayName(pd.getDisplayName());
        
        for (String s : Collections.list(pd.attributeNames())) {
            d.setValue(s, pd.getValue(s));
        }
        Object o = pd.getValue("iconBase");
        if (o instanceof String) {
            d.setIconBase(o.toString());
        }
        d.setExpert(pd.isExpert());
        d.setHidden(pd.isHidden());
        d.setPreferred(pd.isPreferred());
        d.setShortDescription(pd.getShortDescription());
        d.setValueClass(pd.getPropertyType());
        return d;
    }
}
