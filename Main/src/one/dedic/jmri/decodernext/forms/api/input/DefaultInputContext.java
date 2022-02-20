/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.input;

import one.dedic.jmri.decodernext.model.formx.model.*;
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
import java.util.function.Supplier;
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
public final class DefaultInputContext<T> {
    /**
     * Services provided to UI/data in the context.
     */
    private final Lookup  services;
    
    /**
     * The main edited bean.
     */
    private final T masterBean;
    
    private List<NamedBeanLocator> namedLocators = new ArrayList<>();
    private Map<String, Object> namedBeans = new HashMap<>();
    
    private Executor    dataExecutor;
    private Executor    validationExecutor;
    
    /**
     * Set of disabled items
     */
    private Set<String> disabledItems = new HashSet<>();

    public DefaultInputContext(T masterBean, Lookup services) {
        this.services = services;
        this.masterBean = masterBean;
    }
    
    public boolean isEnabled(DataItem item) {
        return !(
            disabledItems.contains(item.id()) ||
            disabledItems.contains(item.getDescriptor().getName())
            
        );
    }
    
    public <T> T lookup(Class<T> clazz) {
        return services.lookup(clazz);
    }

    /**
     * The main model for the input. The main model cannot be {@code null}.
     * @return the main model instance.
     */
    public T getMasterBean() {
        return masterBean;
    }
    
    public void addNamedBean(String id, Object bean) {
        namedBeans.put(id, bean);
    }
    
    public <T> T findNamedBean(String id) {
        Object o = namedBeans.get(id);
        if (o != null) {
            return (T)o;
        }
        for (NamedBeanLocator l : namedLocators) {
            o = l.findNamedBean(this);
            if (o != null) {
                namedBeans.put(id, o);
                return (T)o;
            }
        }
        return null;
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
     * Calls {@link InputContextAware#useInputContext} on passed instances. If the instance 
     * is a {@link InputContextAware}, calls its {@link InputContextAware#useInputContext} directly.
     * Otherwise, if the instance is a {@link Lookup} or a {@link Lookup.Provider}, looks up all
     * {@link InputContextAware} instances in it and calls {@link InputContextAware#useInputContext} on them.
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
                ((InputContextAware)o).useInputContext(ctx);
            } else {
                Lookup l;

                if (o instanceof Lookup) {
                    l = (Lookup)o;
                } else if (o instanceof Lookup.Provider) {
                    l = ((Lookup.Provider)o).getLookup();
                } else {
                    return;
                }
                l.lookupAll(InputContextAware.class).forEach(a -> a.useInputContext(ctx));
            }
        }
    }
}
