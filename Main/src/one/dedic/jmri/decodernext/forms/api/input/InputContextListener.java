/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.input;

import one.dedic.jmri.decodernext.model.formx.model.*;
import java.util.EventListener;

/**
 *
 * @author sdedic
 */
public interface InputContextListener extends EventListener {
    public void dataItemsAdded(InputContextEvent e);
    public void dataItemsRemoved(InputContextEvent e);
}
