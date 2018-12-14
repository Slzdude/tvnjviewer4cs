package com.glavsoft.rfb.protocol;

import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.exceptions.AuthenticationFailedException;
import com.glavsoft.exceptions.FatalException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedProtocolVersionException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.ClipboardController;
import com.glavsoft.rfb.IChangeSettingsListener;
import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.rfb.IRepaintController;
import com.glavsoft.rfb.IRfbSessionListener;
import com.glavsoft.rfb.client.ClientToServerMessage;
import com.glavsoft.rfb.client.FramebufferUpdateRequestMessage;
import com.glavsoft.rfb.client.SetEncodingsMessage;
import com.glavsoft.rfb.client.SetPixelFormatMessage;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.DecodersContainer;
import com.glavsoft.rfb.protocol.state.HandshakeState;
import com.glavsoft.rfb.protocol.state.ProtocolState;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;

import java.util.logging.Logger;

public class Protocol implements ProtocolContext, IChangeSettingsListener {
    private ProtocolState state;
    private final Logger logger = Logger.getLogger("com.glavsoft.rfb.protocol");
    private final IPasswordRetriever passwordRetriever;
    private final ProtocolSettings settings;
    private int fbWidth;
    private int fbHeight;
    private PixelFormat pixelFormat;
    private final Reader reader;
    private final Writer writer;
    private String remoteDesktopName;
    private MessageQueue messageQueue;
    private final DecodersContainer decoders;
    private SenderTask senderTask;
    private ReceiverTask receiverTask;
    private IRfbSessionListener rfbSessionListener;
    private IRepaintController repaintController;
    private PixelFormat serverPixelFormat;
    private Thread senderThread;
    private Thread receiverThread;
    private boolean isTight;
    private String protocolVersion;

    public Protocol(Reader reader, Writer writer, IPasswordRetriever passwordRetriever, ProtocolSettings settings) {
        this.reader = reader;
        this.writer = writer;
        this.passwordRetriever = passwordRetriever;
        this.settings = settings;
        this.decoders = new DecodersContainer();
        this.decoders.instantiateDecodersWhenNeeded(settings.encodings);
        this.state = new HandshakeState(this);
    }

    public void changeStateTo(ProtocolState state) {
        this.state = state;
    }

    public void handshake() throws UnsupportedProtocolVersionException, UnsupportedSecurityTypeException, AuthenticationFailedException, TransportException, FatalException {
        while (this.state.next()) {
        }

        this.messageQueue = new MessageQueue();
    }

    public PixelFormat getPixelFormat() {
        return this.pixelFormat;
    }

    public void setPixelFormat(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
        if (this.repaintController != null) {
            this.repaintController.setPixelFormat(pixelFormat);
        }

    }

    public String getRemoteDesktopName() {
        return this.remoteDesktopName;
    }

    public void setRemoteDesktopName(String name) {
        this.remoteDesktopName = name;
    }

    public int getFbWidth() {
        return this.fbWidth;
    }

    public void setFbWidth(int fbWidth) {
        this.fbWidth = fbWidth;
    }

    public int getFbHeight() {
        return this.fbHeight;
    }

    public void setFbHeight(int fbHeight) {
        this.fbHeight = fbHeight;
    }

    public IPasswordRetriever getPasswordRetriever() {
        return this.passwordRetriever;
    }

    public ProtocolSettings getSettings() {
        return this.settings;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Writer getWriter() {
        return this.writer;
    }

    public Reader getReader() {
        return this.reader;
    }

    public void startNormalHandling(IRfbSessionListener rfbSessionListener, IRepaintController repaintController, ClipboardController clipboardController) {
        this.rfbSessionListener = rfbSessionListener;
        this.repaintController = repaintController;
        this.serverPixelFormat = this.pixelFormat;
        this.serverPixelFormat.trueColourFlag = 1;
        this.setPixelFormat(this.createPixelFormat(this.settings));
        this.sendMessage(new SetPixelFormatMessage(this.pixelFormat));
        this.logger.fine("sent: " + this.pixelFormat);
        this.sendSupportedEncodingsMessage(this.settings);
        this.settings.addListener(this);
        this.settings.addListener(repaintController);
        this.sendRefreshMessage();
        this.senderTask = new SenderTask(this.messageQueue, this.writer, this);
        this.senderThread = new Thread(this.senderTask);
        this.senderThread.start();
        this.decoders.resetDecoders();
        this.receiverTask = new ReceiverTask(this.reader, repaintController, clipboardController, this.decoders, this);
        this.receiverThread = new Thread(this.receiverTask);
        this.receiverThread.start();
    }

    public void sendMessage(ClientToServerMessage message) {
        this.messageQueue.put(message);
    }

    private void sendSupportedEncodingsMessage(ProtocolSettings settings) {
        this.decoders.instantiateDecodersWhenNeeded(settings.encodings);
        SetEncodingsMessage encodingsMessage = new SetEncodingsMessage(settings.encodings);
        this.sendMessage(encodingsMessage);
        this.logger.fine("sent: " + encodingsMessage.toString());
    }

    private PixelFormat createPixelFormat(ProtocolSettings settings) {
        int serverBigEndianFlag = this.serverPixelFormat.bigEndianFlag;
        switch (settings.getBitsPerPixel()) {
            case 0:
                return this.serverPixelFormat;
            case 3:
                return PixelFormat.create3bppPixelFormat(serverBigEndianFlag);
            case 6:
                return PixelFormat.create6bppPixelFormat(serverBigEndianFlag);
            case 8:
                return PixelFormat.create8bppBGRPixelFormat(serverBigEndianFlag);
            case 16:
                return PixelFormat.create16bppPixelFormat(serverBigEndianFlag);
            case 32:
                return PixelFormat.create32bppPixelFormat(serverBigEndianFlag);
            default:
                return PixelFormat.create32bppPixelFormat(serverBigEndianFlag);
        }
    }

    public void settingsChanged(SettingsChangedEvent e) {
        ProtocolSettings settings = (ProtocolSettings) e.getSource();
        if (settings.isChangedEncodings()) {
            this.sendSupportedEncodingsMessage(settings);
        }

        if (settings.changedBitsPerPixel() && this.receiverTask != null) {
            this.receiverTask.queueUpdatePixelFormat(this.createPixelFormat(settings));
        }

    }

    public void sendRefreshMessage() {
        this.sendMessage(new FramebufferUpdateRequestMessage(0, 0, this.fbWidth, this.fbHeight, false));
        this.logger.fine("sent: full FB Refresh");
    }

    public void cleanUpSession(String message) {
        this.cleanUpSession();
        this.rfbSessionListener.rfbSessionStopped(message);
    }

    public synchronized void cleanUpSession() {
        if (this.senderTask != null) {
            this.senderTask.stopTask();
        }

        if (this.receiverTask != null) {
            this.receiverTask.stopTask();
        }

        if (this.senderTask != null) {
            try {
                this.senderThread.join(1000L);
            } catch (InterruptedException var3) {
            }

            this.senderTask = null;
        }

        if (this.receiverTask != null) {
            try {
                this.receiverThread.join(1000L);
            } catch (InterruptedException var2) {
            }

            this.receiverTask = null;
        }

    }

    public void setTight(boolean isTight) {
        this.isTight = isTight;
    }

    public boolean isTight() {
        return this.isTight;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }
}
