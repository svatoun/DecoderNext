/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.support;

import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.ValidationResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import javax.swing.Action;
import javax.swing.JComponent;
import one.dedic.jmri.decodernext.validation.ValidationContainer;
import one.dedic.jmri.decodernext.validation.ValidationFeedback;

/**
 * Covers a set of {@link ValidationFeedback} instances into a single unit. Usually
 * covers a JPanel and collects feedbacks in its subtree. These Collections can be
 * chained up to the root of the UI (e.g. a dialog).
 * 
 * @author sdedic
 */
public class FeedbackGroup implements ValidationFeedback, ValidationContainer<ValidationFeedback> {
    private JComponent    ui;
    
    /**
     * Registered items.
     */
    private Collection<ValidationFeedback>  items = new LinkedHashSet<>();
    
    /**
     * Key cache.
     */
    private Map<Object, ValidationFeedback> itemKeys;
    
    private boolean active;

    public FeedbackGroup(JComponent ui) {
        this.ui = ui;
    }

    public FeedbackGroup() {
    }

    public void setUi(JComponent ui) {
        this.ui = ui;
    }

    public JComponent getUI() {
        return ui;
    }
    
    public Collection<ValidationFeedback> getItems() {
        return new ArrayList<>(items);
    }
    
    public void add(ValidationFeedback item) {
        synchronized (this) {
            if (items.add(item)) {
                itemKeys = null;
            }
        }
    }
    
    public void remove(ValidationFeedback item) {
        if (item == null) {
            return;
        }
        synchronized (this) {
            if (items.remove(item)) {
                itemKeys = null;
            }
        }
    }
    
    @Override
    public void addNotify() {
        if (active) {
            return;
        }
        List<ValidationFeedback> list;
        synchronized (this) {
            if (items.isEmpty()) {
                return;
            }
            list = new ArrayList<>(items);
        }
        list.forEach(ValidationFeedback::addNotify);
        active = true;
    }

    @Override
    public void removeNotify() {
        if (!active) {
            return;
        }
        List<ValidationFeedback> list;
        synchronized (this) {
            if (items.isEmpty()) {
                return;
            }
            list = new ArrayList<>(items);
        }
        list.forEach(ValidationFeedback::removeNotify);
        active = false;
    }
    
    @Override
    public void indicateResult(ValidationResult result) {
        forwardPartialResults(result, (f, r) -> {
            f.indicateResult(result);
            return true;
        });
    }
    
    private ValidationResult forwardPartialResults(ValidationResult result, BiFunction<ValidationFeedback, ValidationResult, Boolean> delegate) {
        Collection<ValidationFeedback> remainder = new HashSet<>(this.items);
        Map<ValidationFeedback, Set<ValidationMessage>> messageSets = new HashMap<>();
        List<ValidationMessage> allMessages = result.getMessages();
        for (ValidationFeedback f : items) {
            for (ValidationMessage m : allMessages) {
                if (f.getKeys().contains(m.key())) {
                    messageSets.computeIfAbsent(f, (f2) -> new LinkedHashSet<>()).add(m);
                    remainder.remove(f);
                }
            }
        }
        
        List<ValidationMessage> leftovers = new ArrayList<>();
        for (ValidationFeedback f : messageSets.keySet()) {
            ValidationResult forward = new ValidationResult();
            Collection<ValidationMessage> msgs = messageSets.get(f);
            forward.addAll(new ArrayList<>(msgs));
            if (Boolean.FALSE.equals(delegate.apply(f, forward))) {
                leftovers.addAll(msgs);
            }
        }
        remainder.stream().forEach(f -> delegate.apply(f, ValidationResult.EMPTY));
        if (leftovers.isEmpty()) {
            return ValidationResult.EMPTY;
        }
        ValidationResult r = new ValidationResult();
        r.addAll(leftovers);
        return r;
    }

    @Override
    public boolean reportMessages(ValidationResult result) {
        forwardPartialResults(result, ValidationFeedback::reportMessages);
        return true;
    }

    @Override
    public Action transferControl(Object key) {
        Optional<ValidationFeedback> fb = items.stream().filter(f -> f.getKeys().contains(key)).findFirst();
        if (fb.isPresent()) {
            return fb.get().transferControl(key);
        } else {
            return null;
        }
    }

    @Override
    public Collection<Object> getKeys() {
        synchronized (this) {
            if (itemKeys != null) {
                return itemKeys.keySet();
            }
        }
        Map<Object, ValidationFeedback> keys = new HashMap<>();
        items.forEach(f -> {
            f.getKeys().forEach((k) -> {
                keys.put(k, f);
            });
        });
        synchronized (this) {
            if (itemKeys == null) {
                itemKeys = Collections.unmodifiableMap(keys);
            }
            return itemKeys.keySet();
        }
    }

    @Override
    public void attachChild(JComponent ui, ValidationFeedback child) {
        items.add(child);
        if (active) {
            child.addNotify();
        }
        synchronized (this) {
            itemKeys = null;
        }
    }

    @Override
    public void dettachChild(JComponent ui, ValidationFeedback child) {
        if (active) {
            child.removeNotify();
        }
        items.remove(child);
        synchronized (this) {
            itemKeys = null;
        }
    }
}
