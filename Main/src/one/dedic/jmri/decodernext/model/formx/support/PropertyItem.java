/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.support;

import one.dedic.jmri.decodernext.jgoodies.PropertyValueAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.validation.Validator;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import one.dedic.jmri.decodernext.model.formx.model.EntryDescriptor;
import one.dedic.jmri.decodernext.model.formx.model.FormValueModel;
import org.openide.util.Lookup;

/**
 * DataItem that is realized as a property of a bean. Uses JGoodies
 * {@link PropertyAdapter} to access the value.
 * 
 * @author sdedic
 */
public final class PropertyItem<T> implements DataItem {
    private final T bean;
    private final EntryDescriptor descriptor;
    private PropertyValueAdapter<T> adapter;
    private Lookup context = Lookup.EMPTY;
    
    public PropertyItem(T bean, String propertyName, EntryDescriptor desc) {
        this.bean = bean;
        this.descriptor = desc;
        adapter = new PropertyValueAdapter<>(bean, propertyName);
        adapter.setItem(this);
    }
    
    public PropertyItem<T> withContext(Lookup context) {
        this.context = context;
        return this;
    }
    
    public PropertyItem<T> addValidator(Validator v) {
        return this;
    }

    @Override
    public EntryDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public FormValueModel getModel() {
        return adapter;
    }

    @Override
    public String id() {
        return descriptor.getId();
    }
}
