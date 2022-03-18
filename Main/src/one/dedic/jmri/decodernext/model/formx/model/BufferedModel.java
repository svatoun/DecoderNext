/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.model;

import java.beans.PropertyChangeListener;
import com.jgoodies.binding.value.ValueModel;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a model whose changes are fired with a delay. This allows to coalesce
 * rapid user UI gestures, but also allows to inspect immediate values to do before-commit
 * validation.
 * <p>
 * The model can fire {@link #PROP_DIRTY} property change to indicate the value is being
 * changed, but is not ready yet. A {@link ValueModel#PROPERTY_VALUE} is fired when
 * the value finally changes. A A {@link ValueModel#PROPERTY_VALUE} can be fired with
 * {@code null} new value to indicate the value became unreadable.
 * Unreadable values cause {@link ValueModel#getValue} to fail.
 * <p>
 * The sequence of events with BufferedModel is:
 * <ol>
 * <li>the UI starts to change
 * <li>{@link #PROP_DIRTY} fires
 * <li>the UI completes the change
 * <li>{@link PROPERTY_VALUE} fires
 * </ol>
 * 
 * @author sdedic
 */
public interface BufferedModel extends ValueModel, DelegateModel {
    public ValueModel getDelegate();
    /**
     * Represents 'dirty' state. When the model is 'dirty', the value is being
     * changed, but may not be ready yet.
     */
    public static final String PROP_DIRTY = "dirty"; // NOI18N
    
    public default void addPropertyChangeListener(String propName, PropertyChangeListener l) {
        addPropertyChangeListener(l);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    public default void removePropertyChangeListener(String propName, PropertyChangeListener l) {
        removePropertyChangeListener(l);
    }
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    /**
     * The model is dirty, if it has not been written to underlying
     * data model. For example, if an integer edit box has been changed,
     * but contains input not representable as a number.
     * 
     * @return true, if the model is dirty 
     */
    public default boolean isDirty() {
        return false;
    }
    
    /**
     * Returns the pending value. If a value is being changed, returns the
     * pending value. If the value is settled already in the data model, returns
     * that value.
     * <p>
     * Note that pending value may be invalid, so the method may throw
     * an exception.
     * @return pending or settled value.
     */
    public default Object getPendingValue() {
        return getValue();
    }
    
    /**
     * Returns the model's final value. If the value is being changed (the model is dirty),
     * it should return a Future which becomes completed after the model is finally updated. 
     * However the future can complete exceptionally, if the value is invalid / not representable.
     * <p>
     * If the model is not dirty, the implementation should return a completed future.
     * 
     * @return future which becomes completed after the model is updated from the buffer.
     */
    public default CompletableFuture<Object> getTargetValue() {
        return CompletableFuture.completedFuture(getValue());
    }
    
    /**
     * Helper method to get target value from a {@link ValueModel}.
     * @param del
     * @return 
     */
    public static CompletableFuture<Object> getTargetValue(ValueModel del) {
        if (del instanceof BufferedModel) {
            return ((BufferedModel)del).getTargetValue();
        } else {
            CompletableFuture<Object> res = new CompletableFuture<>();
            try {
                res.complete(del.getValue());
            } catch (IllegalArgumentException | IllegalStateException ex) {
                res.completeExceptionally(ex);
            }
            return res;
        }
    }
}
