/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

/**
 *
 * @author sdedic
 */
public class ValidationConstants {
    /**
     * Marks a JLabel as a placeholder that should indicate validation result status.
     * Such label must have properly set the {@link javax.swing.JLabel#setLabelFor} reference,
     * so the validation infrastructure will connect it to the appropriate Validator result.
     */
    public static final String  COMPONENT_VALIDATION_ICON = "jmri.validationIcon";
    
    /**
     * Client property, which holds an instance of {@link Validator} for a Component.
     * The infrastructure will connect to the component's focus lost (or other appropriate
     * event) and will perform a validation.
     */
    public static final String  COMPONENT_VALIDATOR = "jmri.validator";
    
    /**
     * Declares a component a feedback area, and attaches a key.
     */
    public static final String  COMPONENT_FEEDBACK = "jmri.feedback.key";
    
    /**
     * Declares a feedback for a control
     */
    public static final String  COMPONENT_FEEDBACK_CONTROL = "jmri.feedback.control";
    
    /**
     * A key that identifies the component's validation messages.
     */
    public static final String  COMPONENT_VALIDATION_KEY = "jmri.validationKey";
    
    public static final String EMPTY_ICON = "one/dedic/jmri/decodernext/resources/blankIcon.png"; // NOI18N
    public static final String OK_ICON = "one/dedic/jmri/decodernext/resources/okIcon.png"; // NOI18N
    public static final String INFO_ICON = "one/dedic/jmri/decodernext/resources/infoIcon.png"; // NOI18N
    public static final String WARNING_ICON = "one/dedic/jmri/decodernext/resources/warningIcon.png"; // NOI18N
    public static final String ERROR_ICON = "one/dedic/jmri/decodernext/resources/errorIcon.png"; // NOI18N
}
