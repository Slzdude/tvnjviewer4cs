package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public class RREDecoder extends Decoder {
    public RREDecoder() {
    }

    public void decode(Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int numOfSubrectangles = reader.readInt32();
        int color = renderer.readPixelColor(reader);
        renderer.fillRect(color, rect);

        for (int i = 0; i < numOfSubrectangles; ++i) {
            color = renderer.readPixelColor(reader);
            int x = reader.readUInt16();
            int y = reader.readUInt16();
            int width = reader.readUInt16();
            int height = reader.readUInt16();
            renderer.fillRect(color, rect.x + x, rect.y + y, width, height);
        }

    }
}
