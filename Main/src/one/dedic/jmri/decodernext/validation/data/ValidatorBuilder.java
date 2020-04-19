/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import one.dedic.jmri.decodernext.validation.support.CompoundValidator;
import com.jgoodies.validation.Validator;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import one.dedic.jmri.decodernext.validation.ValidationConstants;
import one.dedic.jmri.decodernext.validation.ValidationUtils;

/**
 * Simplifies building of a Validator. Uses a 'fluent' interface to build and attach a 
 * validator to a Component. Each builder can have a {@limk ValidatedValue} that extracts
 * the actual value to be validated; the extracted value will be passed to one or more
 * Validators to verify. Output from all validators is consolidated in {@link ValidationResult}
 * <p/>
 * If {@link Validator} implements also {@link ValidatorSetup}, it will be configured with
 * {@link ValidatorSetup#setLabel} so it can include the label in messages printed. Label 
 * is defined by {@link #display}, or is taken from {@link JLabel} for the validated Component.
 * <o/>
 * Message key used by the validator is configured by {@link #key(java.lang.Object)}, or
 * can be declared as {@link ValidationConstants#COMPONENT_VALIDATION_KEY} client property
 * on the validated JComponent.
 * 
 * @author sdedic
 */
public class ValidatorBuilder {
    private final JComponent  theComponent;
    private Container parent;

    private String displayName;
    private ValidatedValue valueProvider;
    private JLabel  iconIndicator; 
    private JLabel  controlLabel; 
    private Object key;
    private List<Validator> validators = new ArrayList<>();
    
    private Validator finalValidator;

    public ValidatorBuilder(JComponent theComponent) {
        this.theComponent = theComponent;
    }
    
    public static ValidatorBuilder forComponent(JComponent c) {
        return new ValidatorBuilder(c);
    }
    
    public ValidatorBuilder key(Object k) {
        this.key = k;
        return this;
    }
    
    public ValidatorBuilder display(String dispName) {
        this.displayName = dispName;
        return this;
    }
    
    public ValidatorBuilder  value(ValidatedValue p) {
        this.valueProvider = p;
        return this;
    }
    
    public ValidatorBuilder  validate(Validator v) {
        validators.add(v);
        return this;
    }
    
    public ValidatorBuilder  validate(Validator... vs) {
        validators.addAll(Arrays.asList(vs));
        return this;
    }
    
    public ValidatorBuilder  status(JLabel indicator) {
        this.iconIndicator = indicator;
        return this;
    }
    
    private JLabel findControlLabel() {
        if (controlLabel != null) {
            return controlLabel;
        }
        Container parent = theComponent.getParent();
        if (parent == null) {
            return null;
        }
        for (Component c : parent.getComponents()) {
            if (c instanceof JLabel) {
                JLabel l = (JLabel)c;
                if (l.getLabelFor() == theComponent &&
                    l != iconIndicator &&
                    l.getText().length() > 1) {
                    controlLabel = l;
                    return l;
                }
            }
        }
        return null;
    }
    
    private void findIconIndicator() {
        if (iconIndicator != null) {
            return;
        }

        Container parent = theComponent.getParent();
        if (parent == null) {
            return;
        }
        for (Component c : parent.getComponents()) {
            if (c instanceof JLabel) {
                JLabel l = (JLabel)c;
                if (l.getLabelFor() == theComponent &&
                    l.getText().length() <= 1) {
                    iconIndicator = l;
                    return;
                }
            }
        }
    }
    
    private void findParent() {
        if (parent != null) {
            return;
        }
        parent = theComponent.getParent();
    }
    
    private void findDisplayName() {
        if (displayName != null) {
            return;
        }
        JLabel l = findControlLabel();
        if (l != null) {
            String s = l.getText().trim();
            if (s.endsWith(":")) { // NOI18N
                s = s.substring(0, s.length() - 1).trim();
            }
            displayName = s;
        }
    }
    
    private void configureValidator(Validator v) {
        if (!(v instanceof ValidatorSetup)) {
            return;
        }
        ValidatorSetup s = (ValidatorSetup)v;
        if (key != null) {
            s.setMessageKey(key);
        }
        if (displayName != null) {
            s.setLabel(displayName);
        }
    }
    
    private Validator constructValidator() {
        Validator tmp;
        
        if (validators.size() > 1) {
            tmp = new CompoundValidator(validators);
        } else {
            tmp = validators.get(0);
        }
        
        if (valueProvider != null) {
            finalValidator = new ValueExtractProxy(valueProvider, tmp);
        } else {
            finalValidator = tmp;
        }
        return finalValidator;
    }
    
    private void declareValidator() {
        if (finalValidator == null) {
            return;
        }
        ValidationUtils.attachValidator(theComponent, finalValidator);
    }
    
    private void declareFeedback() {
        theComponent.putClientProperty(ValidationConstants.COMPONENT_VALIDATION_KEY, key);
        if (iconIndicator == null) {
            return;
        }
        iconIndicator.putClientProperty(ValidationConstants.COMPONENT_VALIDATION_ICON, key);
    }
    
    public Validator build() {
        findParent();
        findIconIndicator();
        findControlLabel();
        findDisplayName();
        validators.forEach(this::configureValidator);
        
        Validator product = constructValidator();
        declareValidator();
        declareFeedback();
        
        ValidationUtils.attachValidator(theComponent, product);
        
        return product;
    }
}
