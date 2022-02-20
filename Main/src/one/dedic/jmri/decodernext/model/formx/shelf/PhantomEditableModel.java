/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.shelf;

import one.dedic.jmri.decodernext.jgoodies.ValueWeakListener;
import com.jgoodies.binding.value.ComponentModel;
import static com.jgoodies.binding.value.ComponentModel.PROPERTY_EDITABLE;
import static com.jgoodies.binding.value.ComponentModel.PROPERTY_ENABLED;
import static com.jgoodies.binding.value.ComponentModel.PROPERTY_VISIBLE;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.common.bean.Bean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CompletableFuture;
import javax.swing.JComponent;
import one.dedic.jmri.decodernext.model.formx.model.EntryDescriptor;
import one.dedic.jmri.decodernext.model.formx.model.DataItem;
import one.dedic.jmri.decodernext.model.formx.model.ExComponentModel;
import one.dedic.jmri.decodernext.model.formx.spi.EditableFormItem;

/**
 * Special model for input components that have not been yet materialized. 
 * Phantoms should be used to avoid UI initialization of large parts of complex forms
 * or dialogs, i.e. whole tabs, sheets or views.
 * <p>
 * Phantom model acts as a proxy for UI that was not yet initialized; its {@link #resolve}
 * method should be called when the component comes into existence. From that point on,
 * the phantom delegates everything to the real component. When resolved,
 * it will configure the real component model with the values collected so far.
 * 
 * @author sdedic
 */
public class PhantomEditableModel implements EditableFormItem {
    private final DataItem  editedData;
    private final ModelFactory f;
    private final EntryDescriptor descriptor;
    private ValueModel dataModel;
    private ValueModel inputModel;
    private ExComponentModel compModel;
    
    private EditableFormItem delegate;
    
    private DataValueModel phantomInput;
    private SimpleComponentModel phantomComponent;
    
    public PhantomEditableModel(DataItem editedData, EntryDescriptor desc) {
        this(editedData, desc, null);
    }

    public PhantomEditableModel(DataItem editedData, EntryDescriptor desc, ModelFactory f) {
        this.editedData = editedData;
        this.descriptor = desc;
        this.f = f;
    }

    public DataItem getEditedData() {
        return editedData;
    }

    public EntryDescriptor getDescriptor() {
        return descriptor;
    }
    
    protected CompletableFuture<JComponent> materializeAndActivate() {
        if (delegate != null) {
            return delegate.getComponentModel().requestDisplay(true);
        }
        if (f == null) {
            CompletableFuture<JComponent> cf = new CompletableFuture<>();
            cf.completeExceptionally(new IllegalStateException());
            return cf;
        }
        return f.create(descriptor, editedData).thenCompose((m) -> {
           resolve(m);
           return m.getComponentModel().requestDisplay(true);
        });
    }
    
    protected void resolve(EditableFormItem model) {
        boolean initInput = false;
        boolean initComp = false;
        
        synchronized (this) {
            if (delegate != null && model != delegate) {
                throw new IllegalStateException();
            }
            this.delegate = model;
        
            synchronized (this) {
                if (phantomInput != null) {
                    initInput = true;
                    phantomInput.reattach();
                }
                if (phantomComponent != null) {
                    initComp = true;
                    phantomComponent.reattach();
                }
            }
        }
        // initialize the component's input model with the data once
        // materialized.
        if (initInput) {
            model.getInputModel().setValue(dataModel.getValue());
        } 
        if (initComp) {
            ComponentModel d = model.getComponentModel();
            d.setEditable(phantomComponent.editable);
            d.setEnabled(phantomComponent.enabled);
            d.setVisible(phantomComponent.visible);
        }
    }

    @Override
    public ValueModel getInputModel() {
        synchronized (this) {
            if (inputModel == null) {
                if (delegate == null) {
                    phantomInput = new DataValueModel();
                    inputModel = phantomInput;
                } else {
                    inputModel = delegate.getInputModel();
                }
            }
        }
        return inputModel;
    }

