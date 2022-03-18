/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.input;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import one.dedic.jmri.decodernext.forms.api.model.DataItem;
import one.dedic.jmri.decodernext.forms.api.model.EntryDescriptor;
import one.dedic.jmri.decodernext.forms.api.model.FormEntry;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sdedic
 */
public class InputContextUtils {
    private static ThreadLocal<InputContext>  threadContext = new ThreadLocal<>();
    private static ThreadLocal<ExecutionContext>  execContext = new ThreadLocal<>();
    
    public static class ExecutionContext {
        InputContext input;
        DataItem    data;
        FormEntry   entry;
        EntryDescriptor description;

        ExecutionContext(InputContext input, DataItem data, FormEntry entry) {
            this.input = input;
            this.data = data;
            this.entry = entry;
        }
        
        ExecutionContext copy() {
            return new ExecutionContext(input, data, entry);
        }
        
        public ExecutionContext description(EntryDescriptor d) {
            this.description = d;
            return this;
        }
        
        public ExecutionContext data(DataItem d) {
            this.data = d;
            return this;
        }
        
        public ExecutionContext entry(FormEntry e) {
            this.entry = e;
            return this;
        }
        
        public static EntryDescriptor findDescription() {
            ExecutionContext ee = execContext.get();
            return ee == null ? null : ee.description;
        }
    }
    
    public static ExecutionContext context(InputContext ctx) {
        ExecutionContext c = execContext.get();
        c.input = ctx;
        return c;
    }
    
    public <T, E extends Exception> T callWithInputContext(Callable<T> c, Class<E> clazz) throws E {
        InputContext oldC = threadContext.get();
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
        InputContext oldC = threadContext.get();
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
    public static void callUseInputContext(InputContext ctx, Object... arr) {
        if (arr == null) {
            return;
        }
        callUseInputContext(ctx, Arrays.asList(arr));
    }
    
    public static void callUseInputContext(InputContext ctx, Collection<Object> arr) {
        if (arr == null) {
            return;
        }
        for (Object o : arr) {
            if (o == null) {
                continue;
            }
            if (o instanceof one.dedic.jmri.decodernext.model.formx.model.InputContextAware) {
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
    
    public static InputContext  inputContext() {
        return threadContext.get();
    }
    
    public <T> CompletableFuture task2Future(RequestProcessor target, Callable<T> task) {
        AtomicReference<RequestProcessor.Task> refT = new AtomicReference<>();
        CompletableFuture<T> f = new CompletableFuture<T>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (isDone()) {
                    return false;
                }
                RequestProcessor.Task t = refT.get();
                if (t == null) {
                    return false;
                }
                if (t.cancel()) {
                    return super.cancel(mayInterruptIfRunning);
                }
                if (mayInterruptIfRunning && (task instanceof Cancellable)) {
                    if (!((Cancellable)task).cancel()) {
                        return super.cancel(true);
                    }
                }
                return super.cancel(mayInterruptIfRunning);
            }
            
        };
        RequestProcessor.Task rt = target.post(() -> {
            try {
                f.complete(task.call());
            } catch (ThreadDeath td) {
                throw td;
            } catch (Exception | Error t) {
                f.completeExceptionally(t);
            }
        });
        refT.set(rt);
        
        return f;
    }
}
