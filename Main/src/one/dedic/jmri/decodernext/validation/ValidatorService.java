/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

import com.jgoodies.validation.ValidationResult;
import java.awt.Component;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;

/**
 *
 * @author sdedic
 */
public interface ValidatorService {
    /**
     * Adds a change listener. The method may be called from any thread, but
     * the listener will be always invoked in the Swing EDT.
     * 
     * @param l listener
     */
    public void addChangeListener(ChangeListener l);
    public void removeChangeListener(ChangeListener l);
    
    /**
     * Obtains the result of last validation. May be called from an arbitrary thread.
     * @return most recent validation result.
     */
    @Nonnull
    public ValidationResult getValidation();
    
    /**
     * Performs validation. The validation is done even if the current result is up-to-date.
     * The method must be called in EDT.
     * @return 
     */
    @Nonnull
    public ValidationResult validate();
    
    /**
     * Attempts to find a component for the validation message key. The method returns
     * null, if it cannot find the component. If {@code null} is passed, the service
     * returns the Component it attaches to.
     * @param key key obtained from {@link ValidationMessage#key}.
     * @return component or {@code null} if not known.
     */
    @CheckReturnValue
    public Component findComponent(Object key);
    
    /**
     * Returns an indicator which can be used to represent
     * results of a validation.
     * @return indicator Component
     */
    public JLabel getIndicator();
}
