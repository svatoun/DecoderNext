/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sdedic
 */
public class UiUtils {
    private static final int WAIT_FINISHED_TIMEOUT = 10000;

    /**
     * Sets focus to the next focusable component according to focus traversal policy
     * @param component currently focused component
     */
    public static void focusNextComponent(Component component) {
        Container focusCycleRoot = component.getFocusCycleRootAncestor();
        if (focusCycleRoot == null) {
            return;
        }
        final FocusTraversalPolicy focusTraversalPolicy = focusCycleRoot.getFocusTraversalPolicy();
        if (focusTraversalPolicy == null) {
            return;
        }
        final Component componentAfter = focusTraversalPolicy.getComponentAfter(focusCycleRoot, component);
        if (componentAfter != null) {
            componentAfter.requestFocus();
        }
    }
    
    public static void runInSwing(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    /**
     * Scroll panel to make the component visible
     * @param component
     */
    public static void scrollToVisible(final JComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                component.scrollRectToVisible(new Rectangle(10, component.getHeight()));
            }
        });
    }

    /**
     * Make sure that the code will run in AWT dispatch thread
     * @param runnable
     */
    public static void runInAwtDispatchThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }

    }

    /**
     * Utility that sets border and traversal keys for JTextArea in JTextField style
     */
    public static void makeTextAreaLikeTextField(javax.swing.JTextArea ta, javax.swing.JTextField tf) {
        ta.setBorder(tf.getBorder());
        ta.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                 tf.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        ta.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                                 tf.getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
    }

    public static void waitFinished(RequestProcessor.Task task) {
        if (task.getDelay() > 0 && !task.isFinished()) {
            try {
                task.waitFinished(WAIT_FINISHED_TIMEOUT);
            } catch (InterruptedException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    private static class LabelFinder {
        private static final String PROP_DESCRIPTIVE_LABEL = "form.descriptiveLabel";
        private static final String PROP_FEEDBACK_LABEL = "form.feedbackLabel";
        
        private LabelFinder() {
        }
        
        private void scanLabel(JLabel l) {
            Component target = l.getLabelFor();
            if (!(target instanceof JComponent)) {
                return;
            }
            JComponent jcTarget = (JComponent)target;
            String text = l.getText();
            boolean descriptive = !text.equals("M");
            
            String propName = descriptive ? PROP_DESCRIPTIVE_LABEL : PROP_FEEDBACK_LABEL;
            if (jcTarget.getClientProperty(propName) instanceof JLabel) {
                return;
            }
            jcTarget.putClientProperty(propName, l);
        }
        
        private void scanContainer(Container parent) {
            for (Component c : parent.getComponents()) {
                if (!(c instanceof JComponent)) {
                    continue;
                }
                JComponent jc = (JComponent)c;
                if (c instanceof JLabel) {
                    scanLabel((JLabel)c);
                } else {
                   if (jc.getClientProperty(PROP_DESCRIPTIVE_LABEL) == null) {
                       jc.putClientProperty(PROP_DESCRIPTIVE_LABEL, "");
                   }
                   if (jc.getClientProperty(PROP_FEEDBACK_LABEL) == null) {
                       jc.putClientProperty(PROP_FEEDBACK_LABEL, "");
                   }
                }
                scanContainer(jc);
            }
        }
        
    }

    private static JLabel findLabel(JComponent target, Container inParent, String prop) {
        if (inParent == null) {
            inParent = target.getParent();
        }
        Object o = target.getClientProperty(prop);
        if (o == null) {
            new LabelFinder().scanContainer(inParent);
        }
        o = target.getClientProperty(prop);
        return (o instanceof JLabel) ? (JLabel)o : null;
    }

    public static JLabel findDescriptiveLabel(JComponent target, Container inParent) {
        return findLabel(target, inParent, LabelFinder.PROP_DESCRIPTIVE_LABEL);
    }

    public static JLabel findFeedbackLabel(JComponent target, Container inParent) {
        return findLabel(target, inParent, LabelFinder.PROP_FEEDBACK_LABEL);
    }
}
