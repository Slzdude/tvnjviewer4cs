package com.glavsoft.rfb.protocol.state;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.encoding.ServerInitMessage;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.ProtocolSettings;

public class InitTightState extends InitState {
    public InitTightState(ProtocolContext context) {
        super(context);
    }

    protected void clientAndServerInit() throws TransportException {
        ServerInitMessage serverInitMessage = this.getServerInitMessage();
        int nServerMessageTypes = this.reader.readUInt16();
        int nClientMessageTypes = this.reader.readUInt16();
        int nEncodingTypes = this.reader.readUInt16();
        this.reader.readUInt16();
        ProtocolSettings settings = this.context.getSettings();
        settings.serverMessagesCapabilities.read(this.reader, nServerMessageTypes);
        settings.clientMessagesCapabilities.read(this.reader, nClientMessageTypes);
        settings.encodingTypesCapabilities.read(this.reader, nEncodingTypes);
        this.completeContextData(serverInitMessage);
    }
}
