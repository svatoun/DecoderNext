/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

import com.jgoodies.validation.Validatable;
import com.jgoodies.validation.Validator;
import java.awt.Component;
import javax.swing.JComponent;

/**
 *
 * @author sdedic
 */
public class ValidationUtils {
    
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
