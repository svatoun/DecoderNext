/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.model;


import com.jgoodies.binding.beans.BeanUtils;
import com.jgoodies.binding.beans.PropertyNotBindableException;
import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CompletableFuture;

import javax.swing.Timer;
import org.openide.util.Lookup;

/**
 * Delayed input model: delays change events, that might cause revalidation or
 * dependent value changes. The model fires events with a delay, but the value
 * is immediately visible from {@link #getPendingValue}. After the timeout, the cached
 * value is discarded, and {@link #getValue} starts to provide the fresh value.
 * Note that if the value is invalid, fails conversion, or mandatory validation, {@link #getValue} 
 * or {@link #getPendingValue()} can throw an exception.
 * <p>
 * The model fires <b>immediately</b> {@link BufferedModel#PROP_DIRTY} property change. The
 * dirty status is then cleared when the {@link #PROPERTY_VALUE} change arrives. Setting
 * the value externally will immediately clear dirty status.
 * <p>
 * 
 * @author sdedic
 */
public class DelayedInputModel extends AbstractValueModel implements BufferedModel, DelegateModel {
    private static final Object NONE = new Object();
    /**
     * Refers to the underlying subject ValueModel.
     */
    private final ValueModel subject;

    /**
     * The Timer used to perform the delayed commit.
     */
    private final Timer timer;

    /**
     * If {@code true} all pending updates will be coalesced.
     * In other words, an update will be fired if no updates
     * have been received for this model's delay.
     */
    private boolean coalesce;

    /**
     * Holds the most recent pending PropertyChangeEvent as provided
     * from the subject change notification that this model defers.
     * {@code #fireDelayedValueChange}.
     */
    private PropertyChangeEvent pendingEvt;
    
    /**
     * Input's dirty status
     */
    private boolean dirty;
    
    /**
     * Last set value, to suppress an extra dirty + value change.
     */
    private Object lastSet;

    private Object cachedGetValue = NONE;

    // Instance Creation ******************************************************

    /**
     * Constructs a DelayedReadValueModel for the given subject ValueModel
     * and the specified Timer delay in milliseconds with coalescing disabled.
     *
     * @param subject   the underlying (or wrapped) ValueModel
     * @param delay     the milliseconds to wait before a change
     *     shall be committed
     *
     * @throws IllegalArgumentException if the delay is negative
     */
    DelayedInputModel(ValueModel subject, int delay) {
        this(subject, delay, false);
    }


    /**
     * Constructs a DelayedReadValueModel for the given subject ValueModel
     * and the specified Timer delay in milliseconds using the given
     * coalesce mode.
     *
     * @param subject   the underlying (or wrapped) ValueModel
     * @param delay     the milliseconds to wait before a change
     *     shall be committed
     * @param coalesce  {@code true} to coalesce all pending changes,
     *     {@code false} to fire changes with the delay when an update
     *     has been received
     *
     * @throws IllegalArgumentException if the delay is negative
     *
     * @see #setCoalesce(boolean)
     */
    DelayedInputModel(ValueModel subject, int delay, boolean coalesce) {
        this.subject = subject;
        this.coalesce = coalesce;
        this.timer = new Timer(delay, new ValueUpdateListener());
        timer.setRepeats(false);
        
        PropertyChangeListener l = new SubjectValueChangeHandler();
        if (subject instanceof BufferedModel) {
            ((BufferedModel)subject).addPropertyChangeListener(l);
            subject.addValueChangeListener(l);
        } else {
            try {
                BeanUtils.addPropertyChangeListener(subject, PROP_DIRTY, l);
                subject.addValueChangeListener(l);
            } catch (PropertyNotBindableException ex) {
                BeanUtils.addPropertyChangeListener(subject, l);
            }
        }
    }

    @Override
    public ValueModel getDelegate() {
        return subject;
    }

    public static DelayedInputModel create(ValueModel subject, int delay) {
        return create(subject, delay, false);
    }
    
