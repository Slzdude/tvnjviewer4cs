package com.glavsoft.rfb.client;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Writer;

public class PointerEventMessage implements ClientToServerMessage {
    private final byte buttonMask;
    private final short x;
    private final short y;

    public PointerEventMessage(byte buttonMask, short x, short y) {
        this.buttonMask = buttonMask;
        this.x = x;
        this.y = y;
    }

    public void send(Writer writer) throws TransportException {
        writer.writeByte(5);
        writer.writeByte(this.buttonMask);
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
        writer.flush();
    }

    public String toString() {
        return "PointerEventMessage: [x: " + this.x + ", y: " + this.y + ", button-mask: " + this.buttonMask + "]";
    }
}
