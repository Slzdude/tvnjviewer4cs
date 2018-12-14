package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public class RawDecoder extends Decoder {
    private static RawDecoder instance = new RawDecoder();

    public static RawDecoder getInstance() {
        return instance;
    }

    private RawDecoder() {
    }

    public void decode(Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        this.decode(reader, renderer, rect.x, rect.y, rect.width, rect.height);
    }

    public void decode(Reader reader, Renderer renderer, int x, int y, int width, int height) throws TransportException {
        int length = width * height * renderer.getBytesPerPixel();
        byte[] bytes = ByteBuffer.getInstance().getBuffer(length);
        reader.readBytes(bytes, 0, length);
        renderer.drawBytes(bytes, x, y, width, height);
    }
}
