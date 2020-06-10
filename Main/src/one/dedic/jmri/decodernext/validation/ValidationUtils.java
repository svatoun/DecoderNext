/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

import com.jgoodies.validation.Validatable;
import com.jgoodies.validation.Validator;
import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;

/**
 *
 * @author sdedic
 */
public class ValidationUtils {
    
    public static Set<Object> getOneOrMoreItems(Object data, Object defValue) {
        if (data == null) {
            if (defValue == null) {
                return null;
            }
            return Collections.singleton(defValue);
        } else if (data instanceof Collection) {
            return new HashSet<>((Collection)data);
        } else if (data instanceof Object[]) {
            return new HashSet<>(Arrays.asList((Object[])data));
        } else {
            return Collections.singleton(data);
        }
    }
    
    public static ValidationContainer<ValidationFeedback> findFeedbackContainer(JComponent start) {
        return findValidatorContainer(start, ValidationConstants.COMPONENT_FEEDBACK);
    }
    
    public static ValidationContainer<ValidatorService> findValidatorContainer(JComponent start) {
        return findValidatorContainer(start, ValidationConstants.COMPONENT_VALIDATOR);
    }
    
    private static <T extends SwingAttached> ValidationContainer<T> findValidatorContainer(Component start, String property) {
        while (start instanceof JComponent) {
            ValidationFeedback feedback = getFeedback((JComponent)start);
            if (feedback instanceof ValidationContainer) {
                return (ValidationContainer<T>)feedback;
            }
            start = start.getParent();
        }
        return null;
    }
    
    public static ValidationFeedback getFeedback(JComponent c) {
        Object o = c.getClientProperty(ValidationConstants.COMPONENT_FEEDBACK);
        if (o instanceof ValidationFeedback) {
            return ((ValidationFeedback)o);
        } else {
            return null;
        }
    }
    
    public static void attachValidator(JComponent c, Validator f) {
        c.putClientProperty(ValidationConstants.COMPONENT_VALIDATOR, f);
    }

    public static void attachValidator(JComponent c, Validatable f) {
        c.putClientProperty(ValidationConstants.COMPONENT_VALIDATOR, f);
    }

    public static void declareAsFeedback(JComponent c, Object k) {
        c.putClientProperty(ValidationConstants.COMPONENT_FEEDBACK, k);
    }

    public static void attachFeedback(JComponent c, ValidationFeedback f) {
        c.putClientProperty(ValidationConstants.COMPONENT_FEEDBACK, f);
    }
}
