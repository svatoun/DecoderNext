/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import one.dedic.jmri.decodernext.forms.api.model.DataItem;
import one.dedic.jmri.decodernext.forms.api.model.FormEntry;
import one.dedic.jmri.decodernext.forms.spi.model.NamedBeanLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Represents complete context for the user input. The object is used to configure validators,
 * converters, user input models that use this information to connect to other values.
 * 
 * @author sdedic
 */
public final class DefaultInputContext implements InputContext {
    /**
     * Services provided to UI/data in the context.
     */
    private final Lookup  services;
    
    /**
     * The main edited bean.
     */
    private final Object masterBean;
    
    private List<NamedBeanLocator> namedLocators = new ArrayList<>();
    private Map<String, Object> namedBeans = new HashMap<>();
    private Map<String, Set<String>> aliases = new HashMap<>();
    private Map<String, FormEntry> entries = new HashMap<>();
    
    private Executor    dataExecutor;
    private Executor    validationExecutor;
    
    /**
     * Set of disabled items
     */
    private Set<String> disabledItems = new HashSet<>();
    
    private List<InputContextListener> listeners = new ArrayList<>();

    public DefaultInputContext(Object masterBean, Lookup services) {
        this.services = services;
        this.masterBean = masterBean;
    }

    @Override
    public void addObject(String id, Object subject, String... alternativeIds) {
        synchronized (this) {
            namedBeans.put(id, subject);
            addObjectAlias(id, alternativeIds);
        }
    }

    @Override
    public void addObjectAlias(String id, String... aliases) {
        synchronized (this) {
            this.aliases.computeIfAbsent(id, x -> new HashSet<>()).addAll(Arrays.asList(aliases));
        }
    }

    @Override
    public void addInputContextListener(InputContextListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeInputContextListener(InputContextListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean isEnabled(DataItem item) {
        return !(disabledItems.contains(item.getDescriptor().getName()));
    }
    
    public <T> T lookup(Class<T> clazz) {
        return services.lookup(clazz);
    }

    /**
     * The main model for the input. The main model cannot be {@code null}.
     * @return the main model instance.
     */
    @Override
    public Object getSubject() {
        return masterBean;
    }
    
    public void addNamedBean(String id, Object bean) {
        namedBeans.put(id, bean);
    }

    @Override
    public Lookup getLookup() {
        return services;
    }
    
    @Override
    public <T> T findObject(String id) {
        Object o = namedBeans.get(id);
        if (o != null) {
            return (T)o;
        }
        for (NamedBeanLocator l : namedLocators) {
            o = l.findNamedBean(id, this);
            if (o != null) {
                namedBeans.put(id, o);
                return (T)o;
            }
        }
        return null;
    }

    @Override
    public Executor getValidationExecutor() {
        return validationExecutor;
    }

    public final static class Control<T> {
        private DefaultInputContext        context;
        private InstanceContent     instances = new InstanceContent();

        public Control(Lookup lkp, T masterBean) {
            context = new DefaultInputContext(masterBean, new AbstractLookup(instances));
        }
        
        public void include(Object instance) {
            instances.add(instance);
        }
        
        public DefaultInputContext contextInstance() {
            return context;
        }
        
        public void addNamedBean(String id, Object bean) {
            context.namedBeans.put(id, bean);
        }
        
        public void addLocator(NamedBeanLocator locator) {
            context.namedLocators.add(locator);
        }
    }
    
    private static ThreadLocal<DefaultInputContext>  threadContext = new ThreadLocal<>();
    
    public <T, E extends Exception> T callWithInputContext(Callable<T> c, Class<E> clazz) throws E {
        DefaultInputContext oldC = threadContext.get();
        try {
            return c.call();
        } catch (Exception ex) {
            if (clazz.isInstance(ex)) {
                throw (E)ex;
            }
            throw new CompletionException(ex);
        } finally {
            threadContext.set(oldC);
        }
    }

    public <T> T callWithInputContext(Supplier<T> c) {
        DefaultInputContext oldC = threadContext.get();
        try {
            return c.get();
        } finally {
            threadContext.set(oldC);
        }
    }
    
    /**
     * Calls {@link InputContextAware#withInputContext} on passed instances. If the instance 
     * is a {@link InputContextAware}, calls its {@link InputContextAware#withInputContext} directly.
     * Otherwise, if the instance is a {@link Lookup} or a {@link Lookup.Provider}, looks up all
     * {@link InputContextAware} instances in it and calls {@link InputContextAware#withInputContext} on them.
     * @param ctx
     * @param arr 
     */
    public static void callUseInputContext(DefaultInputContext ctx, Object... arr) {
        if (arr == null) {
            return;
        }
        callUseInputContext(ctx, Arrays.asList(arr));
    }
    
    public static void callUseInputContext(DefaultInputContext ctx, Collection<Object> arr) {
        if (arr == null) {
            return;
        }
        for (Object o : arr) {
            if (o == null) {
                continue;
            }
            if (o instanceof InputContextAware) {
                ((InputContextAware)o).withInputContext(ctx);
            } else {
                Lookup l;

                if (o instanceof Lookup) {
                    l = (Lookup)o;
                } else if (o instanceof Lookup.Provider) {
                    l = ((Lookup.Provider)o).getLookup();
                } else {
                    return;
                }
                l.lookupAll(InputContextAware.class).forEach(a -> a.withInputContext(ctx));
            }
        }
    }
}
