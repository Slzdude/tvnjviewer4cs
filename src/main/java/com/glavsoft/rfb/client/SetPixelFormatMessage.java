package com.glavsoft.rfb.client;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.transport.Writer;

public class SetPixelFormatMessage implements ClientToServerMessage {
    private final PixelFormat pixelFormat;

    public SetPixelFormatMessage(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    public void send(Writer writer) throws TransportException {
        writer.writeByte(0);
        writer.writeInt16(0);
        writer.writeByte(0);
        this.pixelFormat.send(writer);
        writer.flush();
    }
}
