/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.ValidationResult;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import one.dedic.jmri.decodernext.validation.support.ValidatorGroup;
import org.openide.util.Lookup;

/**
 * The feedback controller provides an overall model for validation feedback
 * for a Swing container. It collects underlying ValidationService's data,
 * organizes validation. 
 * 
 * @author sdedic
 */
public class FeedbackController {
    public static final String PROP_CONTAINS_ERRORS = "containsErrors";
    public static final String PROP_RESULT = "result";

    private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
    private final L l = new L();

    private Lookup context;
    private ValidationResult  result = ValidationResult.EMPTY;
    private Map<Object, Collection<ValidationFeedback>> feedbacks = new HashMap<>();
    private Collection<ValidationFeedback> allFeedbacks = new HashSet<>();
    
    private Set<ValidationMessage>  suppressedMessages = new HashSet<>();
    private Set<Object> suppressedKeys = new HashSet<>();
    private Timer scheduled = new Timer(300, l);
    
    private ValidatorGroup  validators = new ValidatorGroup();
    
    /**
     * Summary feedback, which is always set.
     */
    private ValidationFeedback  summaryFeedback;

    public FeedbackController() {
        validators.addChangeListener(l);
    }
    
    class L implements ChangeListener, ActionListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            scheduleValidation();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            performValidation();
        }
        
    }

    public ValidationFeedback getSummaryFeedback() {
        return summaryFeedback;
    }

    public void setSummaryFeedback(ValidationFeedback summaryFeedback) {
        this.summaryFeedback = summaryFeedback;
    }

    public Lookup getContext() {
        return context;
    }

    public void setContext(Lookup context) {
        this.context = context;
        validators.attach(context);
    }
    
    public void suppressMessage(ValidationMessage msg) {
        this.suppressedMessages.add(msg);
        setResult(result);
    }
    
    public void suppressKey(Object key) {
        this.suppressedKeys.add(key);
        setResult(result);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        supp.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        supp.removePropertyChangeListener(l);
    }
    
    public void addInput(ValidatorService service) {
        validators.addValidator(service);
    }
    
    public void addFeedback(ValidationFeedback feedback) {
        for (Object o : feedback.getKeys()) {
            feedbacks.computeIfAbsent(o, (x) -> new LinkedHashSet<>()).add(feedback);
        }
        if (feedback instanceof ContextValidator) {
            ((ContextValidator)feedback).attach(context);
        }
        allFeedbacks.add(feedback);
    }
    
    public void performValidation() {
        collectValidationResult();
    }
    
    private void collectValidationResult() {
        setResult(validators.validate());
    }
    
    void publishFeedback(ValidationResult result) {
        Map<ValidationFeedback, Set<ValidationMessage>> partials = new HashMap<>();
        for (ValidationMessage msg : result.getMessages()) {
            Object k = msg.key();
            Collection<ValidationFeedback> fbs = feedbacks.get(k);
            if (fbs == null) {
                continue;
            }
            for (ValidationFeedback fb : fbs) {
                partials.computeIfAbsent(fb, (f) -> new LinkedHashSet<>()).add(msg);
            }
        }
        Set<ValidationFeedback> remainder = new HashSet<>(allFeedbacks);
        for (ValidationFeedback fb : partials.keySet()) {
            ValidationResult r = new ValidationResult();
            r.addAll(new ArrayList<>(partials.get(fb)));
            if (!fb.reportMessages(r)) {
                fb.indicateResult(r);
            }
            remainder.remove(fb);
        }
        remainder.stream().forEach(f -> f.indicateResult(ValidationResult.EMPTY));
        
        if (summaryFeedback != null) {
            summaryFeedback.reportMessages(result);
        }
    }
    
    void setResult(ValidationResult overall) {
        ValidationResult nr;
        if (suppressedKeys.isEmpty() && suppressedMessages.isEmpty()) {
            nr = overall;
        } else {
            nr =  new ValidationResult();
            for (ValidationMessage m : result.getMessages()) {
                if (suppressedMessages.contains(m)) {
                    continue;
                }
                if (suppressedKeys.contains(m.key())) {
                    continue;
                }
                nr.add(m);
            }
        }

        ValidationResult previous = this.result;
        result = nr;
        supp.firePropertyChange(PROP_CONTAINS_ERRORS, previous.hasErrors(), result.hasErrors());
        supp.firePropertyChange(PROP_RESULT, previous, result);
        
        publishFeedback(result);
    }
    
    private void scheduleValidation() {
        scheduled.setRepeats(false);
        scheduled.start();
    }
    
    public boolean getContainsErrors() {
        return result.hasErrors();
    }
    
    public void connectFrom(Container from) {
        for (Component c : from.getComponents()) {
            if (!(c instanceof JComponent)) {
                continue;
            }
            JComponent jc = (JComponent)c;
            Object v = jc.getClientProperty(ValidationConstants.COMPONENT_VALIDATOR);
            boolean handled = false;
            if (v instanceof ValidatorService) {
                addInput((ValidatorService)v);
                handled = true;
            }
            v = jc.getClientProperty(ValidationConstants.COMPONENT_FEEDBACK);
            if (v instanceof ValidationFeedback) {
                addFeedback((ValidationFeedback)v);
                handled = true;
            }
            if (!handled && jc.getComponentCount() > 0) {
                connectFrom(jc);
            }
        }
    }
}
