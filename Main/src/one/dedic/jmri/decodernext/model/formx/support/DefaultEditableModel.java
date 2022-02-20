/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.support;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.common.bean.Bean;
import com.jgoodies.validation.Validator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.CompletableFuture;
import one.dedic.jmri.decodernext.model.formx.model.EntryDescriptor;
import one.dedic.jmri.decodernext.model.formx.model.InputContext;
import one.dedic.jmri.decodernext.model.formx.model.BufferedModel;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import one.dedic.jmri.decodernext.model.formx.model.ExComponentModel;
import one.dedic.jmri.decodernext.model.formx.model.InputContextAware;
import one.dedic.jmri.decodernext.model.formx.spi.ValidatorSetup;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import one.dedic.jmri.decodernext.model.formx.spi.EditableFormItem;

/**
 *
 * @author sdedic
 */
public class DefaultEditableModel implements EditableFormItem, InputContextAware {
    private final EntryDescriptor descriptor;
    private final DataItem data;
    private final BufferedModel inputModel;
    private final ExComponentModel uiModel;
    private Lookup explicitLookup;
    private Validator validator;

    public DefaultEditableModel(EntryDescriptor descriptor, DataItem data, ValueModel inputModel, ExComponentModel uiModel) {
        this.descriptor = descriptor;
        this.data = data;
        this.inputModel = inputModel instanceof BufferedModel ?
                (BufferedModel)inputModel : new DelegatingBufferedModel(inputModel);
        this.uiModel = uiModel;
    }

    @Override
    public void useInputContext(InputContext ctx) {
        InputContext.callUseInputContext(ctx, inputModel, uiModel, validator);
    }
    
    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
        if (validator instanceof ValidatorSetup) {
            ValidatorSetup ss = (ValidatorSetup)validator;
            ss.setLabel(getDescriptor().getDisplayName());
            ss.setMessageKey(getDescriptor().getId());
        }
    }
    
    public void setLookup(Lookup lkp) {
        synchronized (this) {
            Lookup del = getDelegateLookup();
            if (del == null) {
                this.explicitLookup = lkp;
            } else {
                this.explicitLookup = new ProxyLookup(lkp, del);
            }
        }
    }

    @Override
    public Lookup getLookup() {
        synchronized (this) {
            if (explicitLookup != null) {
                return explicitLookup;
            } 
        }
        Lookup lkp = getDelegateLookup();
        return lkp == null ? Lookup.EMPTY : lkp;
    }
    
    private Lookup getDelegateLookup() {
        if (inputModel instanceof Lookup) {
            return (Lookup)inputModel;
        } else if (inputModel instanceof Lookup.Provider) {
            return ((Lookup.Provider)inputModel).getLookup();
        } else {
            return null;
        }
    }
    
    @Override
    public EntryDescriptor getDescriptor() {
        return descriptor != null ? descriptor : data.getDescriptor();
    }

    @Override
    public DataItem getEditedData() {
        return data;
    }

    @Override
    public BufferedModel getInputModel() {
        return inputModel;
    }

    @Override
    public ExComponentModel getComponentModel() {
        return uiModel;
    }

    static class DelegatingBufferedModel extends Bean implements BufferedModel, Lookup.Provider, PropertyChangeListener {
        private final ValueModel del;
        private final Lookup.Provider lkp;

        @Override
        public ValueModel getDelegate() {
            return del;
        }

        @Override
        public boolean isDirty() {
            return del instanceof BufferedModel ? ((BufferedModel)del).isDirty() : false;
        }
        
        public DelegatingBufferedModel(ValueModel del) {
            this.del = del;
            if (del instanceof Lookup.Provider) {
                lkp = ((Lookup.Provider)del);
            } else {
                lkp = () -> Lookup.EMPTY;
            }
        }

        @Override
        protected PropertyChangeSupport createPropertyChangeSupport(Object bean) {
            del.addValueChangeListener(this);
            return super.createPropertyChangeSupport(bean);
        }

        @Override
        public Lookup getLookup() {
            return lkp.getLookup();
        }
        
        @Override
        public Object getValue() {
            return del.getValue();
        }

        @Override
        public void setValue(Object arg0) {
            del.setValue(arg0);
        }

        @Override
        public CompletableFuture<Object> getTargetValue() {
            return del instanceof BufferedModel ? ((BufferedModel)del).getTargetValue() : BufferedModel.getTargetValue(del);
        }

        @Override
        public Object getPendingValue() {
            return del instanceof BufferedModel ? ((BufferedModel)del).getPendingValue(): del.getValue();
        }

        @Override
        public void addValueChangeListener(PropertyChangeListener l) {
            addPropertyChangeListener(PROPERTY_VALUE, l);
        }

        @Override
        public void removeValueChangeListener(PropertyChangeListener l) {
            removePropertyChangeListener(PROPERTY_VALUE, l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
        
    }
}
