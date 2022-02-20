/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

import java.beans.FeatureDescriptor;

/**
 *
 * @author sdedic
 */
public final class EntryDescriptor extends FeatureDescriptor {
    /**
     * Unique ID of the entry. Use dotted notation, if necessary.
     */
    private final String  id;
    
    /**
     * Base resource name for icons.
     */
    private String  iconBase;
    
    /**
     * Value's representation class.
     */
    private Class<?> valueClass;

    public EntryDescriptor(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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
