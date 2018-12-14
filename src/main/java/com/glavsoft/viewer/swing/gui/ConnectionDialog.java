package com.glavsoft.viewer.swing.gui;

import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.utils.Strings;
import com.glavsoft.viewer.swing.ConnectionParams;
import com.glavsoft.viewer.swing.Utils;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ConnectionDialog extends JDialog {
    private static final int PADDING = 4;
    public static final int COLUMNS_HOST_FIELD = 30;
    public static final int COLUMNS_PORT_USER_FIELD = 13;
    private ConnectionParams connectionParams;
    private final boolean hasJsch;
    private final JTextField serverPortField;
    private JCheckBox useSshTunnelingCheckbox;
    private final JComboBox serverNameCombo;
    private JTextField sshUserField;
    private JTextField sshHostField;
    private JTextField sshPortField;
    private JLabel sshUserLabel;
    private JLabel sshHostLabel;
    private JLabel sshPortLabel;
    private final ConnectionsHistory connectionsHistory;
    private JLabel ssUserWarningLabel;

    public ConnectionDialog(JFrame owner, final WindowListener appWindowListener, final ConnectionParams connectionParams, final ProtocolSettings settings, boolean hasJsch) {
        super(owner, "New TightVNC Connection");
        this.connectionParams = connectionParams;
        this.hasJsch = hasJsch;
        JPanel pane = new JPanel(new GridBagLayout());
        this.add(pane);
        pane.setBorder(new EmptyBorder(4, 4, 4, 4));
        this.setLayout(new GridBagLayout());
        int gridRow = 0;
        this.serverNameCombo = new JComboBox();
        this.connectionsHistory = new ConnectionsHistory(connectionParams);
        this.initConnectionsHistoryCombo();
        settings.copySerializedFieldsFrom(this.connectionsHistory.getSettings(connectionParams));
        settings.addListener(this.connectionsHistory);
        this.serverNameCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Object item = ConnectionDialog.this.serverNameCombo.getSelectedItem();
                if (item instanceof ConnectionParams) {
                    ConnectionParams cp = (ConnectionParams) item;
                    ConnectionDialog.this.completeInputFieldsFrom(cp);
                    ProtocolSettings settingsNew = ConnectionDialog.this.connectionsHistory.getSettings(cp);
                    settings.copySerializedFieldsFrom(settingsNew);
                }

            }
        });
        this.addFormFieldRow(pane, gridRow, new JLabel("Remote Host:"), this.serverNameCombo, true);
        this.serverPortField = new JTextField(13);
        this.addFormFieldRow(pane, ++gridRow, new JLabel("Port:"), this.serverPortField, false);
        ++gridRow;
        if (hasJsch) {
            gridRow = this.createSshOptions(connectionParams, pane, gridRow);
        }

        this.completeInputFieldsFrom(connectionParams);
        JPanel buttonPanel = new JPanel();
        JButton connectButton = new JButton("Connect");
        buttonPanel.add(connectButton);
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object item = ConnectionDialog.this.serverNameCombo.getSelectedItem();
                String hostName = item instanceof ConnectionParams ? ((ConnectionParams) item).hostName : (String) item;
                ConnectionDialog.this.setServerNameString(hostName);
                ConnectionDialog.this.setPort(ConnectionDialog.this.serverPortField.getText());
                ConnectionDialog.this.setSshOptions();
                ConnectionDialog.this.connectionsHistory.reorderConnectionsList(connectionParams, settings);
                ConnectionDialog.this.connectionsHistory.save();
                ConnectionDialog.this.serverNameCombo.removeAllItems();
                ConnectionDialog.this.completeCombo();
                if (ConnectionDialog.this.validateFields()) {
                    ConnectionDialog.this.setVisible(false);
                } else {
                    ConnectionDialog.this.serverNameCombo.requestFocusInWindow();
                }

            }
        });
        JButton optionsButton = new JButton("Options...");
        buttonPanel.add(optionsButton);
        optionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OptionsDialog od = new OptionsDialog(ConnectionDialog.this);
                od.initControlsFromSettings(settings, true);
                od.setVisible(true);
                ConnectionDialog.this.toFront();
            }
        });
        JButton closeButton = new JButton("Close");
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ConnectionDialog.this.setVisible(false);
                appWindowListener.windowClosing((WindowEvent) null);
            }
        });
        GridBagConstraints cButtons = new GridBagConstraints();
        cButtons.gridx = 0;
        cButtons.gridy = gridRow;
        cButtons.weightx = 100.0D;
        cButtons.weighty = 100.0D;
        cButtons.gridwidth = 2;
        cButtons.gridheight = 1;
        pane.add(buttonPanel, cButtons);
        this.getRootPane().setDefaultButton(connectButton);
        this.addWindowListener(appWindowListener);
        this.setResizable(false);
        Utils.decorateDialog(this);
        Utils.centerWindow(this);
    }

    private void initConnectionsHistoryCombo() {
        this.serverNameCombo.setEditable(true);
        new AutoCompletionComboEditorDocument(this.serverNameCombo);
        this.connectionParams.completeEmptyFieldsFrom(this.connectionsHistory.getMostSuitableConnection(this.connectionParams));
        this.completeCombo();
        this.serverNameCombo.setRenderer(new HostnameComboboxRenderer());
    }

    private void completeCombo() {
        if (Strings.isTrimmedEmpty(this.connectionParams.hostName) && this.connectionsHistory.getConnectionsList().isEmpty()) {
            this.connectionParams.hostName = "";
            this.serverNameCombo.addItem(new ConnectionParams());
        } else {
            this.serverNameCombo.addItem(new ConnectionParams(this.connectionParams));
            Iterator i$ = this.connectionsHistory.getConnectionsList().iterator();

            while (i$.hasNext()) {
                ConnectionParams cp = (ConnectionParams) i$.next();
                if (!cp.equals(this.connectionParams)) {
                    this.serverNameCombo.addItem(cp);
                }
            }

        }
    }

    private void completeInputFieldsFrom(ConnectionParams cp) {
        this.serverPortField.setText(String.valueOf(cp.getPortNumber()));
        if (this.hasJsch) {
            this.completeSshInputFieldsFrom(cp);
        }

    }

    private int createSshOptions(final ConnectionParams connectionParams, JPanel pane, int gridRow) {
        GridBagConstraints cUseSshTunnelLabel = new GridBagConstraints();
        cUseSshTunnelLabel.gridx = 0;
        cUseSshTunnelLabel.gridy = gridRow;
        cUseSshTunnelLabel.weightx = 100.0D;
        cUseSshTunnelLabel.weighty = 100.0D;
        cUseSshTunnelLabel.gridwidth = 2;
        cUseSshTunnelLabel.gridheight = 1;
        cUseSshTunnelLabel.anchor = 21;
        cUseSshTunnelLabel.ipadx = 4;
        cUseSshTunnelLabel.ipady = 10;
        this.useSshTunnelingCheckbox = new JCheckBox("Use SSH tunneling");
        pane.add(this.useSshTunnelingCheckbox, cUseSshTunnelLabel);
        ++gridRow;
        this.sshHostLabel = new JLabel("SSH Server:");
        this.sshHostField = new JTextField(30);
        this.addFormFieldRow(pane, gridRow, this.sshHostLabel, this.sshHostField, true);
        ++gridRow;
        this.sshPortLabel = new JLabel("SSH Port:");
        this.sshPortField = new JTextField(13);
        this.addFormFieldRow(pane, gridRow, this.sshPortLabel, this.sshPortField, false);
        ++gridRow;
        this.sshUserLabel = new JLabel("SSH User:");
        this.sshUserField = new JTextField(13);
        JPanel sshUserFieldPane = new JPanel(new FlowLayout(0, 0, 0));
        sshUserFieldPane.add(this.sshUserField);
        this.ssUserWarningLabel = new JLabel(" (will be asked if not specified)");
        sshUserFieldPane.add(this.ssUserWarningLabel);
        this.addFormFieldRow(pane, gridRow, this.sshUserLabel, sshUserFieldPane, false);
        ++gridRow;
        this.useSshTunnelingCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean useSsh = e.getStateChange() == 1;
                connectionParams.setUseSsh(useSsh);
                ConnectionDialog.this.sshUserLabel.setEnabled(useSsh);
                ConnectionDialog.this.sshUserField.setEnabled(useSsh);
                ConnectionDialog.this.ssUserWarningLabel.setEnabled(useSsh);
                ConnectionDialog.this.sshHostLabel.setEnabled(useSsh);
                ConnectionDialog.this.sshHostField.setEnabled(useSsh);
                ConnectionDialog.this.sshPortLabel.setEnabled(useSsh);
                ConnectionDialog.this.sshPortField.setEnabled(useSsh);
            }
        });
        this.completeSshInputFieldsFrom(connectionParams);
        return gridRow;
    }

    private void addFormFieldRow(JPanel pane, int gridRow, JLabel label, JComponent field, boolean fill) {
        GridBagConstraints cLabel = new GridBagConstraints();
        cLabel.gridx = 0;
        cLabel.gridy = gridRow;
        cLabel.weightx = cLabel.weighty = 100.0D;
        cLabel.gridwidth = cLabel.gridheight = 1;
        cLabel.anchor = 22;
        cLabel.ipadx = 4;
        cLabel.ipady = 10;
        pane.add(label, cLabel);
        GridBagConstraints cField = new GridBagConstraints();
        cField.gridx = 1;
        cField.gridy = gridRow;
        cField.weightx = 0.0D;
        cField.weighty = 100.0D;
        cField.gridwidth = cField.gridheight = 1;
        cField.anchor = 21;
        if (fill) {
            cField.fill = 2;
        }

        pane.add(field, cField);
    }

    private void completeSshInputFieldsFrom(ConnectionParams connectionParams) {
        boolean useSsh = connectionParams.useSsh();
        this.useSshTunnelingCheckbox.setSelected(useSsh);
        this.sshUserLabel.setEnabled(useSsh);
        this.sshUserField.setEnabled(useSsh);
        this.ssUserWarningLabel.setEnabled(useSsh);
        this.sshHostLabel.setEnabled(useSsh);
        this.sshHostField.setEnabled(useSsh);
        this.sshPortLabel.setEnabled(useSsh);
        this.sshPortField.setEnabled(useSsh);
        this.sshUserField.setText(connectionParams.sshUserName);
        this.sshHostField.setText(connectionParams.sshHostName);
        this.sshPortField.setText(String.valueOf(connectionParams.getSshPortNumber()));
    }

    private void setSshOptions() {
        if (this.hasJsch) {
            this.connectionParams.sshUserName = this.sshUserField.getText();
            this.connectionParams.sshHostName = this.sshHostField.getText();
            this.connectionParams.parseSshPortNumber(this.sshPortField.getText());
            this.sshPortField.setText(String.valueOf(this.connectionParams.getSshPortNumber()));
        }

    }

    protected boolean validateFields() {
        return !Strings.isTrimmedEmpty(this.connectionParams.hostName);
    }

    protected void setServerNameString(String text) {
        this.connectionParams.hostName = text;
    }

    public void setPort(String text) {
        this.connectionParams.parseRfbPortNumber(text);
    }
}
