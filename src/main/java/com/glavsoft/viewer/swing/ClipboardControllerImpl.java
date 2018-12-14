package com.glavsoft.viewer.swing;

import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.rfb.ClipboardController;
import com.glavsoft.rfb.client.ClientCutTextMessage;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.utils.Strings;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.AccessControlException;

public class ClipboardControllerImpl implements ClipboardController, Runnable {
    private static final String STANDARD_CHARSET = "ISO-8859-1";
    private static final long CLIPBOARD_UPDATE_CHECK_INTERVAL_MILS = 1000L;
    private Clipboard clipboard;
    private String clipboardText = null;
    private volatile boolean isRunning;
    private boolean isEnabled;
    private final ProtocolContext context;
    private Charset charset;

    public ClipboardControllerImpl(ProtocolContext context, String charsetName) {
        this.context = context;

        try {
            this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            this.updateSavedClipboardContent();
        } catch (AccessControlException var4) {
        }

        if (Strings.isTrimmedEmpty(charsetName)) {
            this.charset = Charset.defaultCharset();
        } else if ("standard".equalsIgnoreCase(charsetName)) {
            this.charset = Charset.forName("ISO-8859-1");
        } else {
            this.charset = Charset.isSupported(charsetName) ? Charset.forName(charsetName) : Charset.defaultCharset();
        }

        if (this.charset.name().startsWith("UTF")) {
            this.charset = Charset.forName("ISO-8859-1");
        }

    }

    public void updateSystemClipboard(byte[] bytes) {
        if (this.clipboard != null) {
            StringSelection stringSelection = new StringSelection(new String(bytes, this.charset));
            if (this.isEnabled) {
                this.clipboard.setContents(stringSelection, (ClipboardOwner) null);
            }
        }

    }

    private void updateSavedClipboardContent() {
        if (this.clipboard != null && this.clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            try {
                this.clipboardText = (String) this.clipboard.getData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException var2) {
            } catch (IOException var3) {
            }
        } else {
            this.clipboardText = null;
        }

    }

    public String getClipboardText() {
        return this.clipboardText;
    }

    public String getRenewedClipboardText() {
        String old = this.clipboardText;
        this.updateSavedClipboardContent();
        return this.clipboardText != null && !this.clipboardText.equals(old) ? this.clipboardText : null;
    }

    public void setEnabled(boolean enable) {
        if (!enable) {
            this.isRunning = false;
        }

        if (enable && !this.isEnabled) {
            (new Thread(this)).start();
        }

        this.isEnabled = enable;
    }

    public void run() {
        this.isRunning = true;

        while (this.isRunning) {
            String clipboardText = this.getRenewedClipboardText();
            if (clipboardText != null) {
                this.context.sendMessage(new ClientCutTextMessage(clipboardText.getBytes(this.charset)));
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException var3) {
            }
        }

    }

    public void settingsChanged(SettingsChangedEvent e) {
        ProtocolSettings settings = (ProtocolSettings) e.getSource();
        this.setEnabled(settings.isAllowClipboardTransfer());
    }
}
