/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

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
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class DefaultValidatorService extends FocusAdapter implements ChangeListener, ActionListener,
        ValidatorService, ContextValidator {

    private final Validator delegate;
    private final JComponent target;
    private final List<ChangeListener> listeners = new ArrayList<>();
    private JLabel indicator;
    private ValidationResult result = new ValidationResult();
    private Lookup context;

    public DefaultValidatorService(Validator delegate, JComponent target) {
        this.delegate = delegate;
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
    public JLabel getIndicator() {
        return indicator;
    }

    public DefaultValidatorService setIndicator(JLabel indicator) {
        this.indicator = indicator;
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
        if (target instanceof ContextValidator) {
            ((ContextValidator) target).attach(context);
        }
        return delegate.validate(target);
    }

    protected void fireChange() {
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

    public static DefaultValidatorService attachValidatorService(JComponent c, JLabel indicator, Validator worker) {
        return attachValidatorService0(c, indicator, worker, Lookup.EMPTY, 0);
    }

    public static DefaultValidatorService attachValidatorService(JComponent c, JLabel indicator, Validator worker, Lookup context) {
        return attachValidatorService0(c, indicator, worker, context, 0);
    }
    
    private static DefaultValidatorService attachValidatorService0(JComponent c, JLabel indicator, Validator worker, Lookup context, int level) {
        assert c != null;
        DefaultValidatorService service = null;
        if (c instanceof JTextField) {
            service = new DefaultValidatorService(worker, c);
            JTextField tf = (JTextField) c;
            c.addFocusListener(service);
        }
        if (c instanceof JComboBox) {
            JComboBox cb = (JComboBox) c;
            if (cb.isEditable()) {
                Component e = cb.getEditor().getEditorComponent();
                if (e instanceof JComponent) {
                    service = attachValidatorService0((JComponent) e, indicator, worker, context, level + 1);
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
