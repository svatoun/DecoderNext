/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

import com.jgoodies.validation.Validatable;

/**
 *
 * @author sdedic
 */
public class ValidationConstants {
    /**
     * Marks a JLabel as a placeholder that should indicate validation result status.
     * Such label must have properly set the {@link javax.swing.JLabel#setLabelFor} reference,
     * so the validation infrastructure will connect it to the appropriate Validator result.
     * The value of the client property must be a message key (Object) shown by that icon,
     * or Boolean.TRUE, which will accept the key from the linked Component.
     */
    public static final String  COMPONENT_VALIDATION_ICON = "jmri.validationIcon";
    
    /**
     * Client property, which holds an instance of {@link Validator} for a Component.
     * The infrastructure will connect to the component's focus lost (or other appropriate
     * event) and will perform a validation.
     * <p>
     * The key can contain either {@link Validatable} or {@link Validator}.
     */
    public static final String  COMPONENT_VALIDATOR = "jmri.validator";
    
    /**
     * Client property that holds the validator service, if present. A validator service
     * should be used in preference to just plain {@link #COMPONENT_VALIDATOR} at runtime.
     */
    public static final String  COMPONENT_VALIDATOR_SERVICE = "jmri.validator.service";
    
    /**
     * Declares a component a feedback area, and attaches a key.
     */
    public static final String  COMPONENT_FEEDBACK = "jmri.feedback.key";
    
    /**
     * Declares a feedback for a control
     */
    public static final String  COMPONENT_FEEDBACK_CONTROL = "jmri.feedback.control";
    
    /**
     * A key that identifies the component's validation messages. If set on components,
     * it will identify that component as a target for Action to activate the key.
     * The client property can be set to a single Object, Collection or an array of Objects.
     */
    public static final String  COMPONENT_VALIDATION_KEY = "jmri.validationKey";
    
    /**
     * Client property name, which holds a change monitor for the component.
     */
    public static final String  CONTROL_CHANGE_SUPPORT = "jmri.control.change.support";
    
    public static final String  CONTROL_CHANGE_MARK = "jmri.control.change.key";
    
    public static final String EMPTY_ICON = "one/dedic/jmri/decodernext/resources/blankIcon.png"; // NOI18N
    public static final String OK_ICON = "one/dedic/jmri/decodernext/resources/okIcon.png"; // NOI18N
    public static final String INFO_ICON = "one/dedic/jmri/decodernext/resources/infoIcon.png"; // NOI18N
    public static final String WARNING_ICON = "one/dedic/jmri/decodernext/resources/warningIcon.png"; // NOI18N
    public static final String ERROR_ICON = "one/dedic/jmri/decodernext/resources/errorIcon.png"; // NOI18N
}
