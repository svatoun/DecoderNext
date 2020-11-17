package one.dedic.jmri.decodernext.ui.controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

/**
 * Support for completion in editable combo boxes.
 * @author sdedic
 */
public class ComboBoxCompleter extends KeyAdapter implements DocumentListener,
        ActionListener, PropertyChangeListener, FocusListener, PopupMenuListener {

    private final JComboBox combo;
    private final DocFilter docFilter = new DocFilter();
    private JTextComponent editor;
    private ComboBoxModel model;
    private boolean hidePopupOnFocusLoss = true;
    private int locked;
    private JPopupMenu comboPopup;
    private boolean autoPopup;

    public ComboBoxCompleter(JComboBox combo) {
        this.combo = combo;
        this.model = combo.getModel();
        combo.addPropertyChangeListener(this);
        
        editor = (JTextComponent) combo.getEditor().getEditorComponent();

        configureEditor(combo.getEditor());
        // Handle initially selected object
        Object selected = combo.getSelectedItem();
        if (selected != null) {
            editor.setText(selected.toString());
        }
        highlightCompletedText(0);
        
        try {
            ComboBoxUI ui = combo.getUI();
            if (ui instanceof BasicComboBoxUI) {
                Field f = BasicComboBoxUI.class.getDeclaredField("popup");
                f.setAccessible(true);
                comboPopup = (JPopupMenu)f.get(ui);
            }
        } catch (ReflectiveOperationException ex) {
        }
        if (comboPopup != null) {
            comboPopup.addPopupMenuListener(this);
        }
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        autoPopup = false;
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
        autoPopup = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isLocked()) {
            highlightCompletedText(0);
        }
    }

    void configureEditor(ComboBoxEditor newEditor) {
        if (editor != null) {
            editor.removeKeyListener(this);
            editor.removeFocusListener(this);
            ((AbstractDocument)editor.getDocument()).setDocumentFilter(null);
        }

        if (newEditor != null) {
            editor = (JTextComponent) newEditor.getEditorComponent();
            editor.addKeyListener(this);
            editor.addFocusListener(this);
            ((AbstractDocument)editor.getDocument()).setDocumentFilter(docFilter);
        }
    }

    public void focusGained(FocusEvent e) {
        if (selectionMark != -1) {
            editor.getCaret().setSelectionVisible(true);
        } else {
            highlightCompletedText(0);
        }
    }

    public void focusLost(FocusEvent e) {
        // Workaround for Bug 5100422 - Hide Popup on focus loss
        if (hidePopupOnFocusLoss) {
            combo.setPopupVisible(false);
        }
        // workaround for some strange editable combobox bug:
        // the selection remains visible after losing focus. Making explicitly
        // NOT visible:
        if (editor.getSelectionStart() == editor.getSelectionEnd()) {
            selectionMark = -1;
        } else {
            selectionMark = editor.getSelectionStart();
            editor.getCaret().setSelectionVisible(false);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(PROP_EDITOR)) {
            configureEditor((ComboBoxEditor) e.getNewValue());
        }
        if (e.getPropertyName().equals(PROP_MODEL)) {
            model = (ComboBoxModel) e.getNewValue();
        }
    }
    private static final String PROP_MODEL = "model";
    private static final String PROP_EDITOR = "editor";
    
    private boolean hitBackspace;
    private boolean hitBackspaceOnSelection;
    
    private int selectionMark = -1;

    @Override
    public void keyPressed(KeyEvent e) {
        if (combo.isDisplayable() && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
            if (!combo.isPopupVisible()) {
                autoPopup = true;
                combo.setPopupVisible(true);
            }
        }
        hitBackspace = false;
        switch (e.getKeyCode()) {
            // determine if the pressed key is backspace (needed by the remove method)
            case KeyEvent.VK_BACK_SPACE:
                hitBackspace = true;
                hitBackspaceOnSelection = editor.getSelectionStart() != editor.getSelectionEnd();
                break;
            // ignore delete key
            case KeyEvent.VK_DELETE:
//                e.consume();
                break;
        }
    }
    
    class DocFilter extends DocumentFilter {

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (isLocked()) {
                return;
            }
            if (length == 0) {
                insertString(fb, offset, text, attrs);
            } else {
                fb.remove(offset, length);
                insertString(fb, offset, text, attrs);
            }
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (isLocked()) {
                return;
            }
            super.insertString(fb, offset, string, attr);
            try {
                lock();
                String content = editor.getText();
                Object item = lookupItem(content);
                int offs = offset;
                // insert the string into the document
                // lookup and select a matching item
                if (item != null) {
                    setSelectedItem(item);
                    fb.remove(0, content.length());
                    fb.insertString(0, item.toString(), null);
                    if (content.equalsIgnoreCase(item.toString())) {
                        editor.selectAll();
                    } else {
                        // select the completed part
                        highlightCompletedText(offs + string.length());
                    }
                } else {
                    setSelectedItem(null);
                }
            } finally {
                unlock();
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (isLocked()) {
                super.remove(fb, offset, length);
                return;
            }
            int offs = offset;

            // return immediately when selecting an item
            if (!hitBackspace || combo.getSelectedItem() == null) {
                super.remove(fb, offset, length);
                return;
            }
            // user hit backspace => move the selection backwards
            // old item keeps being selected
            if (offs > 0) {
                if (hitBackspaceOnSelection) {
                    offs--;
                }
            }
            String content = editor.getText();
            if (length == content.length()) {
                super.remove(fb, offset, length);
                combo.setSelectedItem(null);
                if (autoPopup) {
                    combo.hidePopup();
                }
                return;
            }
            try {
                lock();
                Object selected = lookupItem(content);
                if (selected != null) {
                    editor.setText(selected.toString());
                }
                highlightCompletedText(offs);
            } finally {
                unlock();
            }
        }
        
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    private boolean isLocked() {
        return locked > 0;
    }

    private void lock() {
        locked++;
    }

    private boolean unlock() {
        if (--locked <= 0) {
            locked = 0;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    private void highlightCompletedText(int start) {
        editor.setCaretPosition(editor.getText().length());
        editor.moveCaretPosition(start);
        System.err.println("Moving caret: " + editor.getText());
    }

    private void setSelectedItem(Object item) {
        try {
            lock();
            model.setSelectedItem(item);
        } finally {
            unlock();
        }
    }

    private Object lookupItem(String pattern) {
        Object selectedItem = model.getSelectedItem();
        // only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
            return selectedItem;
        } else {
            // iterate over all items
            for (int i = 0, n = model.getSize(); i < n; i++) {
                Object currentItem = model.getElementAt(i);
                // current item starts with the pattern?
                if (currentItem != null && startsWithIgnoreCase(currentItem.toString(), pattern)) {
                    return currentItem;
                }
            }
        }
        // no item starts with the pattern => return null
        return null;
    }

    // checks if str1 starts with str2 - ignores case
    private boolean startsWithIgnoreCase(String str1, String str2) {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }

}
