package com.glavsoft.viewer.swing.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

public class AutoCompletionComboEditorDocument extends PlainDocument {
    private ComboBoxModel model;
    private boolean selecting;
    private JComboBox comboBox;
    private final boolean hidePopupOnFocusLoss;
    private JTextComponent editor;

    public AutoCompletionComboEditorDocument(final JComboBox comboBox) {
        this.comboBox = comboBox;
        this.model = comboBox.getModel();
        this.editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        this.editor.setDocument(this);
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!AutoCompletionComboEditorDocument.this.selecting) {
                    AutoCompletionComboEditorDocument.this.highlightCompletedText(0);
                }

            }
        });
        Object selectedItem = comboBox.getSelectedItem();
        if (selectedItem != null) {
            this.setText(selectedItem.toString());
            this.highlightCompletedText(0);
        }

        this.hidePopupOnFocusLoss = System.getProperty("java.version").startsWith("1.5");
        this.editor.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (AutoCompletionComboEditorDocument.this.hidePopupOnFocusLoss) {
                    comboBox.setPopupVisible(false);
                }

            }
        });
    }

    public void remove(int offs, int len) throws BadLocationException {
        if (!this.selecting) {
            super.remove(offs, len);
        }
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (!this.selecting) {
            super.insertString(offs, str, a);
            Object item = this.lookupItem(this.getText(0, this.getLength()));
            if (item != null) {
                this.setSelectedItem(item);
                this.setText(item.toString());
                this.highlightCompletedText(offs + str.length());
                if (this.comboBox.isDisplayable()) {
                    this.comboBox.setPopupVisible(true);
                }
            }

        }
    }

    private void setText(String text) {
        try {
            super.remove(0, this.getLength());
            super.insertString(0, text, (AttributeSet) null);
        } catch (BadLocationException var3) {
            throw new RuntimeException(var3);
        }
    }

    private void setSelectedItem(Object item) {
        this.selecting = true;
        this.model.setSelectedItem(item);
        this.selecting = false;
    }

    private void highlightCompletedText(int offs) {
        JTextComponent editor = (JTextComponent) this.comboBox.getEditor().getEditorComponent();
        editor.setSelectionStart(offs);
        editor.setSelectionEnd(this.getLength());
    }

    private Object lookupItem(String pattern) {
        Object selectedItem = this.model.getSelectedItem();
        if (selectedItem != null && this.startsWithIgnoreCase(selectedItem, pattern)) {
            return selectedItem;
        } else {
            int i = 0;

            for (int n = this.model.getSize(); i < n; ++i) {
                Object currentItem = this.model.getElementAt(i);
                if (this.startsWithIgnoreCase(currentItem, pattern)) {
                    return currentItem;
                }
            }

            return null;
        }
    }

    private boolean startsWithIgnoreCase(Object currentItem, String pattern) {
        return currentItem.toString().toLowerCase().startsWith(pattern.toLowerCase());
    }
}
