/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms;

import one.dedic.jmri.decodernext.jgoodies.ValueWeakListener;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.common.bean.Bean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CompletableFuture;
import one.dedic.jmri.decodernext.forms.api.model.BufferedModel;
import static one.dedic.jmri.decodernext.forms.api.model.BufferedModel.PROP_DIRTY;
import one.dedic.jmri.decodernext.forms.api.model.DataInputException;
import one.dedic.jmri.decodernext.forms.api.model.ModelUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binds an input model to a data model, forwarding only representable
 * inputs to the data layer. All data changes will be however propagated
 * to the input layer.
 * <p>
 * The input model need not to be connected initially. It will receive the current
 * data value when it is connected.
 * 
 * @author sdedic
 */
public class BasicInputModel<T> extends Bean implements ValueModel, BufferedModel {
    private static final Logger LOG = LoggerFactory.getLogger(BasicInputModel.class);
    
    private ValueModel    dataModel;
    private ValueModel    inputModel;
    private boolean dirty;
    private boolean suppress;
    private PropertyChangeListener weakL;
    private PropertyChangeListener iL;
    
    public BasicInputModel() {
    }

    public BasicInputModel(ValueModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ValueModel getDelegate() {
        return inputModel;
    }
    
    protected void setDataModel(ValueModel m) {
        synchronized (this) {
            if (dataModel != null && weakL != null) {
                dataModel.removeValueChangeListener(weakL);
                weakL = null;
            }
            dataModel = m;
            attachDataListener();
        }
    }
    
    // @GuardedBy(this)
    private void attachDataListener() {
        if (dataModel == null || inputModel == null || weakL != null) {
            return;
        }
        weakL = new ValueWeakListener(dataModel, this::forwardToInput);
        dataModel.addValueChangeListener(weakL);
        iL = this::forwardInputEvents;
        if (inputModel instanceof BufferedModel) {
            ((BufferedModel)inputModel).addPropertyChangeListener(iL);
        } else {
            inputModel.addValueChangeListener(iL);
        }
    }
    
    protected void setInputModel(ValueModel m) {
        synchronized (this) {
            if (inputModel != null) {
                if (inputModel instanceof BufferedModel) {
                    ((BufferedModel)inputModel).removePropertyChangeListener(iL);
                } else {
                    inputModel.removeValueChangeListener(iL);
                }
                iL = null;
                if (m == null && weakL != null) {
                    dataModel.removeValueChangeListener(weakL);
                    weakL = null;
                }
            }
            this.inputModel = m;
            attachDataListener();
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void forwardToInput(PropertyChangeEvent evt) {
        if (!ValueModel.PROPERTY_VALUE.equals(evt.getPropertyName())) {
            return;
        }
        Object o = dataModel.getValue();
        try {
            forwardToInput((T)o);
        } catch (ClassCastException ex) {
            LOG.warn("Invalid type {}: ", o.getClass());
        } catch (IllegalArgumentException ex) {
            LOG.warn("Value {} rejectted by input {}: {}",
                o, inputModel, ex);
        }
    }
    
    protected ValueModel getInputModel() {
        return inputModel;
    }
    
    protected void forwardToInput(T value) {
        // relevant property change will be fired by the input model.
        inputModel.setValue(value);
    }

    @Override
    public Object getValue() {
        ValueModel im = inputModel;
        return im != null ? 
                im.getValue() :
                dataModel.getValue();
    }

    @Override
    public void setValue(Object val) {
        // set to the data model; it will fire an event that will be reflected
        // into the input model (if it exists)
        synchronized (this) {
            attachDataListener();
        }
        try {
            dataModel.setValue(val);
        } catch (DataInputException ex) {
            onInputError(val, ex);
        }
    }

    @Override
    public void addValueChangeListener(PropertyChangeListener pl) {
        addPropertyChangeListener(PROPERTY_VALUE, pl);
    }

    @Override
    public void removeValueChangeListener(PropertyChangeListener pl) {
        removePropertyChangeListener(PROPERTY_VALUE, pl);
    }

    @Override
    public boolean isDirty() {
        ValueModel m = inputModel;
        if (dirty) {
            return true;
        }
        if (m instanceof BufferedModel) {
            return ((BufferedModel)m).isDirty();
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getPendingValue() {
        return (T)ModelUtilities.getPendingValue(inputModel);
    }

    @Override
    public CompletableFuture<Object> getTargetValue(boolean allowSpecial) {
        return ModelUtilities.getTargetValue(inputModel, allowSpecial);
    }
    
    private void forwardToData0() {
    }
    
    protected Object onInputError(Object value, DataInputException ex) {
        // no op
        boolean oDirty;
        synchronized (this) {
            oDirty = dirty;
            dirty = true;
        }
        firePropertyChange(PROP_DIRTY, oDirty, dirty);
        return value;
    }
    
    protected ValueModel getDataModel() {
        return dataModel;
    }
    
    public boolean isBroken() {
        return false;
    }
    
    protected void forwardDirtyStatus(PropertyChangeEvent evt) {
        firePropertyChange(PROP_DIRTY, evt.getOldValue(), evt.getNewValue());
    }
    
    protected synchronized boolean isDirtyStatus() {
        return dirty;
    }
    
    protected void setDirty(boolean d) {
        boolean od;
        synchronized (this) {
            od = isDirty();
            dirty = d;
            if (od == isDirty()) {
                return;
            }
        }
        firePropertyChange(PROP_DIRTY, od, d);
    }
    
    protected Object forwardToData(Object prev) {
        Object v = null;
        try {
            ValueModel im = inputModel;
            v = im.getValue();
        } catch (IllegalArgumentException ex) {
            return onInputError(v, new DataInputException(ex.getLocalizedMessage(), ex));
        } catch (DataInputException ex) {
            // this is expected...
            return onInputError(v, ex);
        } catch (IllegalStateException ex) {
            // unexpected...
        }
        return forwardToData(prev, v);
    }
    
    protected void fireValueChange(Object old, Object val) {
        firePropertyChange(ValueModel.PROPERTY_VALUE, old, val);
    }
    
    protected Object forwardToData(Object prev, Object val) {
        try {
            suppress = true;
            setDirty(false);
            dataModel.setValue(val);
        } catch (DataInputException ex) {
            // this is expected...
            return onInputError(val, ex);
        } catch (IllegalStateException ex) {
            // unexpected...
        } finally {
            suppress = false;
        }
        firePropertyChange(ValueModel.PROPERTY_VALUE, prev, val);
        return val;
    }
    
    protected static final Object SUPPRESS = new Object();
    
    private void forwardInputEvents(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null) {
            firePropertyChange(null, evt.getOldValue(), evt.getNewValue());
            return;
        }
        Object nv = evt.getNewValue();
        switch (evt.getPropertyName()) {
                // forward even dirty values:
            case ValueModel.PROPERTY_VALUE:
                if (suppress) {
                    return;
                }
                nv = forwardToData(evt.getOldValue());
                /*
                if (nv == SUPPRESS) {
                    return;
                }
                break;
                */
                return;
            case PROP_DIRTY:
                forwardDirtyStatus(evt);
                return;
            default:
                // forward any other input events
                break; 
        }
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), nv);
    }

    public static final ValueModel NULL_MODEL = new ValueModel() {
        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public void setValue(Object arg0) {}
        @Override
        public void addValueChangeListener(PropertyChangeListener _a) {}
        @Override
        public void removeValueChangeListener(PropertyChangeListener _a) {}
    };
    
}
