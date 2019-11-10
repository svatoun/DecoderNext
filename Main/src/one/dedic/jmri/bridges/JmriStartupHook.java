/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.bridges;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import org.openide.modules.OnStart;

/**
 * Hooks JMRI shutdown to the NetBeans module system shutdown.
 * 
 * @author sdedic
 */
@OnStart
public class JmriStartupHook implements Runnable {
    @Override
    public void run() {
        InstanceManager.store(new JmriDefaultShutDownManager(), ShutDownManager.class);
    }
}
