/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.swing;

import com.jgoodies.binding.value.ComponentModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.common.bean.Bean;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import one.dedic.jmri.decodernext.forms.api.model.DelayedInputModel;
import one.dedic.jmri.decodernext.forms.api.model.TypedValueModel;

/**
 * Factory for {@link ValueModel}s for standard Swing classes.
 * @author sdedic
 */
public final class StandardInputModels {
    public static TypedValueModel<String> textContent(JTextComponent jt) {
        return new TextContentModel(jt);
    }
    
    public static ValueModel textContentDelayed(JTextComponent jt) {
        return DelayedInputModel.create(textContent(jt), 200, true);
    }
    
    public static ComponentModel textComponent(JTextComponent jt) {
        return BasicControlModel.create(jt);
    }
    
    private static final Object NONE = new Object() {
        @Override
        public String toString() {
            return "<NONE>";
        }
    };
    
    /**
     * Abstract {@link ValueModel} for Swing components. Events are all reported
     * in the EDT, but the model allows to get/set values from an arbitrary thread.
     * 
     * @param <T> value type
     * @param <C> component type
     */
    private static abstract class AbstractSwingModel<T, C extends JComponent> extends Bean implements ValueModel {
        protected final C component;
        private T valueCopy;
        private Object pendingSetValue = NONE;

        public AbstractSwingModel(C component) {
            this.component = component;
        }
        
        protected abstract T fromComponent();
        protected abstract void toComponent(T value);

        @Override
        public Object getValue() {
            if (SwingUtilities.isEventDispatchThread()) {
                return valueCopy = fromComponent();
            } else {
                if (pendingSetValue != NONE) {
                    return pendingSetValue;
                } else {
                    SwingUtilities.invokeLater(this::syncValueCopy);
                    return valueCopy;
                }
            }
        }
        
        protected synchronized T valueCopy() {
            return valueCopy;
        }
        
        protected synchronized T updateCopy(T nc) {
            T o = this.valueCopy;
            valueCopy = nc;
            return o;
        }
        
        protected void syncValueCopy() {
            T o = fromComponent();
            Object v = updateCopy(o);
            if (Objects.equals(o, v)) {
                firePropertyChange(PROPERTY_VALUE, o, v);
            }
        }

        @Override
        public void setValue(Object val) {
            Object pvs;
            synchronized (this) {
                if (!SwingUtilities.isEventDispatchThread()) {
                    pendingSetValue = (T)val;
                    SwingUtilities.invokeLater(() -> setValue(val));
                    return;
                }
                if (pendingSetValue != NONE && pendingSetValue != val) {
                    return;
                }
                pendingSetValue = NONE;
            }
            toComponent((T)val);
        }

        @Override
        public void addValueChangeListener(PropertyChangeListener l) {
            addPropertyChangeListener(PROPERTY_VALUE, l);
        }

        @Override
        public void removeValueChangeListener(PropertyChangeListener l) {
            removePropertyChangeListener(PROPERTY_VALUE, l);
        }
        
        protected final void valueChanged() {
            assert SwingUtilities.isEventDispatchThread();
            T oldV = valueCopy;
            valueCopy = fromComponent();
            firePropertyChange(PROPERTY_VALUE, oldV, valueCopy);
        }
        
    }
    
    /**
     * Input model suitable for text-based components.
     */
    private static class TextContentModel extends AbstractSwingModel<String, JTextComponent> 
        implements DocumentListener, TypedValueModel<String> {
        public TextContentModel(JTextComponent textComponent) {
            super(textComponent);
            textComponent.getDocument().addDocumentListener(this);
        }

        @Override
        public Class<String> valueClass() {
            return String.class;
        }

        @Override
        protected String fromComponent() {
            return component.getText().trim();
        }

        @Override
        protected void toComponent(String value) {
            component.setText(value);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            valueChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            valueChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            valueChanged();
        }
    }
}
