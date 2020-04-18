/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import java.awt.Component;
import java.lang.reflect.Field;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * Extracts text value from a component. Supports the following components:
 * <ul>
 * <li>JTextComponent - extracts the current document contents, trimmed.
 * <li>editable JComboBox - gives the document content of the editor.
 * <li>non-editable JComboBox - gives toString of the selected item.
 * </ul>
 * @author sdedic
 */
public class TextValue implements ValidatedValue<String> {
    private static Field basicComboBoxUIList;
    private static final JList dummyList = new JList();
    
    private Field getListField() {
        if (basicComboBoxUIList != null) {
            return basicComboBoxUIList;
        }
        try {
            Field f = BasicComboBoxUI.class.getDeclaredField("listBox");
            f.setAccessible(true);
            basicComboBoxUIList = f;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        return basicComboBoxUIList;
    }
    
    private JList getComboList(JComboBox cb) {
        ComboBoxUI ui = cb.getUI();
        if (!(ui instanceof BasicComboBoxUI)) {
            return dummyList;
        } else {
            try {
                return (JList)getListField().get(ui);
            } catch (ReflectiveOperationException ex) {
                return dummyList;
            }
        }
    }
    
    @Override
    public String getValue(JComponent ui) {
        if (!(ui instanceof JComponent)) {
            throw new IllegalArgumentException();
        }
        String s = null;
        
        if (ui instanceof JTextField) {
            s = ((JTextField)ui).getText();
        } else if (ui instanceof JComboBox) {
            JComboBox jc = (JComboBox)ui;
            if (jc.isEditable()) {
                if (jc.getSelectedIndex() == -1) {
                    JTextField tf = (JTextField)jc.getEditor().getEditorComponent();
                    s = tf.getText();
                }
            } else {
                Component r = jc.getRenderer().getListCellRendererComponent(getComboList(jc),
                        jc.getSelectedItem(), jc.getSelectedIndex(), true, false);
                if (r instanceof JLabel) {
                    return ((JLabel)r).getText();
                } else {
                    Object o = jc.getSelectedItem();
                    return o == null ? "" : o.toString().trim();
                }
            }
        }
        if (s == null) {
            s = "";
        } else {
            s = s.trim();
        }
        return s;
    }

    @Override
    public boolean test(Object t) {
        return t instanceof JComponent;
    }
}
