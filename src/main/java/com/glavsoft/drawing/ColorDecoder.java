package com.glavsoft.drawing;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.transport.Reader;

public class ColorDecoder {
    protected byte redShift;
    protected byte greenShift;
    protected byte blueShift;
    public short redMax;
    public short greenMax;
    public short blueMax;
    private final int bytesPerPixel;
    private final int bytesPerPixelSignificant;
    private final byte[] buff;
    private int startShift;
    private int startShiftCompact;
    private int addShiftItem;
    private final boolean isTightSpecific;

    public ColorDecoder(PixelFormat pf) {
        this.redShift = pf.redShift;
        this.greenShift = pf.greenShift;
        this.blueShift = pf.blueShift;
        this.redMax = pf.redMax;
        this.greenMax = pf.greenMax;
        this.blueMax = pf.blueMax;
        this.bytesPerPixel = pf.bitsPerPixel / 8;
        this.bytesPerPixelSignificant = 24 == pf.depth && 32 == pf.bitsPerPixel ? 3 : this.bytesPerPixel;
        this.buff = new byte[this.bytesPerPixel];
        if (0 == pf.bigEndianFlag) {
            this.startShift = 0;
            this.startShiftCompact = 0;
            this.addShiftItem = 8;
        } else {
            this.startShift = pf.bitsPerPixel - 8;
            this.startShiftCompact = Math.max(0, pf.depth - 8);
            this.addShiftItem = -8;
        }

        this.isTightSpecific = 4 == this.bytesPerPixel && 3 == this.bytesPerPixelSignificant && 255 == this.redMax && 255 == this.greenMax && 255 == this.blueMax;
    }

    protected int readColor(Reader reader) throws TransportException {
        return this.getColor(reader.readBytes(this.buff, 0, this.bytesPerPixel), 0);
    }

    protected int readCompactColor(Reader reader) throws TransportException {
        return this.getCompactColor(reader.readBytes(this.buff, 0, this.bytesPerPixelSignificant), 0);
    }

    protected int readTightColor(Reader reader) throws TransportException {
        return this.getTightColor(reader.readBytes(this.buff, 0, this.bytesPerPixelSignificant), 0);
    }

    protected int convertColor(int rawColor) {
        return 255 * (rawColor >> this.redShift & this.redMax) / this.redMax << 16 | 255 * (rawColor >> this.greenShift & this.greenMax) / this.greenMax << 8 | 255 * (rawColor >> this.blueShift & this.blueMax) / this.blueMax;
    }

    public void fillRawComponents(byte[] comp, byte[] bytes, int offset) {
        int rawColor = this.getRawTightColor(bytes, offset);
        comp[0] = (byte) (rawColor >> this.redShift & this.redMax);
        comp[1] = (byte) (rawColor >> this.greenShift & this.greenMax);
        comp[2] = (byte) (rawColor >> this.blueShift & this.blueMax);
    }

    protected int getTightColor(byte[] bytes, int offset) {
        return this.convertColor(this.getRawTightColor(bytes, offset));
    }

    private int getRawTightColor(byte[] bytes, int offset) {
        return this.isTightSpecific ? (bytes[offset++] & 255) << 16 | (bytes[offset++] & 255) << 8 | bytes[offset++] & 255 : this.getRawColor(bytes, offset);
    }

    protected int getColor(byte[] bytes, int offset) {
        return this.convertColor(this.getRawColor(bytes, offset));
    }

    private int getRawColor(byte[] bytes, int offset) {
        int shift = this.startShift;
        int item = this.addShiftItem;
        int rawColor = (bytes[offset++] & 255) << shift;

        for (int i = 1; i < this.bytesPerPixel; ++i) {
            rawColor |= (bytes[offset++] & 255) << (shift += item);
        }

        return rawColor;
    }

    protected int getCompactColor(byte[] bytes, int offset) {
        int shift = this.startShiftCompact;
        int item = this.addShiftItem;
        int rawColor = (bytes[offset++] & 255) << shift;

        for (int i = 1; i < this.bytesPerPixelSignificant; ++i) {
            rawColor |= (bytes[offset++] & 255) << (shift += item);
        }

        return this.convertColor(rawColor);
    }
}
