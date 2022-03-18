/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.forms.api.swing;

import com.jgoodies.binding.value.ComponentModel;
import com.jgoodies.common.bean.Bean;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CompletableFuture;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import one.dedic.jmri.decodernext.forms.api.input.InputContext;
import one.dedic.jmri.decodernext.forms.api.input.InputContextAware;
import one.dedic.jmri.decodernext.forms.api.ui.ExComponentModel;
import org.openide.util.Lookup;

/**
 * A ComponentModel implementation for basic Swing components. Allows to control
 * appearance. Schedules all modifications to EDT, just in case.
 * 
 * @author sdedic
 */
public class BasicControlModel<C extends JComponent> extends Bean implements ExComponentModel, InputContextAware {
    private final C component;
    private final L compListener;
    private InputContext icontext;
    private Lookup lkp;

    private BasicControlModel(C component) {
        this.component = component;
        compListener = new L();
        component.addPropertyChangeListener(compListener);
        component.addComponentListener(compListener);
    }

    @Override
    public Lookup getLookup() {
        if (lkp != null) {
            return lkp;
        }
        synchronized (this) {
            if (lkp != null) {
                return lkp;
            }
            Object o = component.getClientProperty(SwingFormUtils.CLIENTPROP_COMPONENT_LOOKUP);
            if (o instanceof InputContextAware) {
                ((InputContextAware)o).withInputContext(icontext);
            }
            if (o instanceof Lookup.Provider) {
                lkp = ((Lookup.Provider)o).getLookup();
            } else if (o instanceof Lookup) {
                lkp = (Lookup)o;
            } else {
                lkp = Lookup.EMPTY;
            }
        }
        return lkp;
    }

    @Override
    public BasicControlModel withInputContext(InputContext ctx) {
        synchronized (this) {
            icontext = ctx;
        }
        return this;
    }
    
    public static <C extends JComponent> BasicControlModel<C> create(C component) {
        return new BasicControlModel<>(component);
    }
    
    public final C component() {
        return component;
    }

    @Override
    public boolean isEnabled() {
        return component.isEnabled();
    }

    @Override
    public boolean isDisplayed() {
        return component.isShowing();
    }

    @Override
    public void setEnabled(boolean e) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setEnabled(e));
            return;
        }
        component.setEnabled(e);
    }

    @Override
    public boolean isVisible() {
        return component.isVisible();
    }

    @Override
    public void setVisible(boolean v) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setVisible(v));
            return;
        }
        component.setVisible(v);
    }

    @Override
    public boolean isEditable() {
        if (component instanceof JTextComponent) {
            return ((JTextComponent)component).isEditable();
        } else if (component instanceof JComboBox) {
            return ((JComboBox)component).isEditable();
        } else {
            return component.isEnabled();
        }
    }

    @Override
    public void setEditable(boolean e) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setEditable(e));
            return;
        }
        if (component instanceof JTextComponent) {
            ((JTextComponent)component).setEditable(e);
        } else if (component instanceof JComboBox) {
            ((JComboBox)component).setEditable(e);
        } else {
            component.setEnabled(e);
        }
    }
    
    protected boolean propagateEnabled(Boolean old, Boolean n) {
        if ((component instanceof JTextComponent) ||
            (component instanceof JComboBox)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isActive() {
        return component.isShowing() && component.isFocusOwner();
    }

    @Override
    public CompletableFuture<JComponent> reveal(boolean activate) {
        if (!activate || !component.isRequestFocusEnabled()) {
            return CompletableFuture.completedFuture(component);
        }
        CompletableFuture<JComponent> res = new CompletableFuture<>();
        SwingUtilities.invokeLater(() -> {
            component.requestFocusInWindow();
            res.complete(component);
        });
        return res;
    }
    
    class L implements PropertyChangeListener, ComponentListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null) {
                firePropertyChange(null, evt.getOldValue(), evt.getNewValue());
            } else switch (evt.getPropertyName()) {
                case "enabled":
                    if (!propagateEnabled((Boolean)evt.getOldValue(), (Boolean)evt.getNewValue())) {
                        firePropertyChange("editable", evt.getOldValue(), evt.getNewValue());
                    }
                    // fall through
                case "editable":
                case "visible":
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        }

        @Override
        public void componentResized(ComponentEvent e) {
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
            firePropertyChange(ComponentModel.PROPERTY_VISIBLE, false, true);
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            firePropertyChange(ComponentModel.PROPERTY_VISIBLE, true, false);
        }
    }
    
}
