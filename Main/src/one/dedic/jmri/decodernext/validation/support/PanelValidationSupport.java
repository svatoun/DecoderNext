/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.support;

import one.dedic.jmri.decodernext.validation.swing.DefaultIconFeedback;
import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import one.dedic.jmri.decodernext.validation.SwingAttached;
import one.dedic.jmri.decodernext.validation.ValidationConstants;
import one.dedic.jmri.decodernext.validation.ValidationFeedback;
import one.dedic.jmri.decodernext.validation.ValidatorService;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sdedic
 */
public class PanelValidationSupport implements ValidatorService, ContextValidator, ValidationFeedback {
    private static final Logger LOG = LoggerFactory.getLogger(PanelValidationSupport.class);
    
    public static final Predicate<Component> ALL = new Predicate<Component>() {
        @Override
        public boolean test(Component t) {
            return true;
        }
    };

    /**
     * The root where this Support is attached.
     */
    private final JComponent    root;
    
    /**
     * Maps JComponents to Entries.
     */
    private final Map<JComponent, Entry> components = new HashMap<>();
    
    
    private final List<ChangeListener> listeners = new ArrayList<>();
    
    /**
     * Message key to Entry map. Will be looked up when processing {@link ValidationMessage}s.
     */
    private final Map<Object, Entry> entries = new HashMap<>();
    
    private Set cachedKeys;
    
