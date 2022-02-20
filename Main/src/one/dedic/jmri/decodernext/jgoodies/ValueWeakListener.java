/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.jgoodies;

import com.jgoodies.binding.value.ValueModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import org.openide.util.BaseUtilities;

/**
 *
 * @author sdedic
 */
public final class ValueWeakListener implements PropertyChangeListener, Runnable {
    private final ValueModel eventSource;
    private final WeakReference<PropertyChangeListener> delegate;

    public ValueWeakListener(ValueModel eventSource, PropertyChangeListener l) {
        this.eventSource = eventSource;
        this.delegate = new WeakReference<>(l, BaseUtilities.activeReferenceQueue());
    }

    @Override
    public void run() {
        delegate.clear();
        unregister();
    }
    
    private void unregister() {
        eventSource.removeValueChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        PropertyChangeListener l = delegate.get();
        if (l == null) {
            unregister();
        } else {
            l.propertyChange(evt);
        }
    }
}