    public static DelayedInputModel create(ValueModel subject, int delay, boolean coalesce) {
        if (subject instanceof Lookup) {
            return new WithLookup(subject, delay, coalesce, () -> (Lookup)subject);
        } else if (subject instanceof Lookup.Provider) {
            return new WithLookup(subject, delay, coalesce, (Lookup.Provider)subject);
        } else {
            return new DelayedInputModel(subject, delay, coalesce);
        }
    }
    
    private static final class WithLookup extends DelayedInputModel implements Lookup.Provider {
        private final Lookup.Provider lp;
        
        public WithLookup(ValueModel subject, int delay, boolean coalesce, Lookup.Provider lp) {
            super(subject, delay, coalesce);
            this.lp = lp;
        }

        @Override
        public Lookup getLookup() {
            return lp.getLookup();
        }
    }

    // ValueModel Implementation ******************************************

    /**
     * Returns the subject's value or in case of a pending commit,
     * the pending new value.
     *
     * @return the subject's current or future value.
     */
    @Override
    public Object getValue() {
        synchronized (this) {
            if (cachedGetValue != NONE) {
                return cachedGetValue;
            }
        }
        Object r = subject.getValue();
        synchronized (this) {
            cachedGetValue = r;
        }
        return r;
    }


    /**
     * Sets the given new value immediately as the subject's new value.
     * Unlike changes from the subject, this call fires all changes immediately.
     *
     * @param newValue   the value to set
     */
    @Override
    public void setValue(Object newValue) {
        Object ov = null;
        boolean wasDirty = false;
        try {
            ov = getValue();
        } catch (IllegalStateException ex) {
            wasDirty = true;
        }
        synchronized (this) {
            lastSet = newValue;
            cachedGetValue = NONE;
        }
        subject.setValue(newValue);
        if (wasDirty) {
            dirty = false;
            firePropertyChange(PROP_DIRTY, wasDirty, isDirty());
        }
        fireValueChange(ov, newValue);
    }

    // Accessors **************************************************************

    /**
     * Returns the delay, in milliseconds, that is used to defer value change
     * notifications.
     *
     * @return the delay, in milliseconds, that is used to defer
     *     value change notifications
     *
     * @see #setDelay
     */
    public int getDelay() {
        return timer.getDelay();
    }

    /**
     * Sets the delay, in milliseconds, that is used to defer value change
     * notifications.
     *
     * @param delay   the delay, in milliseconds, that is used to defer
     *     value change notifications
     * @see #getDelay
     */
    public void setDelay(int delay) {
        timer.setInitialDelay(delay);
        timer.setDelay(delay);
    }


    /**
     * Returns if this model coalesces all pending changes or not.
     *
     * @return {@code true} if all pending changes will be coalesced,
     *     {@code false} if pending changes are fired with a delay
     *     when an update has been received.
     *
     * @see #setCoalesce(boolean)
     */
    public boolean isCoalesce() {
        return coalesce;
    }

    /**
     * Sets if this model shall coalesce all pending changes or not.
     * In this case, a change event will be fired first,
     * if no updates have been received for this model's delay.
     * If coalesce is {@code false}, a change event will be fired
     * with this model's delay when an update has been received.<p>
     *
     * The default value is {@code false}.<p>
     *
     * Note that this value is not the #coalesce value
     * of this model's internal Swing timer.
     *
     * @param b {@code true} to coalesce,
     *     {@code false} to fire separate changes
     */
    public void setCoalesce(boolean b) {
        coalesce = b;
    }
    
    CompletableFuture<Object> pendingValue = null;


    // Misc *******************************************************************

    /**
     * Stops a running timer. Pending changes - if any - are canceled
     * and won't be performed by the {@code ValueUpdateListener}.
     *
     * @since 1.2
     */
    public void stop() {
        timer.stop();
    }


    /**
     * Checks and answers whether this model has one or more pending changes.
     *
     * @return {@code true} if there are pending changes, {@code false} if not.
     *
     * @since 2.0.4
     */
    public boolean isPending() {
        CompletableFuture<Object> cf = this.pendingValue;
        return timer.isRunning() || (cf != null && !cf.isDone());
    }

