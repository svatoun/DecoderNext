/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.model;

import com.jgoodies.binding.beans.BeanUtils;
import com.jgoodies.binding.beans.PropertyNotBindableException;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.BindingConverter;
import com.jgoodies.binding.value.ValueModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import one.dedic.jmri.decodernext.model.formx.model.BufferedModel;

/**
 * A ValueModel that uses {@link BindingConverter} to convert values. It forwards
 * Lookup to the underlying model. Also implements {@link BufferedModel}, so it can
 * indicate {@link BufferedModel#PROP_DIRTY} state after unsuccessful conversion from the
 * source.
 * 
 * @author sdedic
 */
public class ConvertingBufferedModel extends AbstractValueModel implements BufferedModel {

    /**
     * Holds the ValueModel that in turn holds the source value.
     */
    private final ValueModel source;

    /**
     * Converts values from the source to the target and vice versa.
     */
    private final BindingConverter converter;
    
    private PropertyChangeListener valueChangeHandler;
    
    private volatile boolean conversionDirty;

    // Instance creation ******************************************************

    /**
     * Constructs a ConverterValueModel on the given source ValueModel and
     * BindingConverter.
     *
     * @param source  the ValueModel that holds the source value
     * @param converter   converts source values to target values and vice versa
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public ConvertingBufferedModel(ValueModel source, BindingConverter converter) {
        this.source = source;
        this.converter = converter;
    }

    @Override
    public ValueModel getDelegate() {
        return source;
    }
    
    @Override
    protected PropertyChangeSupport createPropertyChangeSupport(Object bean) {
        PropertyChangeSupport support = super.createPropertyChangeSupport(bean);
        synchronized (this) {
            if (valueChangeHandler == null) {
                valueChangeHandler = createValueChangeHandler();
                source.addValueChangeListener(valueChangeHandler);
                try {
                    BeanUtils.addPropertyChangeListener(source, PROP_DIRTY, valueChangeHandler);
                } catch (PropertyNotBindableException ex) {
                    BeanUtils.addPropertyChangeListener(source, valueChangeHandler);
                }
            }
        }
        return support;
    }

    // Abstract Behavior ******************************************************

    /**
     * Converts a value from the subject to the type or format used
     * by this converter.
     *
     * @param sourceValue  the source's value
     * @return the converted value in the type or format used by this converter
     */
    public Object convertFromSubject(Object sourceValue) {
    	return converter.targetValue(sourceValue);
    }


    @Override
    public void setValue(Object targetValue) {
        source.setValue(converter.sourceValue(targetValue));
    }


    // ValueModel Implementation **********************************************

    /**
     * Converts the subject's value and returns the converted value.
     *
     * @return the converted subject value
     */
    @Override
    public Object getValue() {
        try {
            return convertFromSubject(source.getValue());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            handleRuntimeException(ex);
        }
        return null;
    }
    
    protected void handleRuntimeException(RuntimeException ex) {
        boolean od;
        synchronized (this) {
            od = conversionDirty;
            conversionDirty = true;
        }
        // superclass will check for equality
        firePropertyChange(PROP_DIRTY, od, true);
        throw ex;
    }

    @Override
    public boolean isDirty() {
        if (source instanceof BufferedModel) {
            if (((BufferedModel)source).isDirty()) {
                return true;
            }
        }
        return conversionDirty;
    }

    @Override
    public Object getPendingValue() {
        if (source instanceof BufferedModel) {
            try {
                return convertFromSubject(((BufferedModel)source).getPendingValue());
            } catch (IllegalArgumentException | IllegalStateException ex) {
                handleRuntimeException(ex);
                throw ex;
            }
        } else {
            return getValue();
        }
    }

    @Override
    public CompletableFuture<Object> getTargetValue() {
        if (source instanceof BufferedModel) {
            CompletableFuture<Object> vf = ((BufferedModel)source).getTargetValue();
            return vf.thenApply(this::convertFromSubject).handle((v, ex) -> {
                if (ex != null) {
                    if (ex instanceof RuntimeException) {
                        handleRuntimeException((RuntimeException)ex);
                    } 
                    throw new CompletionException(ex);
                } else {
                    return v;
                }
            });
        } else {
            return BufferedModel.getTargetValue(source);
        }
    }
    
    
    
    // AbstractWrappedValueModel Behavior *************************************

    protected PropertyChangeListener createValueChangeHandler() {
        return new ValueChangeHandler();
    }


    // Helper Class ***********************************************************

    /**
     * Listens to value changes in the wrapped model, converts the old and
     * new value - if any - and fires a value change for this converter.
     */
    private final class ValueChangeHandler implements PropertyChangeListener {
        private PropertyChangeEvent lastEvent;
        
        /**
         * Notifies listeners about a change in the underlying subject.
         * The old and new value used in the PropertyChangeEvent to be fired
         * are converted versions of the observed old and new values.
         * The observed old and new value are converted only
         * if they are non-null. This is because {@code null}
         * may be a valid value or may indicate <em>not available</em>.<p>
         *
         * TODO: We may need to check the identity, not equity.
         *
         * @param evt   the property change event to be handled
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object convertedOldValue = null;
            Object convertedNewValue = null;
            boolean newConversionDirty = false;
            
            if (evt == lastEvent) {
                return;
            }
            String s = evt.getPropertyName();
            if (PROPERTY_VALUE.equals(s)) {
                if (evt.getOldValue() == null) {
                    convertedOldValue = null;
                } else {
                    try {
                        convertedOldValue = convertFromSubject(evt.getOldValue());  
                    } catch (IllegalStateException | IllegalArgumentException ex) {
                        convertedOldValue = null;
                    }
                }

                if (evt.getNewValue() == null) {
                    convertedNewValue = null;
                } else {
                    try {
                        convertedNewValue = convertFromSubject(evt.getNewValue());
                    } catch (IllegalStateException | IllegalArgumentException ex) {
                        convertedNewValue = null;
                        newConversionDirty = true;
                    }
                }
            } else if (s == null) {
                try {
                    convertedNewValue = convertFromSubject(source.getValue());
                } catch (IllegalStateException | IllegalArgumentException ex) {
                    convertedNewValue = null;
                    newConversionDirty = true;
                }
            } else {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                lastEvent = evt;
                return;
            }
            
            boolean oc;
            synchronized (this) {
                oc = conversionDirty;
                conversionDirty = newConversionDirty;
            }
            firePropertyChange(PROP_DIRTY, oc, newConversionDirty);
            fireValueChange(convertedOldValue, convertedNewValue);
            lastEvent = evt;
        }
    }
}
