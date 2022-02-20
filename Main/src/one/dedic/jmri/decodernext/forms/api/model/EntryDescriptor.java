/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.model;

import java.beans.FeatureDescriptor;

/**
 * A simple extension of {@link FeatureDescriptor} that adds an icon.
 * @author sdedic
 */
public final class EntryDescriptor extends FeatureDescriptor {
    /**
     * Base resource name for icons.
     */
    private String  iconBase;
    
    /**
     * Value's representation class.
     */
    private Class<?> valueClass;

    public EntryDescriptor() {
    }

    public String getIconBase() {
        return iconBase;
    }

    public void setIconBase(String iconBase) {
        this.iconBase = iconBase;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }

    public void setValueClass(Class<?> valueClass) {
        this.valueClass = valueClass;
    }
}
