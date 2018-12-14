package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public class RichCursorDecoder extends Decoder {
    private static RichCursorDecoder instance = new RichCursorDecoder();

    private RichCursorDecoder() {
    }

    public static RichCursorDecoder getInstance() {
        return instance;
    }

    public void decode(Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int bytesPerPixel = renderer.getBytesPerPixel();
        int length = rect.width * rect.height * bytesPerPixel;
        if (0 != length) {
            byte[] buffer = ByteBuffer.getInstance().getBuffer(length);
            reader.readBytes(buffer, 0, length);
            StringBuilder sb = new StringBuilder(" ");

            int scanLine;
            for (scanLine = 0; scanLine < length; ++scanLine) {
                sb.append(Integer.toHexString(buffer[scanLine] & 255)).append(" ");
            }

            scanLine = (int) Math.floor((double) ((rect.width + 7) / 8));
            byte[] bitmask = new byte[scanLine * rect.height];
            reader.readBytes(bitmask, 0, bitmask.length);
            sb = new StringBuilder(" ");

            for (int i = 0; i < bitmask.length; ++i) {
                sb.append(Integer.toHexString(bitmask[i] & 255)).append(" ");
            }

            int[] cursorPixels = new int[rect.width * rect.height];

            for (int y = 0; y < rect.height; ++y) {
                for (int x = 0; x < rect.width; ++x) {
                    int offset = y * rect.width + x;
                    cursorPixels[offset] = this.isBitSet(bitmask[y * scanLine + x / 8], x % 8) ? -16777216 | renderer.getPixelColor(buffer, offset * bytesPerPixel) : 0;
                }
            }

            renderer.createCursor(cursorPixels, rect);
        }
    }

    private boolean isBitSet(byte aByte, int index) {
        return (aByte & 1 << 7 - index) > 0;
    }
}
