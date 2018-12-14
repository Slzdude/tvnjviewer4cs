package com.glavsoft.viewer;

import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.rfb.IChangeSettingsListener;
import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.rfb.IRfbSessionListener;
import com.glavsoft.rfb.client.KeyEventMessage;
import com.glavsoft.rfb.protocol.Protocol;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;
import com.glavsoft.utils.Strings;
import com.glavsoft.viewer.cli.Parser;
import com.glavsoft.viewer.swing.ClipboardControllerImpl;
import com.glavsoft.viewer.swing.ConnectionParams;
import com.glavsoft.viewer.swing.ModifierButtonEventListener;
import com.glavsoft.viewer.swing.ParametersHandler;
import com.glavsoft.viewer.swing.Surface;
import com.glavsoft.viewer.swing.UiSettings;
import com.glavsoft.viewer.swing.gui.OptionsDialog;
import com.glavsoft.viewer.swing.gui.PasswordDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

public class Viewer extends JPanel implements Runnable, IRfbSessionListener, WindowListener, IChangeSettingsListener {
    public static Logger logger = Logger.getLogger("com.glavsoft");
    private boolean isZoomToFitSelected;
    private boolean forceReconnection;
    private String reconnectionReason;
    private ContainerManager containerManager;
    private final ConnectionParams connectionParams;
    private String passwordFromParams;
    private Socket workingSocket;
    private Protocol workingProtocol;
    private JFrame containerFrame;
    boolean isSeparateFrame;
    boolean isApplet;
    boolean showControls;
    private Surface surface;
    private final ProtocolSettings settings;
    private final UiSettings uiSettings;
    private boolean isAppletStopped;
    private volatile boolean isStoppingProcess;
    private List kbdButtons;
    protected Viewer.ViewerCallback callme;

    public JComponent getContentPane() {
        return this;
    }

    public Protocol getWorkingProtocol() {
        return this.workingProtocol;
    }

    public boolean isZoomToFitSelected() {
        return this.isZoomToFitSelected;
    }

    public Surface getSurface() {
        return this.surface;
    }

    public UiSettings getUiSettings() {
        return this.uiSettings;
    }

    public void setZoomToFitSelected(boolean zoomToFitSelected) {
        this.isZoomToFitSelected = zoomToFitSelected;
    }

    public static void main(String[] args) {
        Parser parser = new Parser();
        ParametersHandler.completeParserOptions(parser);
        parser.parse(args);
        if (parser.isSet("help")) {
            printUsage(parser.optionsUsage());
        }

        Viewer viewer = new Viewer(parser);
        SwingUtilities.invokeLater(viewer);
    }

    public static void printUsage(String additional) {
        System.out.println("Usage: java -jar (progfilename) [hostname [port_number]] [Options]\n    or\n java -jar (progfilename) [Options]\n    or\n java -jar (progfilename) -help\n    to view this help\n\nWhere Options are:\n" + additional + "\nOptions format: -optionName=optionValue. Ex. -host=localhost -port=5900 -viewonly=yes\n" + "Both option name and option value are case insensitive.");
    }

    public Viewer() {
        this.isSeparateFrame = false;
        this.isApplet = false;
        this.showControls = true;
        this.isAppletStopped = false;
        this.callme = null;
        this.connectionParams = new ConnectionParams();
        this.settings = ProtocolSettings.getDefaultSettings();
        this.uiSettings = new UiSettings();
    }

    public Viewer(String host, int port, boolean highq, Viewer.ViewerCallback listener) {
        this.isSeparateFrame = false;
        this.isApplet = false;
        this.showControls = true;
        this.isAppletStopped = false;
        this.callme = null;
        this.connectionParams = new ConnectionParams(host, port);
        if (highq) {
            this.settings = ProtocolSettings.getHighQualitySettings();
        } else {
            this.settings = ProtocolSettings.getLowQualitySettings();
        }

        this.uiSettings = new UiSettings();
        this.init();
        this.callme = listener;
    }

    private Viewer(Parser parser) {
        this();
        ParametersHandler.completeSettingsFromCLI(parser, this.connectionParams, this.settings, this.uiSettings);
        this.showControls = ParametersHandler.showControls;
        this.passwordFromParams = parser.getValueFor("password");
        this.isApplet = false;
    }

