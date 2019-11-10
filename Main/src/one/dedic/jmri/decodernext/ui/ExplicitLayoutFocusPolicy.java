/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import javax.swing.LayoutFocusTraversalPolicy;

/**
 * A derivation of the default layout policy, which respect defined component order. 
 * Usable with GroupLayout (free form layout), which influence the container order
 * of the individual controls.
 * <p/>
 * When constructed, this FocusPolicy accepts a ordered list of Components.
 * When the focus is about to move from one registered component to another, the
 * Policy makes sure, it will move to the immediately following component, or
 * will cycle using {@link #getFirstComponent} / {@link #getLastComponent}.
 * 
 * @author sdedic
 */
public final class ExplicitLayoutFocusPolicy extends LayoutFocusTraversalPolicy {
    
    private final Container panel;
    private final List<Component> order;

    public ExplicitLayoutFocusPolicy(Container panel, List<Component> order) {
        this.panel = panel;
        this.order = order;
    }

    private Component findMainComponent(Component c) {
        if (c == null) {
            return null;
        }
        int i = order.indexOf(c);
        if (i != -1) {
            return c;
        }
        if (c.getParent() != panel && c.getParent() != null) {
            return findMainComponent(c.getParent());
        }
        return null;
    }

    @Override
    public Component getComponentBefore(Container aContainer, Component aComponent) {
        Component c = super.getComponentBefore(aContainer, aComponent);
        Component mOld = findMainComponent(aComponent);
        Component mNew = findMainComponent(c);
        if (mOld != null && mNew != null && mOld != mNew) {
            int index = order.indexOf(mOld);
            if (index > 0) {
                return order.get(index - 1);
            } else {
                return getLastComponent(aContainer);
            }
        }
        return c;
    }

    @Override
    public Component getComponentAfter(Container aContainer, Component aComponent) {
        Component c = super.getComponentAfter(aContainer, aComponent);
        Component mOld = findMainComponent(aComponent);
        Component mNew = findMainComponent(c);
        if (mOld != null && mNew != null && mOld != mNew) {
            int index = order.indexOf(mOld);
            if (index < order.size() - 1) {
                return order.get(index + 1);
            } else {
                return getFirstComponent(aContainer);
            }
        }
        return c;
    }
    
}
