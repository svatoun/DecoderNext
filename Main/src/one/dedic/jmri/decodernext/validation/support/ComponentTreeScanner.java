/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.support;

import java.awt.Component;
import java.awt.Container;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.swing.JComponent;

/**
 *
 * @author sdedic
 */
public abstract class ComponentTreeScanner {
    private Set<JComponent> rescanFound = new HashSet<>();
    private Predicate<Component> acceptor = Objects::nonNull;
    private Set<JComponent> existingComponents = Collections.emptySet();
    
    public ComponentTreeScanner fromExisting(Set<JComponent> existing) {
        this.existingComponents = existing;
        return this;
    }
    
    public ComponentTreeScanner withAcceptor(Predicate<Component> acceptor) {
        this.acceptor = acceptor;
        return this;
    }
    
    public void scanComponentTree(Container parent, Predicate<JComponent> processor) {
        for (Component c : parent.getComponents()) {
            if (c == null) {
                continue;
            }
            if (!((c instanceof JComponent) && acceptor.test(c))) {
                continue;
            }
            JComponent jc = (JComponent)c;
            rescanFound.add(jc);
            if (existingComponents.contains(jc)) {
                continue;
            }
            if (processor.test(jc)) {
                continue;
            }
            if (c instanceof Container) {
                Container owner = (Container)c;
                if (owner.getComponentCount() > 0) {
                    scanComponents((Container)c);
                }
            }
        }
    }
    
    protected abstract void scanComponents(Container c);
    
    public Set<JComponent>  removedComponents() {
        Set<JComponent> notFound = new HashSet<>(existingComponents);
        notFound.removeAll(rescanFound);
        return notFound;
    }
}
