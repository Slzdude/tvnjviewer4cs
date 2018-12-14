package com.glavsoft.viewer.swing.gui;

import com.glavsoft.viewer.swing.Utils;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

public class PasswordDialog extends JDialog {
    private String password = "";
    private static final int PADDING = 4;
    private final JLabel messageLabel;

    public PasswordDialog(Frame owner, final WindowListener onClose) {
        super(owner, "VNC Authentication", true);
        this.addWindowListener(onClose);
        JPanel pane = new JPanel(new GridLayout(0, 1, 4, 4));
        this.add(pane);
        pane.setBorder(new EmptyBorder(4, 4, 4, 4));
        this.messageLabel = new JLabel("Server requires VNC authentication");
        pane.add(this.messageLabel);
        JPanel passwordPanel = new JPanel();
        passwordPanel.add(new JLabel("Password:"));
        final JPasswordField passwordField = new JPasswordField("", 20);
        passwordPanel.add(passwordField);
        pane.add(passwordPanel);
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        buttonPanel.add(loginButton);
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PasswordDialog.this.password = new String(passwordField.getPassword());
                PasswordDialog.this.setVisible(false);
            }
        });
        JButton closeButton = new JButton("Cancel");
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PasswordDialog.this.password = null;
                PasswordDialog.this.setVisible(false);
                onClose.windowClosing((WindowEvent) null);
            }
        });
        pane.add(buttonPanel);
        this.getRootPane().setDefaultButton(loginButton);
        Utils.decorateDialog(this);
        Utils.centerWindow(this);
        this.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                passwordField.requestFocusInWindow();
            }
        });
    }

    public void setServerHostName(String serverHostName) {
        this.messageLabel.setText("Server '" + serverHostName + "' requires VNC authentication");
        this.pack();
    }

    public String getPassword() {
        return this.password;
    }
}
