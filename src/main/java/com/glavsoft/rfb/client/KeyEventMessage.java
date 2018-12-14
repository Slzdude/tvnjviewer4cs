package com.glavsoft.rfb.client;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Writer;

public class KeyEventMessage implements ClientToServerMessage {
    private final int key;
    private final boolean downFlag;

    public KeyEventMessage(int key, boolean downFlag) {
        this.downFlag = downFlag;
        this.key = key;
    }

    public void send(Writer writer) throws TransportException {
        writer.writeByte(4);
        writer.writeByte(this.downFlag ? 1 : 0);
        writer.writeInt16(0);
        writer.write(this.key);
        writer.flush();
    }

    public String toString() {
        return "[KeyEventMessage: [down-flag: " + this.downFlag + ", key: " + this.key + "(" + Integer.toHexString(this.key) + ")]";
    }
}
