/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.model;

import com.jgoodies.binding.value.ValueModel;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author sdedic
 */
public class ModelUtilities {
    public static EntryDescriptor fromProperty(String beanId, PropertyDescriptor pd) {
        EntryDescriptor d = new EntryDescriptor();
        d.setName(beanId + "." + pd.getName());
        d.setDisplayName(pd.getDisplayName());
        
        for (String s : Collections.list(pd.attributeNames())) {
            d.setValue(s, pd.getValue(s));
        }
        Object o = pd.getValue("iconBase");
        if (o instanceof String) {
            d.setIconBase(o.toString());
        }
        d.setExpert(pd.isExpert());
        d.setHidden(pd.isHidden());
        d.setPreferred(pd.isPreferred());
        d.setShortDescription(pd.getShortDescription());
        d.setValueClass(pd.getPropertyType());
        return d;
    }
    
    private static Object transform(Object v, TransformModel xform) {
        if (v instanceof SpecialValue) {
            SpecialValue sv = (SpecialValue)v;
            if (sv.isCommon()) {
                return SpecialValue.common(xform.transform(sv.getCommonValue()));
            } else {
                return v;
            }
        } else {
            return xform.transform(v);
        }
    }
    
    public static Object getPendingValue(ValueModel model) {
        if (model == null) {
            return null;
        }
        if (model instanceof BufferedModel) {
            return ((BufferedModel)model).getPendingValue();
        } else if (model instanceof DelegateModel) {
            ValueModel d = ((DelegateModel)model).getDelegate();
            if (model instanceof TransformModel) {
                return transform(getPendingValue(d), (TransformModel)model);
            } else {
                return getPendingValue(d);
            }
        } else {
            return model.getValue();
        }
    }

    /**
     * Helper method to get target value from a {@link ValueModel}.
     * @param del
     * @return 
     */
    public static CompletableFuture<Object> getTargetValue(ValueModel del, boolean returnSpecial) {
        if (del == null) {
            return null;
        }
        if (del instanceof BufferedModel) {
            return ((BufferedModel)del).getTargetValue(returnSpecial);
        } else if (del instanceof DelegateModel) {
            if (del instanceof TransformModel) {
                return getTargetValue(((DelegateModel)del).getDelegate(), returnSpecial).thenApply(
                    v -> transform(v, (TransformModel)del)
                );
            } else {
                return getTargetValue(((DelegateModel)del).getDelegate(), returnSpecial);
            }
        } else {    
            CompletableFuture<Object> res = new CompletableFuture<>();
            try {
                res.complete(del.getValue());
            } catch (IllegalArgumentException | IllegalStateException ex) {
                res.completeExceptionally(ex);
            }
            return res;
        }
    }
    
    public static <T, U> U applyDelegates2(ValueModel m, Class<T> iface, Function<T, U> code, U defV) {
        if (iface.isInstance(m)) {
            return code.apply((T)m);
        }
        if (m instanceof DelegateModel) {
            return applyDelegates2(((DelegateModel)m).getDelegate(), iface, code, defV);
        } else {
            return defV;
        }
    }
    
    public static <T> void applyDelegates(ValueModel m, Class<T> iface, Consumer<T> code, boolean first) {
        if (iface.isInstance(m)) {
            code.accept((T)m);
            if (first) {
                return;
            }
        }
        if (m instanceof DelegateModel) {
            applyDelegates(((DelegateModel)m).getDelegate(), iface, code, first);
        }
    }
    
    public static Lookup getLookup(ValueModel m) {
        List<Object> fixed = new ArrayList<>();
        Lookup found = null;
        for (ValueModel c = m; c != null; ) {
            fixed.add(c);
            if (c instanceof Lookup) {
                found = (Lookup)c;
            } else if (c instanceof Lookup.Provider) {
                found = ((Lookup.Provider)c).getLookup();
            }
            if (c instanceof DelegateModel) {
                c = ((DelegateModel)c).getDelegate();
            } else {
                break;
            }
        }
        fixed.remove(found);
        if (found == null) {
            return fixed.isEmpty() ? Lookup.EMPTY : Lookups.fixed(fixed);
        } else {
            return fixed.isEmpty() ? found : new ProxyLookup(Lookups.fixed(fixed), found);
        }
    }
    
    public static <T> Iterator<? extends T> delegates(ValueModel m, Class<T> iface) {
        Iterator<? extends T> it = new Iterator<T>() {
            T nextItem;
            ValueModel model = m;
            T prepare() {
                if (nextItem != null) {
                    return nextItem;
                }
                while (model != null) {
                    if (iface.isInstance(model)) {
                        nextItem = iface.cast(model);
                        if (model instanceof DelegateModel) {
                            model = ((DelegateModel)model).getDelegate();
                        }
                        break;
                    } else if (model instanceof DelegateModel) {
                        model = ((DelegateModel)model).getDelegate();
                    } else {
                        model = null;
                    }
                }
                return nextItem;
            }
            
            @Override
            public boolean hasNext() {
                return prepare() != null;
            }

            @Override
            public T next() {
                T r = prepare();
                if (r == null) {
                    throw new NoSuchElementException();
                }
                nextItem = null;
                return r;
            }
        };
        return it;
    }
    
    public static <T> Stream<T> streamDelegates(ValueModel m, Class<T> iface) {
        Spliterator<T> split = Spliterators.spliteratorUnknownSize(delegates(m, iface), Spliterator.ORDERED);
        return StreamSupport.stream(split, false);
    }
    
    public static EntryDescriptor getModelDecription(ValueModel m) {
        return streamDelegates(m, HasDescription.class).map(f -> f.getDescriptor()).findFirst().orElse(null);
    }
}
