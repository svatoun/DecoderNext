/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.swing;

import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationResult;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import one.dedic.jmri.decodernext.validation.ValidationConstants;
import one.dedic.jmri.decodernext.validation.ValidationFeedback;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class DefaultIconFeedback implements ValidationFeedback, ContextValidator {
    private final JLabel label;
    private final Set<Object> keys;
    private JComponent target;
    private String componentName;
    private Severity lastSeverity;

    private Timer fadeTimer;

    public DefaultIconFeedback(JLabel iconPlaceholder, Set<Object> keys) {
        this.label = iconPlaceholder;
        this.keys = keys;
    }
    
    @Override
    public void addNotify() {
        label.setText("");
        label.setIcon(ImageUtilities.loadImageIcon(ValidationConstants.EMPTY_ICON, false));
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public JComponent getTarget() {
        return target;
    }

    public void setTarget(JComponent target) {
        this.target = target;
    }
    
    @Override
    public Collection<Object> getKeys() {
        return keys;
    }

    @Override
    public void indicateResult(ValidationResult result) {
        String iconResource;
        boolean fadeIcon = false;
        
        Severity s = result.getSeverity();
        switch (s) {
            case OK:
                if (lastSeverity != null &&
                    (lastSeverity.ordinal() <= Severity.WARNING.ordinal())) {
                    iconResource = ValidationConstants.OK_ICON;
                    fadeIcon = true;
                } else {
                    iconResource = ValidationConstants.EMPTY_ICON;
                }
                break;
            case INFO:
                iconResource = ValidationConstants.INFO_ICON;
                break;
            case WARNING:
                iconResource = ValidationConstants.WARNING_ICON;
                break;
            case ERROR:
                iconResource = ValidationConstants.ERROR_ICON;
                break;
            default:
                return;
        }

        ImageIcon icon = ImageUtilities.loadImageIcon(iconResource, false);
        label.setIcon(icon);
        if (fadeIcon) {
            fadeTimer = new Timer(2000, this::removeIcon);
            fadeTimer.setRepeats(false);
            fadeTimer.start();
        }
        
        String text = result.getMessagesText();
        JComponent linked = ((JComponent)label.getLabelFor());
        label.setToolTipText(text);
        if (text != null && s.ordinal() <= s.WARNING.ordinal()) {
            if (s != lastSeverity) {
                JComponent tooltipTarget = label;
                String originalTooltip = null;
                
                if (linked != null) {
                    originalTooltip = linked.getToolTipText();
                    linked.setToolTipText(text);
                    tooltipTarget = linked;
                }
                Rectangle labelBounds = label.getBounds();
                Point leftTop = labelBounds.getLocation();
                leftTop.move(4, 4);
                MouseEvent ev = new MouseEvent(tooltipTarget, 0, System.currentTimeMillis(), 
                        0, leftTop.x, leftTop.x, 0, false);
                ToolTipManager mgr = ToolTipManager.sharedInstance();
                Field f = null;
                try {
                    f = mgr.getClass().getDeclaredField("showImmediately");
                    f.setAccessible(true);
                    f.set(mgr, Boolean.TRUE);
                    mgr.mouseMoved(ev);
                    f.set(mgr, false);
                } catch (ReflectiveOperationException ex) {
                    // ignore
                }
                if (tooltipTarget != null) {
                    tooltipTarget.setToolTipText(originalTooltip);
                }
            }
        } else {
            ((JComponent)label.getLabelFor()).setToolTipText(null);
        }
        lastSeverity = s;
    }
    
    private void removeIcon(ActionEvent e) {
        label.setIcon(ImageUtilities.loadImageIcon(ValidationConstants.EMPTY_ICON, false));
        fadeTimer = null;
    }

    @Override
    public boolean reportMessages(ValidationResult result) {
        indicateResult(result);
        return false;
    }

    @NbBundle.Messages({
        "# {0} - name of the component",
        "ACTION_SelectComponent=Go to {0}"
    })
    @Override
    public Action transferControl(Object key) {
        if (!keys.contains(key)) {
            return null;
        }
        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                target.requestFocus();
            }
        };
        action.putValue(Action.NAME, getComponentName());
        return action;
    }

    @Override
    public void removeNotify() {
        if (fadeTimer != null) {
            fadeTimer.stop();
            fadeTimer = null;
        }
    }

    @Override
    public void attach(Lookup context) {
    }
}
