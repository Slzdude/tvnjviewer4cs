package com.glavsoft.viewer.swing.gui;

import com.glavsoft.viewer.swing.ConnectionParams;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class HostnameComboboxRenderer extends DefaultListCellRenderer {
    public HostnameComboboxRenderer() {
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String stringValue = this.renderListItem((ConnectionParams) value);
        this.setText(stringValue);
        this.setFont(this.getFont().deriveFont(0));
        if (isSelected) {
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
        } else {
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
        }

        return this;
    }

    public String renderListItem(ConnectionParams cp) {
        String s = "<html><b>" + cp.hostName + "</b>:" + cp.getPortNumber();
        if (cp.useSsh()) {
            s = s + " <i>(via ssh://" + cp.sshUserName + "@" + cp.sshHostName + ":" + cp.getSshPortNumber() + ")</i>";
        }

        return s + "</html>";
    }
}
