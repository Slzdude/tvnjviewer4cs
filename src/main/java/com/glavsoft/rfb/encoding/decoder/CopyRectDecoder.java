package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public class CopyRectDecoder extends Decoder {
    public CopyRectDecoder() {
    }

    public void decode(Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int srcX = reader.readUInt16();
        int srcY = reader.readUInt16();
        if (rect.width != 0 && rect.height != 0) {
            renderer.copyRect(srcX, srcY, rect);
        }
    }
}
