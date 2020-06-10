/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.support;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import one.dedic.jmri.decodernext.validation.SwingAttached;
import one.dedic.jmri.decodernext.validation.ValidationConstants;
import one.dedic.jmri.decodernext.validation.data.ControlChangeEvent;
import one.dedic.jmri.decodernext.validation.data.ControlChangeListener;
import one.dedic.jmri.decodernext.validation.data.ControlChangeMonitor;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class DefaultControlChangeMonitor extends FocusAdapter implements ControlChangeMonitor,
        ChangeListener, ActionListener, DocumentListener, SwingAttached {
    
    private final JComponent target;
    private final JComponent component;
    private boolean dirty;
    
    private List<ControlChangeListener> listeners;

    private DefaultControlChangeMonitor(JComponent target, JComponent component) {
        this.target = target;
        this.component = component;
    }
    
    
    public static ControlChangeMonitor attachChangeMonitor(JComponent c, Lookup context) {
        DefaultControlChangeMonitor service = null;
        if (c instanceof JTextField) {
            service = new DefaultControlChangeMonitor(c, c);
        }
        if (c instanceof JComboBox) {
            JComboBox cb = (JComboBox) c;
            if (cb.isEditable()) {
                Component e = cb.getEditor().getEditorComponent();
                if (e instanceof JComponent) {
                    service = new DefaultControlChangeMonitor((JComponent)e, c);
                }
            }
        }
        c.putClientProperty(ValidationConstants.CONTROL_CHANGE_SUPPORT, service);
        return service;
    }

    @Override
    public void addControlChangeListener(ControlChangeListener l) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(l);
    }

    @Override
    public void removeControlChangeListener(ControlChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public void resetChangeState() {
        dirty = false;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fireChange();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        fireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }
    
    @Override
    public void addNotify() {
        if (target instanceof JTextField) {
            JTextField tf = (JTextField)target;
            tf.addFocusListener(this);
            tf.getDocument().addDocumentListener(this);
        }
    }
    
    @Override
    public void removeNotify() {
        if (target instanceof JTextField) {
            JTextField tf = (JTextField)target;
            tf.removeFocusListener(this);
            tf.getDocument().removeDocumentListener(this);
        }
    }

    protected void fireChange() {
        ControlChangeListener[] ll;
        dirty = true;
        synchronized (this) {
            if (listeners.isEmpty()) {
                return;
            }
            ll = listeners.toArray(new ControlChangeListener[listeners.size()]);
        }
        ControlChangeEvent e = new ControlChangeEvent(this, component);
        for (ControlChangeListener l : ll) {
            l.controlChanged(e);
        }
    }
    
}
