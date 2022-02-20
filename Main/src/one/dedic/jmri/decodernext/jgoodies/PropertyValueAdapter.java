/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.jgoodies;

import com.jgoodies.binding.beans.BeanUtils;
import com.jgoodies.binding.beans.PropertyAccessor;
import com.jgoodies.binding.beans.PropertyAccessors;
import static com.jgoodies.binding.beans.PropertyAdapter.PROPERTY_CHANGED;
import com.jgoodies.binding.beans.PropertyNotBindableException;
import com.jgoodies.binding.beans.PropertyUnboundException;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.common.base.Objects;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import one.dedic.jmri.decodernext.model.formx.model.DataInputException;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import one.dedic.jmri.decodernext.model.formx.model.FormValueModel;
import org.openide.util.WeakListeners;

/**
 * Property adapter for {@link ValueModel}. The adapter tries first to attach using
 * named property listener, then using generic property listener (but still filters for the name.
 * 
 * @author sdedic
 */
public class PropertyValueAdapter<T> extends AbstractValueModel implements FormValueModel {
    private final T bean;
    private final String propertyName;
    private final String getterName;
    private final String setterName;
    private final PropertyAccessor accessor;
    
    private DataItem item;
    
    private boolean enabled;
    private boolean changed;
    private PropertyChangeHandler handler;
    private boolean fireSelf;
    
    public PropertyValueAdapter(T bean, String propertyName) {
        this(bean, propertyName, null, null);
    }

    public PropertyValueAdapter(T bean, String propertyName, String getterName, String setterName) {
        this.bean = bean;
        this.propertyName = propertyName;
        this.getterName = getterName;
        this.setterName = setterName;
        
        accessor = PropertyAccessors.getProvider().getAccessor(bean.getClass(), 
                propertyName, getterName, setterName);
    }

    public DataItem getDataItem() {
        return item;
    }

    public void setItem(DataItem dataItem) {
        this.item = item;
    }

    @Override
    protected PropertyChangeSupport createPropertyChangeSupport(Object bean) {
        PropertyChangeHandler h = new PropertyChangeHandler();
        try {
            try {
                BeanUtils.addPropertyChangeListener(getBean(), getPropertyName(), 
                        WeakListeners.propertyChange(h, getPropertyName(), bean));
                handler = h;
            } catch (PropertyNotBindableException ex) {
                BeanUtils.addPropertyChangeListener(getBean(), WeakListeners.propertyChange(h, bean));
                handler = h;
            }
        } catch (PropertyUnboundException ex) {
            // ignore
            fireSelf = true;
        }
        return super.createPropertyChangeSupport(bean);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        boolean oe = this.enabled;
        this.enabled = enabled;
        
        firePropertyChange(PROP_ENABLED, oe, enabled);
    }
    
    @Override
    public Object getValue() {
        return accessor.getValue(bean);
    }

    @Override
    public void setValue(Object val) {
        Object o = null;
        if (fireSelf) {
            try {
                o = getValue();
            } catch (IllegalStateException ex) {
                // ignore.
            }
        }
        try {
            accessor.setValue(bean, val);
        } catch (PropertyVetoException ex) {
            throw new DataInputException(item, ex.getLocalizedMessage(), ex);
        }
        if (fireSelf) {
            if (!Objects.equals(o, val)) {
                setChanged(true);
            }
            firePropertyChange(PROPERTY_VALUE, o, val);
        }
    }
    
    public T getBean() {
        return bean;
    }

    public String getPropertyName() {
        return propertyName;
    }
    
    public boolean isChanged() {
        return changed;
    }


    /**
     * Resets this tracker's changed state to {@code false}.
     */
    public void resetChanged() {
        setChanged(false);
    }


    /**
     * Sets the changed state to the given value. Invoked by the global
     * PropertyChangeHandler that observes all bean changes. Also invoked
     * by {@code #resetChanged}.
     *
     * @param newValue  the new changed state
     */
    private void setChanged(boolean newValue) {
        boolean oldValue = isChanged();
        changed = newValue;
        firePropertyChange(PROPERTY_CHANGED, oldValue, newValue);
    }

    /**
     * Listens to changes of all bean properties. Fires property changes
     * if the associated property or an arbitrary set of properties has changed.
     */
    private final class PropertyChangeHandler implements PropertyChangeListener {

        /**
         * A bean property has been changed. Sets the changed state to true.
         * Checks whether the observed
         * property or multiple properties have changed.
         * If so, notifies all registered listeners about the change.
         *
         * @param evt   the property change event to be handled
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setChanged(true);
            if (evt.getPropertyName() == null) {
                forwardAdaptedValueChanged(getBean());
            } else if (evt.getPropertyName().equals(getPropertyName())) {
                fireValueChange(evt.getOldValue(), evt.getNewValue(), true);
            }
        }
    }

    private boolean isWriteOnlyProperty(T bean) {
        return accessor.isWriteOnly();
    }

    private void forwardAdaptedValueChanged(T newBean) {
        Object newValue = newBean == null || isWriteOnlyProperty(newBean)
            ? null
            : accessor.getValue(bean);
        fireValueChange(null, newValue);
    }
}
