/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.support;

import com.jgoodies.validation.Validatable;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import one.dedic.jmri.decodernext.validation.ValidationConstants;
import one.dedic.jmri.decodernext.validation.ValidationContainer;
import one.dedic.jmri.decodernext.validation.ValidationFeedback;
import one.dedic.jmri.decodernext.validation.ValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides containment for {@link ValidatorService}s and {@link ValidationFeedback}s 
 * within a JPanel.
 * This class intercepts hierarchy changes and adjust the set of validators
 * participating in panel validation: adds / removes validators from the {@link ValidationGroup}
 * so that only visible / attached Validators are actually processed during validation.
 * Each validator attach/detach will generate a ChangeEvent to inform a controller that
 * a new validation is needed.
 * 
 * @author sdedic
 */
public class PanelValidationSupport2 extends ValidatorGroup implements ValidationContainer<ValidatorService>, ContextValidator {
    private static final Logger LOG = LoggerFactory.getLogger(PanelValidationSupport2.class);

    private final JComponent owner;
    private final ValidatorGroup validators = this;
    private final FeedbackGroup feedbacks = new FeedbackGroup();
    private final HierarchyListener hL = new HierarchyListener() {
        Reference<Component>    formerParent = new WeakReference<>(null);
        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) > 0 && owner.isDisplayable()) {
                Component p = formerParent.get();
                if (owner.getParent() != p) {
                    reattached();
                    formerParent = new WeakReference<>(owner.getParent());
                }
            }
        }
    };

    public static final Predicate<Component> ALL = new Predicate<Component>() {
        @Override
        public boolean test(Component t) {
            return true;
        }
    };

    private Predicate<Component>  acceptor = ALL;

    private boolean initialized;
    
    private final Map<JComponent, CInfo>    components = new HashMap<>();
    
    /**
     * An entry for a registered Component plus what the Component gives us.
     * A Component is only inspected the first time it is encountered.
     */
    class CInfo implements HierarchyListener {
        final JComponent    component;
        ValidatorService    validator;
        ValidationFeedback  feedback;

        public CInfo(JComponent component) {
            this.component = component;
            component.addHierarchyListener(this);
        }

        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if (!initialized) {
                return;
            }
            if ((e.getChangeFlags() & (HierarchyEvent.DISPLAYABILITY_CHANGED | HierarchyEvent.SHOWING_CHANGED)) > 0) {
                boolean visible = component.isShowing();
                if (validator != null) {
                    if (visible) {
                        validators.addValidator(validator);
                    } else {
                        validators.removeValidator(validator);
                    }
                }
                if (feedback != null) {
                    if (visible) {
                        feedbacks.add(feedback);
                    } else {
                        feedbacks.remove(feedback);
                    }
                }
            }
        }
        
        void dispose() {
            validators.removeValidator(validator);
            feedbacks.remove(feedback);
            component.removeHierarchyListener(this);
        }
    }
    
    public PanelValidationSupport2(JComponent owner) {
        this.owner = owner;
        feedbacks.setUi(owner);
        // hook on its own add to the hierarchy
        owner.addHierarchyListener(hL);
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        validators.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        validators.removeChangeListener(l);
    }

    @Override
    public ValidationResult getValidation() {
        return validators.getValidation();
    }

    @Override
    public ValidationResult validate() {
        return validators.validate();
    }

    @Override
    public Component findComponent(Object key) {
        return null;
    }
    
    private int addCounter;
    
    private void reattached() {
        if (!initialized) {
            scanComponents(owner);
        }
        
    }
    
    @Override
    public void addNotify() {
        assert SwingUtilities.isEventDispatchThread();
        if (addCounter++ == 0) {
            validators.addNotify();
            feedbacks.addNotify();
        }
    }

    @Override
    public void removeNotify() {
        assert SwingUtilities.isEventDispatchThread();
        if (--addCounter == 0) {
            validators.removeNotify();
            feedbacks.addNotify();
        }
    }

    private Set<JComponent> rescanFound;
    
    public ValidatorService createDefaultService(JComponent c, Validator worker, AtomicReference<Component> target) {
        DefaultValidatorService service = DefaultValidatorService.attachValidatorService(c, worker);
        target.set(service.getTarget());
        return service;
    }

    public ValidatorService createDefaultService(JComponent c, Validatable worker, AtomicReference<Component> target) {
        DefaultValidatorService service = DefaultValidatorService.attachValidatorService(c, worker);
        target.set(service.getTarget());
        return service;
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
        }  else if (o instanceof Validatable) {
            srv = createDefaultService(jc, (Validatable)o, r);
        }
        if (srv == null) {
            return null;
        }
        
        return registerComponentValidator(jc, srv);
    }
    
    private ValidatorService registerComponentValidator(JComponent jc, ValidatorService v) {
        CInfo ci = components.get(jc);
        if (ci == null) {
            ci = new CInfo(jc);
        }
        ci.validator = v;
        validators.addValidator(v);
        return v;
    }
    
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
    
    private ValidationFeedback createLabelIconFeedback(JComponent jc) {
        return null;
    }
    
    private ValidationFeedback extractFeedbackService(JComponent jc) {
        Object o = jc.getClientProperty(ValidationConstants.COMPONENT_FEEDBACK);
        ValidationFeedback feedback;
        if (o instanceof ValidationFeedback) {
            return (ValidationFeedback)o;
        }
        o = jc.getClientProperty(ValidationConstants.COMPONENT_VALIDATION_ICON);
        if (o != null) {
            return createLabelIconFeedback(jc);
        }
        
        return null;
    }
    
    private ValidationFeedback attachFeedbackService(JComponent jc) {
        ValidationFeedback feedback = extractFeedbackService(jc);
        if (feedback == null) {
            return null;
        }
        feedbacks.add(feedback);
        return feedback;
    }
        
    private void scanComponents(Container parent) {
        LOG.debug("Scanning for validatable components in {}", parent);
        rescanFound = new HashSet<>();
        runAtomic(() -> {
            try {
                scanComponentTree(parent, (c) -> {
                    LOG.debug("Inspecting: {}", c);
                    JComponent jc = (JComponent)c;
                    // only dive into the component if it does not declare
                    // a ValidatorService...
                    boolean a = extractValidatorService(jc) != null |
                            attachFeedbackService(jc) != null;
                    return a;
                });
                LOG.debug("Scanning for feedback in {}", parent);
                scanComponentTree(parent, (c) -> {
//                    attachIconHolder(c);
                    return components.containsKey(c);
                });

                // remove potential components NOT found:
                Set<JComponent> registered = components.keySet();
                registered.removeAll(rescanFound);
            } finally {
                rescanFound = null;
            }
        });
    }

    @Override
    public void attachChild(JComponent ui, ValidatorService child) {
        registerComponentValidator(ui, child);
    }

    @Override
    public void dettachChild(JComponent ui, ValidatorService child) {
        CInfo ci = components.get(ui);
        if (ci == null) {
            return;
        }
        if (ci.feedback == null) {
            remove(ci);
        } else {
            ci.validator = null;
        }
    }
    
    private void remove(CInfo cinfo) {
        cinfo.dispose();
        components.remove(cinfo.component);
    }
}
