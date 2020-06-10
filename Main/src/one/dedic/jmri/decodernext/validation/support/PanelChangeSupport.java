/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.support;

import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.swing.JComponent;
import one.dedic.jmri.decodernext.validation.SwingAttached;
import one.dedic.jmri.decodernext.validation.ValidationConstants;
import one.dedic.jmri.decodernext.validation.ValidationContainer;
import one.dedic.jmri.decodernext.validation.data.ControlChangeMonitor;
import static one.dedic.jmri.decodernext.validation.support.PanelValidationSupport.ALL;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sdedic
 */
public class PanelChangeSupport implements SwingAttached, ValidationContainer<ControlChangeMonitor> {
    private static final Logger LOG = LoggerFactory.getLogger(PanelValidationSupport.class);

    private final JComponent    panel;
    private final Map<JComponent, ControlChangeMonitor> monitors = new HashMap<>();
    
    private Predicate<Component>  acceptor = ALL;

    public PanelChangeSupport(JComponent panel) {
        this.panel = panel;
    }
    
    public Predicate<Component> getAcceptor() {
        return acceptor;
    }

    public void setAcceptor(Predicate<Component> acceptor) {
        this.acceptor = acceptor;
    }
    
    public void build() {
        ComponentTreeScanner scanner = new ComponentTreeScanner() {
            @Override
            protected void scanComponents(Container parent) {
                LOG.debug("Scanning for changeable components in {}", parent);
                scanComponentTree(parent, (c) -> {
                    LOG.debug("Inspecting: {}", c);
                    return attachChangeSupport(c);
                });
            }
        };
        scanner.fromExisting(monitors.keySet());
    }
    
    protected boolean attachChangeSupport(JComponent c) {
        Object o = c.getClientProperty(ValidationConstants.CONTROL_CHANGE_SUPPORT);
        if (o instanceof ControlChangeMonitor) {
            attachChild(c, (ControlChangeMonitor)o);
            return true;
        }
        o = c.getClientProperty(ValidationConstants.CONTROL_CHANGE_MARK);
        if (o == null) {
            return false;
        }
        ControlChangeMonitor mon = DefaultControlChangeMonitor.attachChangeMonitor(c, Lookup.EMPTY);
        if (mon == null) {
            return false;
        }
        attachChild(c, mon);
        return true;
    }
    
    private boolean active;

    @Override
    public void addNotify() {
        if (active) {
            return;
        }
        active = true;
        monitors.values().stream().map(
            m -> (SwingAttached)(m instanceof SwingAttached ? m : null)).
            filter(Objects::nonNull).
            forEach(SwingAttached::addNotify);
    }

    @Override
    public void removeNotify() {
        if (!active) {
            return;
        }
        active = false;
        monitors.values().stream().map(
            m -> (SwingAttached)(m instanceof SwingAttached ? m : null)).
            filter(Objects::nonNull).
            forEach(SwingAttached::removeNotify);
    }

    @Override
    public void attachChild(JComponent ui, ControlChangeMonitor child) {
        if (child == null) {
            return;
        }
        if (monitors.put(ui, child) != null) {
            return;
        }
        if (active && (child instanceof SwingAttached)) {
            ((SwingAttached)child).addNotify();
        }
    }

    @Override
    public void dettachChild(JComponent ui, ControlChangeMonitor child) {
        if (monitors.remove(ui) == null) {
            return;
        }
        if (active && (child instanceof SwingAttached)) {
            ((SwingAttached)child).removeNotify();
        }
    }
    
}
