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
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class PanelValidationSupport implements ValidatorService, ContextValidator {
    public static final Predicate<Component> ALL = new Predicate<Component>() {
        @Override
        public boolean test(Component t) {
            return true;
        }
    };

    private final JComponent    root;
    private final List<ChangeListener> listeners = new ArrayList<>();
    private final Map<Object, Entry> entries = new HashMap<>();
    private final ChangeListener delegeateChangeL = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            makeChanged();
        }
    };

    private boolean changed;
    private Lookup context;
    private ValidationResult    result;
    private Predicate<Component>  acceptor = new Predicate<Component>() {
        @Override
        public boolean test(Component t) {
            return true;
        }
    };

    public PanelValidationSupport(JComponent root) {
        this.root = root;
    }
    
    public ValidatorService build() {
        scanComponents(root);
        return this;
    }

    @Override
    public ValidationResult getValidation() {
        return result;
    }

    @Override
    public Component findComponent(Object key) {
        Entry e = entries.get(key);
        if (e != null) {
            return e.component;
        }
        for (Entry e2 : entries.values()) {
            if (e2.test(key)) {
                return e2.component;
            }
        }
        return null;
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
    public JLabel getIndicator() {
        return null;
    }
    
    private void makeChanged() {
        synchronized (this) {
            if (changed) {
                return;
            }
            changed = true;
        }
        SwingUtilities.invokeLater(this::fireChange);
    }
    
    private void fireChange() {
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
    public ValidationResult validate() {
        assert SwingUtilities.isEventDispatchThread();
        ValidationResult newResult = new ValidationResult();
        for (Entry e : entries.values()) {
            ValidationResult partial = e.validator.validate();
            e.partialResult = partial;
            newResult.addAllFrom(partial);
        }
        synchronized (this) {
            result = newResult;
            changed = false;
        }
        return newResult;
    }
    
    private static class Entry {
        final Component             component;
        final ValidatorService      validator;
        Object  key;
        JLabel  iconPlaceholder;
        Component             validationTarget;
        ValidationResult      partialResult;

        public Entry(Component component, ValidatorService validator) {
            this.component = component;
            this.validator = validator;
        }

        public Entry setValidationTarget(Component validationTarget) {
            this.validationTarget = validationTarget;
            return this;
        }
        
        public boolean test(Object key) {
            if (Objects.equals(this.key, key)) {
                return true;
            }
            for (ValidationMessage msg : partialResult) {
                if (Objects.equals(msg.key(), key)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public ValidatorService createDefaultService(JComponent c, Validator worker, AtomicReference<Component> target) {
        DefaultValidatorService service = DefaultValidatorService.attachValidatorService(c, null, worker);
        target.set(service.getTarget());
        return service;
    }
    
    private void processValidatorService(JComponent jc, Object o) {
        ValidatorService srv = null;
        AtomicReference<Component> r = new AtomicReference<>();
        if (o instanceof ValidatorService) {
            srv = (ValidatorService)o;
        } else if (o instanceof Validator) {
            srv = createDefaultService(jc, (Validator)o, r);
        } 
        if (srv == null) {
            return;
        }
        Object key = jc.getClientProperty(ValidationConstants.COMPONENT_VALIDATION_KEY);
        if (key == null) {
            key = jc;
        }
        Entry e = new Entry(jc, srv).setValidationTarget(r.get());
        entries.put(key, e);
        srv.addChangeListener(delegeateChangeL);
    }
    
    private void processIconHolder(JLabel l) {
        Component target = l.getLabelFor();
        if (target == null || !(target instanceof JComponent)) {
            return;
        }
        JComponent jc = (JComponent)target;
        Entry e = findEntry(jc);
        if (e == null) {
            return;
        }
        e.iconPlaceholder = l;
    }
    
    private Entry findEntry(JComponent jc) {
        for (Entry e : entries.values()) {
            if (e.component == jc) 
                return e;
        }
        return null;
    }
    
    private void scanComponents(Container parent) {
        for (Component c : parent.getComponents()) {
            if (!(c instanceof JComponent) && acceptor.test(c)) {
                continue;
            }
            JComponent jc = (JComponent)c;
            Object o = jc.getClientProperty(ValidationConstants.COMPONENT_VALIDATOR);
            if (o != null) {
                processValidatorService(jc, o);
                continue;
            }
            if (o instanceof Container) {
                scanComponents((Container)o);
            }
        }
        for (Component c : parent.getComponents()) {
            if (!(c instanceof JComponent) && acceptor.test(c)) {
                continue;
            }
            JComponent jc = (JComponent)c;
            Object o = jc.getClientProperty(ValidationConstants.COMPONENT_VALIDATION_ICON);
            if (o != null && jc instanceof JLabel) {
                processIconHolder((JLabel)jc);
            }
            if (o instanceof Container) {
                scanComponents((Container)o);
            }
        }
    }
}
