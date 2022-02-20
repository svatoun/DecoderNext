/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.model.formx.model;

import com.jgoodies.binding.value.ValueModel;
import java.util.function.Consumer;

/**
 *
 * @author sdedic
 */
public interface DelegateModel {
    public ValueModel getDelegate();
    
    public static <T> void apply(ValueModel m, Class<T> iface, Consumer<T> code) {
        if (iface.isInstance(m)) {
            code.accept((T)m);
        }
        if (m instanceof DelegateModel) {
            apply(((DelegateModel)m).getDelegate(), iface, code);
        }
    }
}
