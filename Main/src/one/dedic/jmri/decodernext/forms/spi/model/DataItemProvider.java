/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.spi.model;

import java.util.Collection;
import one.dedic.jmri.decodernext.forms.api.input.InputContext;
import one.dedic.jmri.decodernext.forms.api.model.DataItem;

/**
 *
 * @author sdedic
 */
public interface DataItemProvider {
    public Collection<? extends DataItem>   createData(InputContext ctx);
}
