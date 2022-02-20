/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx;

import one.dedic.jmri.decodernext.model.formx.model.BufferedModel;
import com.jgoodies.binding.value.ValueModel;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public final class FormInputUtils {
    public static boolean isDirty(ValueModel vm) {
        if (vm instanceof BufferedModel) {
            return ((BufferedModel)vm).isDirty();
        } else if (vm instanceof Lookup.Provider) {
            BufferedModel bfm = ((Lookup.Provider)vm).getLookup().lookup(BufferedModel.class);
            if (bfm != null) {
                return bfm.isDirty();
            }
        }
        try {
            vm.getValue();
            return false;
        } catch (IllegalStateException ex) {
            return true;
        }
    }
}
