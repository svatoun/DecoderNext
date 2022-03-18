/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.model;

import com.jgoodies.binding.value.ValueModel;

/**
 * Mixin interface on a model that can provide special values outside
 * the normal type range. For example if the model represents multiple different
 * int values, it cannot provide them from its {@link ModelValue#getValue} - that
 * method must return {@code null} as if the value was not set at all.
 * <p>
 * The model can eventually signal that its value is actually a common value of multiple
 * underlying sources - and certain inputs may choose to indicate such case visually.
 * <p>
 * It is not permitted to use SpecialValue whose {@link SpecialValue#isValid} = false
 * to set a value to a model. The model implementation should throw a {@link DataInputException}.
 * @author sdedic
 */
public interface HasSpiecialValue extends ValueModel {
    /**
     * Returns a special value description. Returns {@code null} if the value is regular
     * and should be read by {@link ValueModel#getValue()}. Should return {@link SpecialValue#ERRONEOUS}
     * if {@link ValueModel#getValue} throws {@link DataInputException}.
     * 
     * @return special value or {@code null}.
     */
    public SpecialValue getSpecialValue();
    
    public void setSpecialValue(SpecialValue specV);
}
