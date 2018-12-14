package com.glavsoft.rfb.protocol.state;

import com.glavsoft.exceptions.AuthenticationFailedException;
import com.glavsoft.exceptions.FatalException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedProtocolVersionException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.encoding.ServerInitMessage;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.ProtocolSettings;

public class InitState extends ProtocolState {
    public InitState(ProtocolContext context) {
        super(context);
    }

    public boolean next() throws UnsupportedProtocolVersionException, TransportException, UnsupportedSecurityTypeException, AuthenticationFailedException, FatalException {
        this.clientAndServerInit();
        return false;
    }

    protected void clientAndServerInit() throws TransportException {
        ServerInitMessage serverInitMessage = this.getServerInitMessage();
        ProtocolSettings settings = this.context.getSettings();
        settings.enableAllEncodingCaps();
        this.completeContextData(serverInitMessage);
    }

    protected void completeContextData(ServerInitMessage serverInitMessage) {
        this.context.setPixelFormat(serverInitMessage.getPixelFormat());
        this.context.setFbWidth(serverInitMessage.getFrameBufferWidth());
        this.context.setFbHeight(serverInitMessage.getFrameBufferHeight());
        this.context.setRemoteDesktopName(serverInitMessage.getName());
        this.logger.fine(serverInitMessage.toString());
    }

    protected ServerInitMessage getServerInitMessage() throws TransportException {
        this.writer.write(this.context.getSettings().getSharedFlag());
        ServerInitMessage serverInitMessage = new ServerInitMessage(this.reader);
        return serverInitMessage;
    }
}
