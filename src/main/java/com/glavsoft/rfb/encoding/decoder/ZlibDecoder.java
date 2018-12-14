package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

import java.io.ByteArrayInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ZlibDecoder extends Decoder {
    private Inflater decoder;

    public ZlibDecoder() {
    }

    public void decode(Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int zippedLength = (int) reader.readUInt32();
        if (0 != zippedLength) {
            int length = rect.width * rect.height * renderer.getBytesPerPixel();
            byte[] bytes = this.unzip(reader, zippedLength, length);
            Reader unzippedReader = new Reader(new ByteArrayInputStream(bytes, zippedLength, length));
            RawDecoder.getInstance().decode(unzippedReader, renderer, rect);
        }
    }

    protected byte[] unzip(Reader reader, int zippedLength, int length) throws TransportException {
        byte[] bytes = ByteBuffer.getInstance().getBuffer(zippedLength + length);
        reader.readBytes(bytes, 0, zippedLength);
        if (null == this.decoder) {
            this.decoder = new Inflater();
        }

        this.decoder.setInput(bytes, 0, zippedLength);

        try {
            this.decoder.inflate(bytes, zippedLength, length);
            return bytes;
        } catch (DataFormatException var6) {
            throw new TransportException("cannot inflate Zlib data", var6);
        }
    }

    public void reset() {
        this.decoder = null;
    }
}
