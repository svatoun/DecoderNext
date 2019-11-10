/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.bridges;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;

/**
 * Hooks JMRI shutdown to the NetBeans module system shutdown.
 * 
 * @author sdedic
 */
@OnStop
public class JmriShutdownHook implements Runnable {
    @Override
    public void run() {
        try {
            SwingUtilities.invokeAndWait(this::shutdownJmri);
        } catch (InterruptedException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    void shutdownJmri() {
        /*
        ((NetBeansShutdownManager)InstanceManager.getDefault(ShutDownManager.class)).netbeansShutdown();
        */
        ShutDownManager mgr = InstanceManager.getDefault(ShutDownManager.class);
        if (mgr instanceof JmriDefaultShutDownManager) {
            ((JmriDefaultShutDownManager)mgr).realShutdown();
        }
    }
}
