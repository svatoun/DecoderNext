/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.model;

import com.jgoodies.binding.value.ValueModel;

/**
 *
 * @author sdedic
 */
public interface DelegateModel extends ValueModel {
    public boolean    isDirty();
    public ValueModel getDelegate();
}
