package com.glavsoft.rfb.client;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Writer;

public class ClientCutTextMessage implements ClientToServerMessage {
    private final byte[] bytes;

    public ClientCutTextMessage(byte[] bytes) {
        this.bytes = bytes;
    }

    public void send(Writer writer) throws TransportException {
        writer.write((byte) 6);
        writer.writeByte(0);
        writer.writeInt16(0);
        writer.write(this.bytes.length);
        writer.write(this.bytes);
        writer.flush();
    }

    public String toString() {
        return "ClientCutTextMessage: [length: " + this.bytes.length + ", text: ...]";
    }
}
