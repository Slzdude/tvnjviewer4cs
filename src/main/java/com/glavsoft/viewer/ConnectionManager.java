package com.glavsoft.viewer;

import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.viewer.swing.ConnectionParams;
import com.glavsoft.viewer.swing.Utils;
import com.glavsoft.viewer.swing.gui.ConnectionDialog;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ConnectionManager {
    private final WindowListener appWindowListener;
    private volatile boolean forceConnectionDialog;
    private JFrame containerFrame;
    private final boolean isApplet;

    public ConnectionManager(WindowListener appWindowListener, boolean isApplet) {
        this.appWindowListener = appWindowListener;
        this.isApplet = isApplet;
    }

    protected void showReconnectDialog(String title, String message) {
        JOptionPane reconnectPane = new JOptionPane(message + "\nTry another connection?", 3, 0);
        JDialog reconnectDialog = reconnectPane.createDialog(this.containerFrame, title);
        Utils.decorateDialog(reconnectDialog);
        reconnectDialog.setVisible(true);
        if (reconnectPane.getValue() != null && (Integer) reconnectPane.getValue() != 1) {
            this.forceConnectionDialog = !this.isApplet;
        } else {
            this.appWindowListener.windowClosing((WindowEvent) null);
        }

    }

    Socket connectToHost(ConnectionParams connectionParams, ProtocolSettings settings) {
        Socket socket = null;
        Object connectionDialog = null;
        boolean wasError = false;
        int port = 0;
        boolean hasJsch = false;
        this.forceConnectionDialog = false;
        String host = connectionParams.hostName;
        port = connectionParams.getPortNumber();
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 10000);
        } catch (Exception e) {
            Messages.print_warn("could not connect to " + host + ":" + port + " (" + e.getMessage() + ")");
            return null;
        }
        return socket;
    }

    public void showConnectionErrorDialog(String message) {
        JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        JDialog errorDialog = errorPane.createDialog(this.containerFrame, "Connection error");
        Utils.decorateDialog(errorDialog);
        errorDialog.setVisible(true);
    }

    public void setContainerFrame(JFrame containerFrame) {
        if (this.containerFrame != null && this.containerFrame != containerFrame) {
            this.containerFrame.dispose();
        }

        this.containerFrame = containerFrame;
    }
}