    public void rfbSessionStopped(String reason) {
        this.cleanUpUISessionAndConnection();
        Messages.print_error("died: " + reason);
    }

    private synchronized void cleanUpUISessionAndConnection() {
        this.isStoppingProcess = true;
        if (this.workingSocket != null && this.workingSocket.isConnected()) {
            try {
                this.workingSocket.close();
            } catch (IOException var2) {
            }
        }

        if (this.containerFrame != null) {
            this.containerFrame.dispose();
            this.containerFrame = null;
        }

        this.isStoppingProcess = false;
    }

    public void windowClosing(WindowEvent e) {
        if (e != null && e.getComponent() != null) {
            e.getWindow().setVisible(false);
        }

        this.closeApp();
    }

    public void closeApp() {
        if (this.workingProtocol != null) {
            this.workingProtocol.cleanUpSession();
        }

        this.cleanUpUISessionAndConnection();
        this.isAppletStopped = true;
        this.repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);
    }

    public void destroy() {
        this.closeApp();
    }

    public void init() {
        this.showControls = true;
        this.isSeparateFrame = false;
        this.passwordFromParams = "";
        this.isApplet = false;
        this.repaint();
        (new Thread(this)).start();
    }

    public void start() {
        this.setSurfaceToHandleKbdFocus();
    }

    public void run() {
        ConnectionManager connectionManager = new ConnectionManager(this, this.isApplet);
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start <= 60000L) {
            this.workingSocket = connectionManager.connectToHost(this.connectionParams, this.settings);
            if (null != this.workingSocket) {
                Messages.print_good("I am connected.");

                try {
                    this.setDoubleBuffered(true);
                    Reader reader = new Reader(this.workingSocket.getInputStream());
                    Writer writer = new Writer(this.workingSocket.getOutputStream());
                    this.workingProtocol = new Protocol(reader, writer, new Viewer.PasswordChooser(this.passwordFromParams, this.connectionParams, this.containerFrame, this), this.settings);

                    try {
                        this.workingSocket.setSoTimeout(30000);
                        this.workingProtocol.handshake();
                        this.workingSocket.setSoTimeout(0);
                    } catch (Exception var7) {
                        Messages.print_error("Connection to VNC server didn't respond. " + var7.getMessage());
                        this.workingSocket.close();
                        JOptionPane.showMessageDialog((Component) null, "VNC server connection is not responding.\nTry launching VNC again!", "VNC Error", 0);
                        return;
                    }

                    ClipboardControllerImpl clipboardController = new ClipboardControllerImpl(this.workingProtocol, this.settings.getRemoteCharsetName());
                    clipboardController.setEnabled(this.settings.isAllowClipboardTransfer());
                    this.settings.addListener(clipboardController);
                    this.surface = new Surface(this.workingProtocol, this, this.uiSettings.getScaleFactor());
                    this.settings.addListener(this);
                    this.uiSettings.addListener(this.surface);
                    this.containerFrame = this.createContainer();
                    connectionManager.setContainerFrame(this.containerFrame);
                    this.updateFrameTitle();
                    this.workingProtocol.startNormalHandling(this, this.surface, clipboardController);
                    if (this.callme != null) {
                        this.callme.connected(this);
                    }

                    return;
                } catch (Exception var9) {
                    Messages.print_error("Error: " + var9.getMessage());
                    var9.printStackTrace();
                }
            }

            try {
                Thread.sleep(5000L);
            } catch (Exception var8) {
            }
        }

        Messages.print_error("Unable to connect after 60s of trying.");
        JOptionPane.showMessageDialog((Component) null, "Could not establish a connection to the user's\ndesktop. Make sure you're in a process that has\na desktop session associated with it.", "VNC Error", 0);
    }

    private JFrame createContainer() {
        this.containerManager = new ContainerManager(this);
        Container container = this.containerManager.createContainer(this.surface, this.isSeparateFrame, this.isApplet);
        if (this.showControls) {
            this.createButtonsPanel(this.workingProtocol, this.containerManager);
            this.containerManager.registerResizeListener(container);
            this.containerManager.updateZoomButtonsState();
        }

        this.setSurfaceToHandleKbdFocus();
        return this.isSeparateFrame ? (JFrame) container : null;
    }

    public void validate() {
        super.validate();
        this.packContainer();
    }

    public void packContainer() {
        this.containerManager.pack();
    }

    protected void createButtonsPanel(final ProtocolContext context, ContainerManager containerManager) {
        ContainerManager.ButtonsBar buttonsBar = containerManager.createButtonsBar();
        this.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ev) {
                Viewer.this.setSurfaceToHandleKbdFocus();
                ev.consume();
            }

            public void keyPressed(KeyEvent ev) {
                Viewer.this.setSurfaceToHandleKbdFocus();
                ev.consume();
            }

            public void keyReleased(KeyEvent ev) {
                Viewer.this.setSurfaceToHandleKbdFocus();
                ev.consume();
            }
        });
        buttonsBar.createButton("refresh", "Refresh screen", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                context.sendRefreshMessage();
                Viewer.this.setSurfaceToHandleKbdFocus();
                Viewer.this.packContainer();
            }
        });
        JToggleButton viewButton = buttonsBar.createToggleButton("view", "View Only", new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    Viewer.this.getSurface().setViewOnly(true);
                    ((JComponent) e.getSource()).setBackground(Color.RED);
                } else {
                    Viewer.this.getSurface().setViewOnly(false);
                    Viewer.this.setSurfaceToHandleKbdFocus();
                    ((JComponent) e.getSource()).setBackground((Color) null);
                }

            }
        });
        viewButton.setSelected(true);
        containerManager.addZoomButtons();
        this.kbdButtons = new LinkedList();
        buttonsBar.createStrut();
        JButton ctrlAltDelButton = buttonsBar.createButton("ctrl-alt-del", "Send 'Ctrl-Alt-Del'", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Viewer.this.sendCtrlAltDel(context);
                Viewer.this.setSurfaceToHandleKbdFocus();
            }
        });
        this.kbdButtons.add(ctrlAltDelButton);
        JButton winButton = buttonsBar.createButton("win", "Send 'Win' key as 'Ctrl-Esc'", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Viewer.this.sendWinKey(context);
                Viewer.this.setSurfaceToHandleKbdFocus();
            }
        });
        this.kbdButtons.add(winButton);
        final JToggleButton ctrlButton = buttonsBar.createToggleButton("ctrl", "Ctrl Lock", new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    context.sendMessage(new KeyEventMessage(65507, true));
                    ((JComponent) e.getSource()).setBackground(Color.RED);
                } else {
                    context.sendMessage(new KeyEventMessage(65507, false));
                    ((JComponent) e.getSource()).setBackground((Color) null);
                }

                Viewer.this.setSurfaceToHandleKbdFocus();
            }
        });
        this.kbdButtons.add(ctrlButton);
        final JToggleButton altButton = buttonsBar.createToggleButton("alt", "Alt Lock", new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    context.sendMessage(new KeyEventMessage(65513, true));
                    ((JComponent) e.getSource()).setBackground(Color.RED);
                } else {
                    context.sendMessage(new KeyEventMessage(65513, false));
                    ((JComponent) e.getSource()).setBackground((Color) null);
                }

                Viewer.this.setSurfaceToHandleKbdFocus();
            }
        });
        this.kbdButtons.add(altButton);
        ModifierButtonEventListener modifierButtonListener = new ModifierButtonEventListener();
        modifierButtonListener.addButton(17, ctrlButton);
        modifierButtonListener.addButton(18, altButton);
        this.surface.addModifierListener(modifierButtonListener);
        containerManager.setButtonsBarVisible(true);
        this.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ev) {
                Viewer.this.setSurfaceToHandleKbdFocus();
            }

            public void componentHidden(ComponentEvent ev) {
                ctrlButton.setSelected(false);
                altButton.setSelected(false);
            }
        });
    }

    void updateFrameTitle() {
        if (this.containerFrame != null) {
            this.containerFrame.setTitle(this.workingProtocol.getRemoteDesktopName() + " [zoom: " + this.uiSettings.getScalePercentFormatted() + "%]");
        }

    }

    protected void setSurfaceToHandleKbdFocus() {
        if (this.surface != null && !this.surface.requestFocusInWindow()) {
            this.surface.requestFocus();
        }

    }

    public void settingsChanged(SettingsChangedEvent e) {
        ProtocolSettings settings = (ProtocolSettings) e.getSource();
        this.setEnabledKbdButtons(!settings.isViewOnly());
    }

    private void setEnabledKbdButtons(boolean enabled) {
        if (this.kbdButtons != null) {
            Iterator i$ = this.kbdButtons.iterator();

            while (i$.hasNext()) {
                JComponent b = (JComponent) i$.next();
                b.setEnabled(enabled);
            }
        }

    }

    private void showOptionsDialog() {
        OptionsDialog optionsDialog = new OptionsDialog(this.containerFrame);
        optionsDialog.initControlsFromSettings(this.settings, false);
        optionsDialog.setVisible(true);
    }

    private void showConnectionInfoMessage(String title) {
        StringBuilder message = new StringBuilder();
        message.append("Connected to: ").append(title).append("\n");
        message.append("Host: ").append(this.connectionParams.hostName).append(" Port: ").append(this.connectionParams.getPortNumber()).append("\n\n");
        message.append("Desktop geometry: ").append(String.valueOf(this.surface.getWidth())).append(" × ").append(String.valueOf(this.surface.getHeight())).append("\n");
        message.append("Color format: ").append(String.valueOf(Math.round(Math.pow(2.0D, (double) this.workingProtocol.getPixelFormat().depth)))).append(" colors (").append(String.valueOf(this.workingProtocol.getPixelFormat().depth)).append(" bits)\n");
        message.append("Current protocol version: ").append(this.workingProtocol.getProtocolVersion());
        if (this.workingProtocol.isTight()) {
            message.append("tight");
        }

        message.append("\n");
        JOptionPane infoPane = new JOptionPane(message.toString(), 1);
        JDialog infoDialog = infoPane.createDialog(this.containerFrame, "VNC connection info");
        infoDialog.setModalityType(ModalityType.MODELESS);
        infoDialog.setVisible(true);
    }

    private void sendCtrlAltDel(ProtocolContext context) {
        context.sendMessage(new KeyEventMessage(65507, true));
        context.sendMessage(new KeyEventMessage(65513, true));
        context.sendMessage(new KeyEventMessage(65535, true));
        context.sendMessage(new KeyEventMessage(65535, false));
        context.sendMessage(new KeyEventMessage(65513, false));
        context.sendMessage(new KeyEventMessage(65507, false));
    }

    private void sendWinKey(ProtocolContext context) {
        context.sendMessage(new KeyEventMessage(65507, true));
        context.sendMessage(new KeyEventMessage(65307, true));
        context.sendMessage(new KeyEventMessage(65307, false));
        context.sendMessage(new KeyEventMessage(65507, false));
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public interface ViewerCallback {
        void connected(Viewer var1);
    }

    private class PasswordChooser implements IPasswordRetriever {
        private final String passwordPredefined;
        private final ConnectionParams connectionParams;
        PasswordDialog passwordDialog;
        private final JFrame owner;
        private final WindowListener onClose;

        private PasswordChooser(String passwordPredefined, ConnectionParams connectionParams, JFrame owner, WindowListener onClose) {
            this.passwordPredefined = passwordPredefined;
            this.connectionParams = connectionParams;
            this.owner = owner;
            this.onClose = onClose;
        }

        public String getPassword() {
            return Strings.isTrimmedEmpty(this.passwordPredefined) ? this.getPasswordFromGUI() : this.passwordPredefined;
        }

        private String getPasswordFromGUI() {
            if (null == this.passwordDialog) {
                this.passwordDialog = new PasswordDialog(this.owner, this.onClose);
            }

            this.passwordDialog.setServerHostName(this.connectionParams.hostName + ":" + this.connectionParams.getPortNumber());
            this.passwordDialog.toFront();
            this.passwordDialog.setVisible(true);
            return this.passwordDialog.getPassword();
        }
    }
}
