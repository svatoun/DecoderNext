/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.model;

import com.jgoodies.binding.beans.BeanUtils;
import com.jgoodies.binding.beans.PropertyNotBindableException;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.BindingConverter;
import com.jgoodies.binding.value.ValueModel;
import static com.jgoodies.binding.value.ValueModel.PROPERTY_VALUE;
import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.message.SimpleValidationMessage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import one.dedic.jmri.decodernext.forms.api.validation.ValidationException;
import static one.dedic.jmri.decodernext.model.formx.model.BufferedModel.PROP_DIRTY;

/**
 *
 * @author sdedic
 */
public class ConvertingModel extends AbstractValueModel implements DelegateModel, TransformModel {

    /**
     * Holds the ValueModel that in turn holds the source value.
     */
    private final ValueModel source;

    /**
     * Converts values from the source to the target and vice versa.
     */
    private final BindingConverter converter;
    
    private ErrorMessageProducer messageProducer;
    
    private String messageKey;
    
    private PropertyChangeListener valueChangeHandler;
    
    private volatile ValidationException conversionError;
    
    // Instance creation ******************************************************

    /**
     * Constructs a ConverterValueModel on the given source ValueModel and
     * BindingConverter.
     *
     * @param source  the ValueModel that holds the source value
     * @param converter   converts source values to target values and vice versa
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public ConvertingModel(ValueModel source, BindingConverter converter) {
        this.source = source;
        this.converter = converter;
    }

    public ErrorMessageProducer getMessageProducer() {
        return messageProducer;
    }

    public ConvertingModel setMessageProducer(ErrorMessageProducer messageProducer) {
        this.messageProducer = messageProducer;
        return this;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public ConvertingModel setMessageKey(String messageKey) {
        this.messageKey = messageKey;
        return this;
    }

    @Override
    public ValueModel getDelegate() {
        return source;
    }

    @Override
    public Object transform(Object v) {
        return convertFromSubject(v);
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
            od = conversionError != null;
            conversionError = createError(ex);
        }
        if (od) {
            throw ex;
        }
        // ... and fire a value change, so clients know to get the updated (erroneous) value
        firePropertyChange(ValueModel.PROPERTY_VALUE, null, null);
        // superclass will check for equality
        firePropertyChange(PROP_DIRTY, od, true);
        throw ex;
    }

    @Override
    public boolean isDirty() {
        if (conversionError != null) {
            return true;
        }
        return ModelUtilities.applyDelegates2(source, DelegateModel.class, DelegateModel::isDirty, false);
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
            Exception err = null;
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
                        err = ex;
                    }
                }
            } else if (s == null) {
                try {
                    convertedNewValue = convertFromSubject(source.getValue());
                } catch (IllegalStateException | IllegalArgumentException ex) {
                    err = ex;
                    newConversionDirty = true;
                }
            } else {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                lastEvent = evt;
                return;
            }
            
            if (err != null) {
                convertedNewValue = null;
                newConversionDirty = true;
            }
            
            boolean oc;
            synchronized (this) {
                oc = conversionError != null;
                conversionError = createError(err);
            }
            firePropertyChange(PROP_DIRTY, oc, newConversionDirty);
            fireValueChange(convertedOldValue, convertedNewValue);
            lastEvent = evt;
        }
    }
    
    private ValidationException createError(Exception orig) {
        if (orig == null) {
            return null;
        }
        if (orig instanceof ValidationException) {
            return (ValidationException)orig;
        }
        Throwable t = orig.getCause();
        if (t instanceof ValidationException) {
            return (ValidationException)t;
        }
        EntryDescriptor desc = ModelUtilities.getModelDecription(source);
        String s = messageProducer == null ? orig.getMessage() : messageProducer.createMessage(orig.getMessage(), orig, desc);
        ValidationMessage msg = new SimpleValidationMessage(s, Severity.ERROR, 
            desc != null ? desc.getName() : messageKey);
        ValidationResult r = new ValidationResult().add(msg);
        ValidationException ex = new ValidationException(r, orig);
        return ex;
    }
}
