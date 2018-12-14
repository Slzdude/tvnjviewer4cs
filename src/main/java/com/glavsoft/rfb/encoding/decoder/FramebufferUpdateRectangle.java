package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.transport.Reader;

public class FramebufferUpdateRectangle {
    public int x;
    public int y;
    public int width;
    public int height;
    private EncodingType encodingType;

    public FramebufferUpdateRectangle() {
    }

    public FramebufferUpdateRectangle(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public void fill(Reader reader) throws TransportException {
        this.x = reader.readUInt16();
        this.y = reader.readUInt16();
        this.width = reader.readUInt16();
        this.height = reader.readUInt16();
        int encoding = reader.readInt32();
        this.encodingType = EncodingType.byId(encoding);
    }

    public EncodingType getEncodingType() {
        return this.encodingType;
    }

    public String toString() {
        return "FramebufferUpdateRect: [x: " + this.x + ", y: " + this.y + ", width: " + this.width + ", height: " + this.height + ", encodingType: " + this.encodingType + "]";
    }
}