    @Override
    public ExComponentModel getComponentModel() {
        synchronized (this) {
            if (compModel == null) {
                if (delegate == null) {
                    phantomComponent = new SimpleComponentModel();
                    compModel = phantomComponent;
                } else {
                    compModel = delegate.getComponentModel();
                }
            }
        }
        return compModel;
    }
    
    private final class SimpleComponentModel extends Bean implements ExComponentModel, PropertyChangeListener {
        private boolean enabled;
        private boolean visible;
        private boolean editable;

        SimpleComponentModel() {
            this.enabled = true;
            this.visible = true;
            this.editable = true;
        }


        @Override
        public boolean isEnabled() {
            EditableFormItem d = delegate;
            return d != null ? 
                    d.getComponentModel().isEnabled() : enabled;
        }


        @Override
        public void setEnabled(boolean b) {
            EditableFormItem d = delegate;
            if (d != null) {
                d.getComponentModel().setEnabled(b);
            } else {
                boolean oldEnabled = isEnabled();
                enabled = b;
                firePropertyChange(PROPERTY_ENABLED, oldEnabled, b);
            }
        }


        @Override
        public boolean isVisible() {
            EditableFormItem d = delegate;
            return d != null ? 
                    d.getComponentModel().isVisible(): visible;
        }


        @Override
        public void setVisible(boolean b) {
            EditableFormItem d = delegate;
            if (d != null) {
                d.getComponentModel().setVisible(b);
            } else {
                boolean oldVisible = isVisible();
                visible = b;
                firePropertyChange(PROPERTY_VISIBLE, oldVisible, b);
            }
        }


        @Override
        public boolean isEditable() {
            EditableFormItem d = delegate;
            return d != null ? 
                    d.getComponentModel().isEditable() : editable;
        }


        @Override
        public void setEditable(boolean b) {
            EditableFormItem d = delegate;
            if (d != null) {
                d.getComponentModel().setEditable(b);
            } else {
                boolean oldEditable = isEditable();
                editable = b;
                firePropertyChange(PROPERTY_EDITABLE, oldEditable, b);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
        
        void reattach() {
            if (delegate != null) {
                delegate.getComponentModel().addPropertyChangeListener(this);
            }
        }

        @Override
        public boolean isActive() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletableFuture<JComponent> requestDisplay(boolean activate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    /**
     * Dummy value model that forwards everything to the data, unless the delegate is initialized.
     */
    class DataValueModel extends Bean implements ValueModel, PropertyChangeListener {
        private PropertyChangeListener dataL;
        
        public DataValueModel() {
        }

        @Override
        public Object getValue() {
            return dataModel.getValue();
        }

        @Override
        public void setValue(Object v) {
            dataModel.setValue(v);
        }
        
        void reattach() {
            if (dataL == null) {
                return;
            }
            if (dataL != this) {
                dataModel.removeValueChangeListener(dataL);
                dataL = this;
                delegate.getInputModel().addValueChangeListener(this);
            }
        }

        @Override
        public void addValueChangeListener(PropertyChangeListener l) {
            if (dataL == null) {
                if (delegate == null) {
                    dataL = new ValueWeakListener(dataModel, this);
                    dataModel.addValueChangeListener(l);
                } else {
                    dataL = this;
                    delegate.getInputModel().addValueChangeListener(l);
                }
            }
            addPropertyChangeListener(PROPERTY_VALUE, l);
        }

        @Override
        public void removeValueChangeListener(PropertyChangeListener l) {
            removePropertyChangeListener(PROPERTY_VALUE, l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PROPERTY_VALUE.equals(evt.getPropertyName())) {
                firePropertyChange(PROPERTY_VALUE, evt.getOldValue(), evt.getNewValue());
            }
        }
    }
    
    public interface ModelFactory {
        CompletableFuture<EditableFormItem> create(EntryDescriptor desc, DataItem d);
    }
}
