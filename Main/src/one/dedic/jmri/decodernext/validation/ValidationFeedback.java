/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.ValidationResult;
import java.util.Collection;
import javax.swing.Action;

/**
 * Implement this interface to provide feedback for individual UI
 * components.
 * <p/>
 * ValidationFeedback instances are collected from components in the UI. In this
 * respect, the UI tree is just used as a communication bus; there may be no functional relationship
 * between ValidationFeedback instance and the Component where it is set. For Components that
 * support validation, but do NOT declare {@link #CLIENT_VALIDATION_FEEDBACK}, default feedback
 * can be constructed.
 * <p/>
 * Validators and ValidationFeedback are loosely coupled by {@link ValidationMessage#key} and 
 * {@link #getKeys}: a {@link ValidationMessage} with a specific key is presented by that ValidationFeedback,
 * which lists an equal key.
 * <p/>
 * The feedback can be asked to:
 * <ul>
 * <li>indicate the {@link ValidationResult}. It should present the most severe state from the result. Optionally
 * it might pop up a bubble, or some other temporary UI.
 * <li>report the message. If there's a permanent place where the message can appear for the user, the Feedback should
 * print it. If there's no support, it must return {@code false} indicating the message should appear elsewhere.
 * <li>transfer control to the component. This control transfer is passed as {@link Action}, so that UI can present
 * a name, or icon as an active place the user can click on.
 * </ul>
 * @author sdedic
 */
public interface ValidationFeedback extends SwingAttached {
    /**
     * Name of client property, which holds the {@link ValidationFeedback} instance.
     */
    public static final String CLIENT_VALIDATION_FEEDBACK = "jmri.validationFeedback";
    
    /**
     * Returns keys which a component corresponds to.
     * @return collection of keys (identifiers).
     */
    public Collection<Object> getKeys();
    
    /**
     * Should display overall indication of validation result. The indication should
     * be subtle, like just an icon or flash, with a more descriptive message displayed
     * on demand, i.e. in a tooltip.
     * 
     * @param result result to indicate.
     */
    public void indicateResult(ValidationResult result);
    
    /**
     * Displays entire validation feedback. The implementation returns false if it
     * has no capacity to display the feedback.
     * @param result result to display
     */
    public boolean reportMessages(ValidationResult result);
    
    /**
     * Returns display name of the feedback object.
     * @return 
     */
    public Action transferControl(Object key);
}
