/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import java.util.function.Predicate;
import javax.swing.JComponent;

/**
 * Service to extract a value from a bean to be passed to a {@link Validator}. This
 * additional indirection layer allows to use the same type of Validator to validate different
 * properties of a bean (to enforce the same semantic). Each instance will be accompanied by
 * instance of ValidateValue that will fetch the actual value to validate.
 * 
 * @author sdedic
 */
public interface ValidatedValue<T> extends Predicate {
    public T getValue(JComponent ui);
}
