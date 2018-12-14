package com.glavsoft.rfb.encoding;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;

public class PixelFormat {
    public byte bitsPerPixel;
    public byte depth;
    public byte bigEndianFlag;
    public byte trueColourFlag;
    public short redMax;
    public short greenMax;
    public short blueMax;
    public byte redShift;
    public byte greenShift;
    public byte blueShift;

    public PixelFormat() {
    }

    public void fill(Reader reader) throws TransportException {
        this.bitsPerPixel = reader.readByte();
        this.depth = reader.readByte();
        this.bigEndianFlag = reader.readByte();
        this.trueColourFlag = reader.readByte();
        this.redMax = reader.readInt16();
        this.greenMax = reader.readInt16();
        this.blueMax = reader.readInt16();
        this.redShift = reader.readByte();
        this.greenShift = reader.readByte();
        this.blueShift = reader.readByte();
        reader.readBytes(3);
    }

    public void send(Writer writer) throws TransportException {
        writer.write(this.bitsPerPixel);
        writer.write(this.depth);
        writer.write(this.bigEndianFlag);
        writer.write(this.trueColourFlag);
        writer.write(this.redMax);
        writer.write(this.greenMax);
        writer.write(this.blueMax);
        writer.write(this.redShift);
        writer.write(this.greenShift);
        writer.write(this.blueShift);
        writer.writeInt16(0);
        writer.writeByte(0);
    }

    public static PixelFormat create32bppPixelFormat(int bigEndianFlag) {
        PixelFormat pixelFormat = new PixelFormat();
        pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
        pixelFormat.bitsPerPixel = 32;
        pixelFormat.blueMax = 255;
        pixelFormat.blueShift = 0;
        pixelFormat.greenMax = 255;
        pixelFormat.greenShift = 8;
        pixelFormat.redMax = 255;
        pixelFormat.redShift = 16;
        pixelFormat.depth = 24;
        pixelFormat.trueColourFlag = 1;
        return pixelFormat;
    }

    public static PixelFormat create16bppPixelFormat(int bigEndianFlag) {
        PixelFormat pixelFormat = new PixelFormat();
        pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
        pixelFormat.bitsPerPixel = 16;
        pixelFormat.blueMax = 31;
        pixelFormat.blueShift = 0;
        pixelFormat.greenMax = 63;
        pixelFormat.greenShift = 5;
        pixelFormat.redMax = 31;
        pixelFormat.redShift = 11;
        pixelFormat.depth = 16;
        pixelFormat.trueColourFlag = 1;
        return pixelFormat;
    }

    public static PixelFormat create8bppBGRPixelFormat(int bigEndianFlag) {
        PixelFormat pixelFormat = new PixelFormat();
        pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
        pixelFormat.bitsPerPixel = 8;
        pixelFormat.redMax = 7;
        pixelFormat.redShift = 0;
        pixelFormat.greenMax = 7;
        pixelFormat.greenShift = 3;
        pixelFormat.blueMax = 3;
        pixelFormat.blueShift = 6;
        pixelFormat.depth = 8;
        pixelFormat.trueColourFlag = 1;
        return pixelFormat;
    }

    public static PixelFormat create6bppPixelFormat(int bigEndianFlag) {
        PixelFormat pixelFormat = new PixelFormat();
        pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
        pixelFormat.bitsPerPixel = 8;
        pixelFormat.blueMax = 3;
        pixelFormat.blueShift = 0;
        pixelFormat.greenMax = 3;
        pixelFormat.greenShift = 2;
        pixelFormat.redMax = 3;
        pixelFormat.redShift = 4;
        pixelFormat.depth = 6;
        pixelFormat.trueColourFlag = 1;
        return pixelFormat;
    }

    public static PixelFormat create3bppPixelFormat(int bigEndianFlag) {
        PixelFormat pixelFormat = new PixelFormat();
        pixelFormat.bigEndianFlag = (byte) bigEndianFlag;
        pixelFormat.bitsPerPixel = 8;
        pixelFormat.blueMax = 1;
        pixelFormat.blueShift = 0;
        pixelFormat.greenMax = 1;
        pixelFormat.greenShift = 1;
        pixelFormat.redMax = 1;
        pixelFormat.redShift = 2;
        pixelFormat.depth = 3;
        pixelFormat.trueColourFlag = 1;
        return pixelFormat;
    }

    public String toString() {
        return "PixelFormat: [bits-per-pixel: " + String.valueOf(255 & this.bitsPerPixel) + ", depth: " + (255 & this.depth) + ", big-endian-flag: " + (255 & this.bigEndianFlag) + ", true-color-flag: " + (255 & this.trueColourFlag) + ", red-max: " + ('\uffff' & this.redMax) + ", green-max: " + ('\uffff' & this.greenMax) + ", blue-max: " + ('\uffff' & this.blueMax) + ", red-shift: " + (255 & this.redShift) + ", green-shift: " + (255 & this.greenShift) + ", blue-shift: " + (255 & this.blueShift) + "]";
    }
}
