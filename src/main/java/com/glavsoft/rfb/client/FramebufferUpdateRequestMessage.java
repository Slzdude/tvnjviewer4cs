package com.glavsoft.rfb.client;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Writer;

public class FramebufferUpdateRequestMessage implements ClientToServerMessage {
    private final boolean incremental;
    private final int height;
    private final int width;
    private final int y;
    private final int x;

    public FramebufferUpdateRequestMessage(int x, int y, int width, int height, boolean incremental) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.incremental = incremental;
    }

    public void send(Writer writer) throws TransportException {
        writer.writeByte(3);
        writer.writeByte(this.incremental ? 1 : 0);
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
        writer.writeInt16(this.width);
        writer.writeInt16(this.height);
        writer.flush();
    }

    public String toString() {
        return "FramebufferUpdateRequestMessage: [x: " + this.x + " y: " + this.y + " width: " + this.width + " height: " + this.height + " incremental: " + this.incremental + "]";
    }
}