    @Override
    public Object getPendingValue() {
        return subject instanceof BufferedModel ?
            ((BufferedModel)subject).getPendingValue() : subject.getValue();
    }

    @Override
    public boolean isDirty() {
        if (isPending()) {
            return true;
        } else {
            return subject instanceof BufferedModel ?
                ((BufferedModel)subject).isDirty() : dirty;
        }
    }

    protected void modelValueChanged(PropertyChangeEvent evt) {
        // the == is deliberate here.
        synchronized (this) {
            if (lastSet == evt.getNewValue()) {
                lastSet = NONE;
                return;
            }
        }
        fireDelayedValueChange(evt);
    }

    /**
     * Sets the given new value after this model's delay.
     * Does nothing if the new value and the latest pending value are the same.
     *
     * @param evt  the PropertyChangeEvent to be fired after this model's delay
     */
    protected void fireDelayedValueChange(PropertyChangeEvent evt) {
        boolean wasDirty = isDirty();
        boolean fireImmediately;
        
        synchronized (this) {
            fireImmediately = evt.getNewValue() == lastSet;
            
            CompletableFuture<Object> cf = new CompletableFuture<Object>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    if (!super.cancel(mayInterruptIfRunning)) {
                        return false;
                    }
                    timer.stop();
                    return true;
                }
            };
            CompletableFuture<Object> p = this.pendingValue;
            if (p != null && !p.isDone()) {
                cf.handle((v, ex) -> {
                    if (ex != null) {
                        this.pendingValue.completeExceptionally(ex);
                        return null;
                    } else {
                        return v;
                    }
                });
            }
            pendingValue = cf;
            pendingEvt = evt;
            if (coalesce) {
                timer.restart();
            } else {
                timer.start();
            }
        }
        if (fireImmediately) {
            fireValueChange(evt.getOldValue(), evt.getNewValue(), false);
            firePropertyChange(PROP_DIRTY, wasDirty, false);
            return;
        }
        firePropertyChange(PROP_DIRTY, wasDirty, true);
    }

    @Override
    public CompletableFuture<Object> getTargetValue(boolean allowSpecial) {
        synchronized (this) {
            if (this.pendingValue != null) {
                return this.pendingValue;
            }
        }
        return ModelUtilities.getTargetValue(subject, allowSpecial);
    }
    
    


    // Event Handling *********************************************************

    /**
     * Describes the delayed action to be performed by the timer.
     */
    private final class ValueUpdateListener implements ActionListener {

        /**
         * An ActionEvent has been fired by the Timer after its delay.
         * Fires the pending PropertyChangeEvent, stops the timer,
         * and updates this model's oldValue.<p>
         *
         * TODO: Consider stopping the timer before firing the change,
         * because the change handling may take some time.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            Object nv;
            boolean d;
            Throwable ex;
            try {
                nv = subject.getValue();
                d = false;
                ex = null;
            } catch (IllegalStateException ex2) {
                d = true;
                nv = null;
                ex = ex2;
            }
            CompletableFuture<Object> p;
            synchronized (DelayedInputModel.this) {
                dirty = d;
                p = pendingValue;
            }
            stop();
            if (p != null) {
                if (ex != null) {
                    p.completeExceptionally(ex);
                } else {
                    p.complete(nv);
                }
            }
            cachedGetValue = NONE;
            fireValueChange(pendingEvt.getOldValue(), nv, false);
            firePropertyChange(PROP_DIRTY, true, isDirty());
        }
    }


    /**
     * Forwards value changes in the subject to listeners of this model.
     */
    private final class SubjectValueChangeHandler implements PropertyChangeListener {
        private PropertyChangeEvent last;
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt == last) {
                return;
            }
            if (ValueModel.PROPERTY_VALUE.equals(evt.getPropertyName())) {
                modelValueChanged(evt);
            } else {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
            last = evt;
        }
    }
}
