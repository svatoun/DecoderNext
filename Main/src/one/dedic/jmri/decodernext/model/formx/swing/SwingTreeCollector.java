/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/**
 * Collects data from Swing component tree, then makes updates old/new ones.
 * 
 * @author sdedic
 */
public class SwingTreeCollector<T> {
    private static final RequestProcessor RESYNC_SCHED = new RequestProcessor(SwingTreeCollector.class);
    private static final int DEFAULT_DELAY = 50;
    
    protected final JComponent rootComponent;
    private final L contL = new L();
    private final RequestProcessor.Task sched = RESYNC_SCHED.create(contL);
    
    private volatile boolean refreshing;
    private List<TreeCollectorListener<T>> listeners;
    private List<T> current = new ArrayList<>();
    private Class<T> searchFor;

    public SwingTreeCollector(JComponent rootComponent, Class<T> clazz) {
        this.rootComponent = rootComponent;
        this.searchFor = clazz;
        rootComponent.addContainerListener(contL);
    }
    
    public final List<T> getItems() {
        synchronized (this) {
            return new ArrayList<>(current);
        }
    }
    
    private void refresh(int time) {
        rootComponent.repaint();
        synchronized (this) {
            if (refreshing) {
                return;
            }
            refreshing = true;
            sched.schedule(time);
        }
    }
    
    class L implements ContainerListener, Runnable {
        void addOrRemoveListeners(boolean add, Container c) {
            c.removeContainerListener(this);
            Arrays.asList(c.getComponents()).stream().
                filter(Container.class::isInstance).
                forEach(i -> addOrRemoveListeners(add, (Container)i));
            if (add) {
                c.addContainerListener(this);
            }
        }
        
        @Override
        public void componentAdded(ContainerEvent e) {
            Component c = e.getChild();
            if (c instanceof Container) {
                addOrRemoveListeners(true, (Container)e.getChild());
            }
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            Component c = e.getChild();
            if (c instanceof Container) {
                addOrRemoveListeners(false, (Container)e.getChild());
            }
        }

        @Override
        public void run() {
            Mutex.EVENT.writeAccess(SwingTreeCollector.this::resync);
        }
    }
    
    private void resync() {
        Refresh r = new Refresh();
        r.inspect(rootComponent);
        r.processUpdates();
    }
    
    public Class<T> searchFor() {
        return searchFor;
    }
    
    public void addTreeCollectorListener(TreeCollectorListener<T> listener) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ArrayList<>();
            }
            listeners.add(listener);
        }
    }

    public void removeTreeCollectorListener(TreeCollectorListener<T> listener) {
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            listeners.remove(listener);
        }
    }
    
    protected T extract(JComponent c) {
        if (searchFor == null) {
            return null;
        }
        if (searchFor.isInstance(c)) {
            return searchFor.cast(c);
        } 
        Lookup l = SwingFormUtils.getComponentLookup(c);
        return l != null ? l.lookup(searchFor) : null;
    }
    
    protected void notifyAdded(Collection<T> additions) {
    }
    
    protected void notifyRemoved(Collection<T> removals) {
    }
    
    private void updateAndFire(Refresh r, Collection<T> prev, Collection<T> next) {
        List<TreeCollectorListener<T>> ll;
        synchronized (this) {
            current = r.items;
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            ll = new ArrayList<>(listeners);
        }
        TreeCollectorEvent<T> e = new TreeCollectorEvent(this, next, prev, r.items);
        if (!prev.isEmpty()) {
            ll.forEach(l -> l.itemsRemoved(e));
        }
        if (!next.isEmpty()) {
            ll.forEach(l -> l.itemsAdded(e));
        }
        if (!next.isEmpty()) {
            ll.forEach(l -> l.itemsChanged(e));
        }
    }
    
    private class Refresh {
        private List<T> items = new ArrayList<>();
        
        private boolean maybeAdd(T inst) {
            if (inst != null) {
                items.add(inst);
                return false;
            } else {
                return true;
            }
        }
        
        private void inspect(JComponent c) {
            if (maybeAdd(extract(c))) {
                if (c instanceof Container) {
                    Arrays.asList(((Container)c).getComponents()).stream().
                        filter(i -> i instanceof JComponent).
                            forEach(i -> inspect((JComponent)i));
                }
            }
        }
        
        private void processUpdates() {
            Map<T, T> prev = new IdentityHashMap<T, T>();
            Map<T, T> next = new IdentityHashMap<T, T>();
            
            current.forEach(i -> prev.put(i, i));
            items.forEach(i -> next.put(i, i));
            
            prev.keySet().removeAll(items);
            next.keySet().removeAll(current);
            
            notifyRemoved(prev.keySet());
            notifyAdded(next.keySet());
            
            updateAndFire(this, prev.keySet(), next.keySet());
        }
    }
    
    public static class ClientProperty<T> extends SwingTreeCollector<T> {
        private final String propertyName;

        public ClientProperty(String propertyName, Class<T> clazz, JComponent rootComponent) {
            super(rootComponent, clazz);
            this.propertyName = propertyName;
        }
        
        protected T convertOrNull(Object o) {
            Class<T> t = searchFor();
            if (t == null) {
                return (T)o;
            }
            return t.cast(o);
        }

        @Override
        protected T extract(JComponent c) {
            T instance = super.extract(c);
            if (instance != null) {
                return instance;
            }
            if (propertyName != null) {
                return convertOrNull(c.getClientProperty(propertyName));
            }
            return null;
        }
    }
}
