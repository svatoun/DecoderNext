/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.model;

import com.jgoodies.binding.value.ValueModel;

/**
 *
 * @author sdedic
 */
public interface TypedValueModel<T> extends ValueModel {
    public Class<T> valueClass();
}
