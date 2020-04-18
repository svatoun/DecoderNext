/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.bridges;

import jmri.managers.DefaultShutDownManager;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class NetBeansShutdownManager extends DefaultShutDownManager {
    private boolean doShutdown;
    
    public NetBeansShutdownManager() {
    }

    @Override
    protected boolean shutdown(int status, boolean exit) {
        if (!exit && !doShutdown) {
            return true;
        }
        if (!super.shutdown(status, exit)) {
            return false;
        }
        Lookup.getDefault().lookup(LifecycleManager.class).exit(status);
        return true;
    }
    
    
    public void netbeansShutdown() {
        try {
            doShutdown = true;
            shutdown(0, false);
        } finally {
            doShutdown = false;
        }
    }
}
