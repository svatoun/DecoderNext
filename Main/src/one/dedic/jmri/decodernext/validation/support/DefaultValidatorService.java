/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.support;

import com.jgoodies.validation.Validatable;
import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import one.dedic.jmri.decodernext.validation.ValidationConstants;
import one.dedic.jmri.decodernext.validation.ValidatorService;
import org.openide.util.Lookup;

/**
 * Implements a ValidationService for common controls, delegating to a 
 * {@link Validator} instance. The implementation monitors UI changes of the
 * control(s), reports changes in an abstract way (fires {@link ChangeEvent}).
 * When asked for validation, it delegates to the underlying {@link Validator}, but
 * caches the result for a potential {@link #getValidation()} query.
 * 
 * @author sdedic
 */
public class DefaultValidatorService extends FocusAdapter implements ChangeListener, 
        ActionListener, DocumentListener,
        ValidatorService, ContextValidator {

    private final Validator delegate;
    private final Validatable delegate2;
    private final JComponent target;
    private final List<ChangeListener> listeners = new ArrayList<>();
    private ValidationResult result = new ValidationResult();
    private Lookup context;
    private Timer  keyTypedTimer = new Timer(300, this::timerExpired);

    public DefaultValidatorService(Validator delegate, JComponent target) {
        this.delegate = delegate;
        this.delegate2 = null;
        this.target = target;
    }

    public DefaultValidatorService(Validatable delegate, JComponent target) {
        this.delegate2 = delegate;
        this.delegate = null;
        this.target = target;
    }

    public JComponent getTarget() {
        return target;
    }
    
    public Lookup getContext() {
        return context;
    }

    public DefaultValidatorService setContext(Lookup context) {
        this.context = context;
        return this;
    }

    @Override
    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public void attach(Lookup context) {
        this.context = context;
    }

    @Override
    public ValidationResult validate() {
        if (delegate instanceof ContextValidator) {
            ((ContextValidator) delegate).attach(context);
        }
        if (delegate2 instanceof ContextValidator) {
            ((ContextValidator) delegate).attach(context);
        }
        if (delegate != null) {
            return delegate.validate(target);
        } else if (delegate2 != null) {
            return delegate2.validate();
        }
        return ValidationResult.EMPTY;
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
        keyTypedTimer.stop();
        ChangeListener[] ll;
        synchronized (this) {
            if (listeners.isEmpty()) {
                return;
            }
            ll = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : ll) {
            l.stateChanged(e);
        }
    }
    
    private void timerExpired(ActionEvent e) {
        fireChange();
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
    public void focusLost(FocusEvent e) {
        fireChange();
    }

    @Override
    public ValidationResult getValidation() {
        return result;
    }
    
    private void scheduleEvent() {
        keyTypedTimer.restart();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        scheduleEvent();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        scheduleEvent();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public Component findComponent(Object key) {
        if (key == null) {
            return null;
        }
        if (key == target) {
            return target;
        }
        if (key.equals(target.getClientProperty(ValidationConstants.COMPONENT_VALIDATION_KEY))) {
            return target;
        }
        for (ValidationMessage msg : result) {
            if (key.equals(msg.key())) {
                return target;
            }
        }
        return null;
    }

    public static DefaultValidatorService attachValidatorService(JComponent c, Validator worker) {
        return attachValidatorService0(c, worker, Lookup.EMPTY, 0);
    }

    public static DefaultValidatorService attachValidatorService(JComponent c, Validatable worker) {
        return attachValidatorService0(c, worker, Lookup.EMPTY, 0);
    }

    public static DefaultValidatorService attachValidatorService(JComponent c, Validator worker, Lookup context) {
        return attachValidatorService0(c, worker, context, 0);
    }
    
    private static DefaultValidatorService attachValidatorService0(JComponent c, Validator worker, Lookup context, int level) {
        assert c != null;
        DefaultValidatorService service = null;
        if (c instanceof JTextField) {
            service = new DefaultValidatorService(worker, c);
        }
        if (c instanceof JComboBox) {
            JComboBox cb = (JComboBox) c;
            if (cb.isEditable()) {
                Component e = cb.getEditor().getEditorComponent();
                if (e instanceof JComponent) {
                    service = attachValidatorService0((JComponent) e, worker, context, level + 1);
                }
            }
        }
        if (level == 0) {
            if (service != null) {
                service.attach(context);
            }
            c.putClientProperty(ValidationConstants.COMPONENT_VALIDATION_KEY, service);
        }
        return service;
    }

    private static DefaultValidatorService attachValidatorService0(JComponent c, Validatable worker, Lookup context, int level) {
        assert c != null;
        DefaultValidatorService service = null;
        if (c instanceof JTextField) {
            service = new DefaultValidatorService(worker, c);
        }
        if (c instanceof JComboBox) {
            JComboBox cb = (JComboBox) c;
            if (cb.isEditable()) {
                Component e = cb.getEditor().getEditorComponent();
                if (e instanceof JComponent) {
                    service = attachValidatorService0((JComponent) e, worker, context, level + 1);
                }
            }
        }
        if (level == 0) {
            if (service != null) {
                service.attach(context);
            }
            c.putClientProperty(ValidationConstants.COMPONENT_VALIDATION_KEY, service);
        }
        return service;
    }
}