    private boolean initialized;
    
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
        root.putClientProperty(ValidationConstants.COMPONENT_VALIDATOR, this);
    }
    
    public ValidatorService build() {
        scanComponents(root);
        root.putClientProperty(ValidationConstants.COMPONENT_VALIDATOR, this);
        root.putClientProperty(ValidationConstants.COMPONENT_FEEDBACK, this);
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
        for (Entry e2 : components.values()) {
            for (ValidationFeedback f : e2.feedbacks) {
                if (f.getKeys().contains(key)) {
                    return e2.component;
                }
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
        for (Entry e : components.values()) {
            ValidatorService srv = e.validator;
            if (srv instanceof ContextValidator) {
                ((ContextValidator)srv).attach(context);
            }
        }
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
        cachedKeys = null;
        assert SwingUtilities.isEventDispatchThread();
        ValidationResult newResult = new ValidationResult();
        for (Entry e : components.values()) {
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
    
    /**
     * Describes the managed JComponent. Records services exposed by it or
     * attached to it.
     */
    private static class Entry {
        /**
         * The component itself.
         */
        final Component             component;
        
        /**
         * Validator service for the component. May be {@code null} to indicate
         * the component is feedback only.
         */
        final ValidatorService      validator;
        
        /**
         * Set of keys declared by the {@component}, or attached by 
         * some Validator.
         */
        final Set                   keys = new HashSet<>();

        final List<ValidationFeedback>    feedbacks = new ArrayList<>();
        
        /**
         * Records a Label attached to a validatable Component.
         */
        JLabel  iconPlaceholder;
        
        Component validationTarget;
        
        /**
         * Last obtained result for this component / feedback.
         */
        ValidationResult partialResult;

        public Entry(Component component, ValidatorService validator, Set keys) {
            this.component = component;
            this.validator = validator;
            this.keys.addAll(keys);
        }

        public Entry setValidationTarget(Component validationTarget) {
            this.validationTarget = validationTarget;
            return this;
        }
        
        public boolean test(Object key) {
            if (keys.contains(key)) {
                return true;
            }
            for (ValidationMessage msg : partialResult) {
                if (keys.contains(msg.key())) {
                    return true;
                }
            }
            return false;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            
            sb.append(String.format("E[%s@%x, k: %s, v: %s, f: %d",
                    component.getClass(), System.identityHashCode(component),
                    keys, validator, feedbacks.size()));
            if (iconPlaceholder != null) {
                sb.append(String.format(", i: %s@%x",
                        iconPlaceholder.getClass(), System.identityHashCode(iconPlaceholder)));
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    public ValidatorService createDefaultService(JComponent c, Validator worker, AtomicReference<Component> target) {
        DefaultValidatorService service = DefaultValidatorService.attachValidatorService(c, worker);
        target.set(service.getTarget());
        return service;
    }

    @Override
    public Collection<Object> getKeys() {
        if (cachedKeys != null) {
            return cachedKeys;
        }
        Set<Object> obs = new HashSet<>();
        components.values().forEach((e) -> {
            e.feedbacks.forEach((f) -> obs.addAll(f.getKeys()));
        });
        cachedKeys = obs;
        return obs;
    }
    
    private ValidatorService extractValidatorService(JComponent jc) {
        Object o = jc.getClientProperty(ValidationConstants.COMPONENT_VALIDATOR);
        if (o == null) {
            return null;
        }
        LOG.debug("Processing validator on {}: {}", jc, o);
        ValidatorService srv = null;
        AtomicReference<Component> r = new AtomicReference<>();
        if (o instanceof ValidatorService) {
            LOG.debug("Found ValidatorService");
            srv = (ValidatorService)o;
        } else if (o instanceof Validator) {
            LOG.debug("Found Validator");
            srv = createDefaultService(jc, (Validator)o, r);
        } 
        if (srv == null) {
            return null;
        }
        Set<Object> keys;
        Object key = jc.getClientProperty(ValidationConstants.COMPONENT_VALIDATION_KEY);
        if (key == null) {
            keys = Collections.singleton(jc);
        } else if (key instanceof Collection) {
            keys = new HashSet<>((Collection)key);
        } else if (key instanceof Object[]) {
            keys = new HashSet<>(Arrays.asList((Object[])key));
        } else {
            keys = Collections.singleton(key);
        }
        LOG.debug("Component identified by keys: {}", keys);
        Entry e = new Entry(jc, srv, keys).setValidationTarget(r.get());
        for (Object x : keys) {
            entries.put(x, e);
        }
        components.put(jc, e);
        srv.addChangeListener(delegeateChangeL);
        return srv;
    }
    
    private ValidationFeedback attachFeedbackService(JComponent jc) {
        Object o = jc.getClientProperty(ValidationConstants.COMPONENT_FEEDBACK);
        if (o == null || !(o instanceof ValidationFeedback)) {
            return null;
        }
        ValidationFeedback feedback = (ValidationFeedback)o;
        LOG.debug("Custom feedback {} provided by {}", feedback, jc);
        Collection keys = feedback.getKeys();
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        LOG.debug("Custom feedback keys: {}", keys);
        keys.forEach((k) -> registerFeedback(k, jc, feedback));
        
        return feedback;
    }
        
    private void registerFeedback(Object key, JComponent jc, ValidationFeedback f) {
        if (components.containsKey(jc)) {
            return;
        }
        Entry e = entries.get(key);
        if (e == null) {
            e = new Entry(jc, null, Collections.singleton(key));
            components.putIfAbsent(jc, e);
        } else {
            LOG.debug("Feedback {}, key {} registered for {}", f, key, e);
    }
        e.feedbacks.add(f);
    }
    
    private ValidationFeedback attachIconHolder(JComponent jc) {
        Object o = jc.getClientProperty(ValidationConstants.COMPONENT_VALIDATION_ICON);
        if (o == null || !(jc instanceof JLabel)) {
            return null;
        }
        JLabel l = (JLabel)jc;
        Component target = l.getLabelFor();
        if (target == null || !(target instanceof JComponent)) {
            return null;
        }
        LOG.debug("Marked as validator-icon: {} for {1}", jc, target);
        JComponent tc = (JComponent)target;
        Entry e = findEntry(tc);
        if (e == null || e.keys.isEmpty()) {
            return null;
        }
        
        if (e.iconPlaceholder != null) {
            LOG.debug("Duplicate placeholder - ignoring");
        }
        LOG.debug("Registered as placeholder");
        e.iconPlaceholder = l;
        
        ValidationFeedback fb = new DefaultIconFeedback(l, e.keys);
        e.feedbacks.add(fb);
        
        return fb;
    }
    
    private Entry findEntry(JComponent jc) {
        return components.get(jc);
    }
    
    private Set<JComponent> rescanFound;
    
    private void scanComponentTree(Container parent, Predicate<JComponent> processor) {
        for (Component c : parent.getComponents()) {
            if (c == null) {
                continue;
            }
            if (!(c instanceof JComponent) && acceptor.test(c)) {
                continue;
            }
            JComponent jc = (JComponent)c;
            rescanFound.add(jc);
            if (components.containsKey(jc)) {
                continue;
            }
            if (processor.test(jc)) {
                continue;
            }
            if (c instanceof Container) {
                Container owner = (Container)c;
                if (owner.getComponentCount() > 0) {
                    scanComponents((Container)c);
                }
            }
        }
    }
    
    private void scanComponents(Container parent) {
        LOG.debug("Scanning for validatable components in {}", parent);
        rescanFound = new HashSet<>();
        scanComponentTree(parent, (c) -> {
            LOG.debug("Inspecting: {}", c);
            JComponent jc = (JComponent)c;
            boolean a = extractValidatorService(jc) != null;
            attachFeedbackService(jc);
            return a;
        });
        LOG.debug("Scanning for feedback in {}", parent);
        scanComponentTree(parent, (c) -> {
            attachIconHolder(c);
            return components.containsKey(c);
        });
        
        // remove potential components NOT found:
        Set<JComponent> registered = new HashSet<>(components.keySet());
        registered.removeAll(rescanFound);
        detach(registered);
    }
    
    private void detach(Collection<JComponent> unregister) {
        Collection<ValidationFeedback> fbs = new HashSet<>();
        unregister.forEach(jc -> {
            Entry e = components.remove(jc);
            entries.keySet().removeAll(e.keys);
            if (e.validator != null) {
                e.validator.removeNotify();
            }
            fbs.addAll(e.feedbacks);
        });
        components.values().forEach(e -> {
            fbs.removeAll(e.feedbacks);
        });
        fbs.forEach(fb -> fb.removeNotify());
    }
    
    private Map<Object, List<ValidationMessage>> splitMessages(ValidationResult result) {
        Map<Object, List<ValidationMessage>> messages = new HashMap<>();
        for (ValidationMessage msg : result) {
            Object o = msg.key();
            Component c = findComponent(o);
            if (c == null) {
                continue;
            }
            Entry e = findEntry((JComponent)c);
            if (e == null) {
                continue;
            }
            messages.computeIfAbsent(o, (x) -> new ArrayList<>());
        }
        return messages;
    }
    
    @Override
    public void indicateResult(ValidationResult result) {
        forwardPartialResults(result, ValidationFeedback::indicateResult);
    }
    
    private void forwardPartialResults(ValidationResult result, BiConsumer<ValidationFeedback, ValidationResult> delegate) {
        Map<ValidationFeedback, Set<ValidationMessage>> messageSets = new HashMap<>();
        List<ValidationMessage> allMessages = result.getMessages();
        for (Entry e : components.values()) {
            Set<ValidationMessage> mset = new LinkedHashSet<>();
            Set keys = new HashSet<>(e.keys);
            e.feedbacks.forEach(f -> {
                Set fk = keys;
                if (!fk.containsAll(f.getKeys())) {
                    fk = new HashSet<>(fk);
                    fk.addAll(f.getKeys());
                }
                for (ValidationMessage m : allMessages) {
                    if (fk.contains(m.key())) {
                        messageSets.computeIfAbsent(f, (f2) -> new LinkedHashSet<>()).add(m);
                    }
                }
            });
        }
        for (ValidationFeedback f : messageSets.keySet()) {
            ValidationResult forward = new ValidationResult();
            forward.addAll(new ArrayList<>(messageSets.get(f)));
            delegate.accept(f, forward);
        }
    }

    @Override
    public boolean reportMessages(ValidationResult result) {
        forwardPartialResults(result, ValidationFeedback::reportMessages);
        return true;
    }

    @Override
    public Action transferControl(Object key) {
        return null;
    }
    
    private void processValidatorsAndFeedbacks(Consumer<SwingAttached> callback) {
        for (Entry e : components.values()) {
            ValidatorService val = e.validator;
            if (val != null) {
                callback.accept(val);
            }
            for (ValidationFeedback fb : e.feedbacks) {
                if (fb != val) {
                    callback.accept(fb);
                }
            }
        }
    }
    
    private int addCounter;

    @Override
    public void addNotify() {
        if (addCounter++ == 0) {
            if (!initialized) {
                build();
            }
            processValidatorsAndFeedbacks(SwingAttached::addNotify);
        }
    }

    @Override
    public void removeNotify() {
        if (--addCounter == 0) {
            processValidatorsAndFeedbacks(SwingAttached::removeNotify);
        }
    }
}
