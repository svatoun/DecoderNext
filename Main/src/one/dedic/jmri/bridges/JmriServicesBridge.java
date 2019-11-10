/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.bridges;

import java.util.function.Supplier;
import jmri.jmrit.roster.Roster;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Bridges the JMRI services into the default Lookup. JMRI starts quite long,
 * so the bridge waits until JMRI fully activates, then it publishes the services.
 * @author sdedic
 */
@ServiceProvider(service = JmriServicesBridge.class)
public class JmriServicesBridge extends ProxyLookup implements Runnable {
    private final InstanceContent ic = new InstanceContent();
    
    public JmriServicesBridge() {
//        AppsBaseBridge.getInstance().whenReady(this, false);
    }
    
    public static Lookup getServiceLookup() {
        return Lookup.getDefault().lookup(JmriServicesBridge.class);
    }
    
    static JmriServicesBridge getInstance() {
        return (JmriServicesBridge)getServiceLookup();
    }

    @Override
    protected void beforeLookup(Template<?> template) {
        super.beforeLookup(template);
    }

    @Override
    public void run() {
        addInstance(Roster.class, "Roster", Roster::getDefault);
        
        // atomically introduce
        setLookups(new AbstractLookup(ic));
    }
    
    private <T> void addInstance(Class<T> clazz, String name, Supplier<T> factory) {
        ic.add(clazz, new TypeConvertor<>(factory, name));
    }
    
    private static class  TypeConvertor<T> implements InstanceContent.Convertor<Class<T>, T> {
        private final Supplier<T> factory;
        private final String dispName;

        public TypeConvertor(Supplier<T> factory, String dispName) {
            this.factory = factory;
            this.dispName = dispName;
        }
        
        @Override
        public T convert(Class<T> arg0) {
            return factory.get();
        }

        @Override
        public Class<? extends T> type(Class<T> arg0) {
            return arg0;
        }

        @Override
        public String id(Class<T> arg0) {
            return arg0.getName();
        }

        @Override
        public String displayName(Class<T> arg0) {
            return dispName;
        }
    }   
}
