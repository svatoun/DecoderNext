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
    public static final String  COMPONENT_VALIDATION_ICON = "jmri.ValidationIcon";
    
    /**
     * Client property, which holds an instance of {@link Validator} for a Component.
     * The infrastructure will connect to the component's focus lost (or other appropriate
     * event) and will perform a validation.
     */
    public static final String  COMPONENT_VALIDATOR = "jmri.Validator";
    
    /**
     * A key that identifies the component's validation messages.
     */
    public static final String  COMPONENT_VALIDATION_KEY = "jmri.validationKey";
}
