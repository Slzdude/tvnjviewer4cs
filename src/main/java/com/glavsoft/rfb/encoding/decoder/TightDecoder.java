package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.ColorDecoder;
import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class TightDecoder extends Decoder {
    private static Logger logger = Logger.getLogger("com.glavsoft.rfb.encoding.decoder");
    private static final int FILL_TYPE = 8;
    private static final int JPEG_TYPE = 9;
    private static final int FILTER_ID_MASK = 64;
    private static final int STREAM_ID_MASK = 48;
    private static final int BASIC_FILTER = 0;
    private static final int PALETTE_FILTER = 1;
    private static final int GRADIENT_FILTER = 2;
    private static final int MIN_SIZE_TO_COMPRESS = 12;
    static final int DECODERS_NUM = 4;
    Inflater[] decoders;
    private int decoderId;
    static final int tightZlibBufferSize = 512;

    public TightDecoder() {
        this.reset();
    }

    public void decode(Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int bytesPerPixel = renderer.getBytesPerPixelSignificant();
        int compControl = reader.readUInt8();
        this.resetDecoders(compControl);
        int compType = compControl >> 4 & 15;
        switch (compType) {
            case 8:
                int color = renderer.readTightPixelColor(reader);
                renderer.fillRect(color, rect);
                break;
            case 9:
                if (bytesPerPixel != 3) {
                }

                this.processJpegType(reader, renderer, rect);
                break;
            default:
                if (compType <= 9) {
                    this.processBasicType(compControl, reader, renderer, rect);
                }
        }

    }

    private void processBasicType(int compControl, Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        this.decoderId = (compControl & 48) >> 4;
        int filterId = 0;
        if ((compControl & 64) > 0) {
            filterId = reader.readUInt8();
        }

        int bytesPerCPixel = renderer.getBytesPerPixelSignificant();
        int lengthCurrentbpp = bytesPerCPixel * rect.width * rect.height;
        byte[] buffer;
        switch (filterId) {
            case 0:
                buffer = this.readTightData(lengthCurrentbpp, reader);
                renderer.drawTightBytes(buffer, 0, rect.x, rect.y, rect.width, rect.height);
                break;
            case 1:
                int paletteSize = reader.readUInt8() + 1;
                int[] palette = this.readPalette(paletteSize, reader, renderer);
                int dataLength = paletteSize == 2 ? rect.height * ((rect.width + 7) / 8) : rect.width * rect.height;
                buffer = this.readTightData(dataLength, reader);
                renderer.drawBytesWithPalette(buffer, rect, palette);
                break;
            case 2:
                buffer = this.readTightData(bytesPerCPixel * rect.width * rect.height, reader);
                byte[][] opRows = new byte[2][rect.width * 3 + 3];
                int opRowIndex = 0;
                byte[] components = new byte[3];
                int pixelOffset = 0;
                ColorDecoder colorDecoder = renderer.getColorDecoder();

                for (int i = 0; i < rect.height; ++i) {
                    byte[] thisRow = opRows[opRowIndex];
                    byte[] prevRow = opRows[opRowIndex = (opRowIndex + 1) % 2];

                    for (int j = 3; j < rect.width * 3 + 3; j += 3) {
                        colorDecoder.fillRawComponents(components, buffer, pixelOffset);
                        pixelOffset += bytesPerCPixel;
                        int d = (255 & prevRow[j + 0]) + (255 & thisRow[j + 0 - 3]) - (255 & prevRow[j + 0 - 3]);
                        thisRow[j + 0] = (byte) (components[0] + (d < 0 ? 0 : (d > colorDecoder.redMax ? colorDecoder.redMax : d)) & colorDecoder.redMax);
                        d = (255 & prevRow[j + 1]) + (255 & thisRow[j + 1 - 3]) - (255 & prevRow[j + 1 - 3]);
                        thisRow[j + 1] = (byte) (components[1] + (d < 0 ? 0 : (d > colorDecoder.greenMax ? colorDecoder.greenMax : d)) & colorDecoder.greenMax);
                        d = (255 & prevRow[j + 2]) + (255 & thisRow[j + 2 - 3]) - (255 & prevRow[j + 2 - 3]);
                        thisRow[j + 2] = (byte) (components[2] + (d < 0 ? 0 : (d > colorDecoder.blueMax ? colorDecoder.blueMax : d)) & colorDecoder.blueMax);
                    }

                    renderer.drawUncaliberedRGBLine(thisRow, rect.x, rect.y + i, rect.width);
                }
        }

    }

    private int[] readPalette(int paletteSize, Reader reader, Renderer renderer) throws TransportException {
        int[] palette = new int[paletteSize];

        for (int i = 0; i < palette.length; ++i) {
            palette[i] = renderer.readTightPixelColor(reader);
        }

        return palette;
    }

    private byte[] readTightData(int expectedLength, Reader reader) throws TransportException {
        if (expectedLength < 12) {
            byte[] buffer = ByteBuffer.getInstance().getBuffer(expectedLength);
            reader.readBytes(buffer, 0, expectedLength);
            return buffer;
        } else {
            return this.readCompressedData(expectedLength, reader);
        }
    }

    private byte[] readCompressedData(int expectedLength, Reader reader) throws TransportException {
        int rawDataLength = this.readCompactSize(reader);
        byte[] buffer = ByteBuffer.getInstance().getBuffer(expectedLength + rawDataLength);
        reader.readBytes(buffer, expectedLength, rawDataLength);
        if (null == this.decoders[this.decoderId]) {
            this.decoders[this.decoderId] = new Inflater();
        }

        Inflater decoder = this.decoders[this.decoderId];
        decoder.setInput(buffer, expectedLength, rawDataLength);

        try {
            decoder.inflate(buffer, 0, expectedLength);
            return buffer;
        } catch (DataFormatException var7) {
            logger.throwing("TightDecoder", "readCompressedData", var7);
            throw new TransportException("cannot inflate tight compressed data", var7);
        }
    }

    private void processJpegType(Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int jpegBufferLength = this.readCompactSize(reader);
        byte[] bytes = ByteBuffer.getInstance().getBuffer(jpegBufferLength);
        reader.readBytes(bytes, 0, jpegBufferLength);
        renderer.drawJpegImage(bytes, 0, jpegBufferLength, rect);
    }

    private int readCompactSize(Reader reader) throws TransportException {
        int b = reader.readUInt8();
        int size = b & 127;
        if ((b & 128) != 0) {
            b = reader.readUInt8();
            size += (b & 127) << 7;
            if ((b & 128) != 0) {
                size += reader.readUInt8() << 14;
            }
        }

        return size;
    }

    private void resetDecoders(int compControl) {
        for (int i = 0; i < 4; ++i) {
            if ((compControl & 1) != 0 && this.decoders[i] != null) {
                this.decoders[i].reset();
            }

            compControl >>= 1;
        }

    }

    public void reset() {
        this.decoders = new Inflater[4];
    }
}
