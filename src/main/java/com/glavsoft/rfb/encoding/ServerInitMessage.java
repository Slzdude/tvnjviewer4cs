package com.glavsoft.rfb.encoding;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public class ServerInitMessage {
    protected int frameBufferWidth;
    protected int frameBufferHeight;
    protected PixelFormat pixelFormat;
    protected String name;

    public ServerInitMessage(Reader reader) throws TransportException {
        this.frameBufferWidth = reader.readUInt16();
        this.frameBufferHeight = reader.readUInt16();
        this.pixelFormat = new PixelFormat();
        this.pixelFormat.fill(reader);
        this.name = reader.readString();
    }

    protected ServerInitMessage() {
    }

    public int getFrameBufferWidth() {
        return this.frameBufferWidth;
    }

    public int getFrameBufferHeight() {
        return this.frameBufferHeight;
    }

    public PixelFormat getPixelFormat() {
        return this.pixelFormat;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "ServerInitMessage: [name: " + this.name + ", framebuffer-width: " + this.frameBufferWidth + ", framebuffer-height: " + this.frameBufferHeight + ", server-pixel-format: " + this.pixelFormat + "]";
    }
}
