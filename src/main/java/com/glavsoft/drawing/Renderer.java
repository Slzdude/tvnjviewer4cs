package com.glavsoft.drawing;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.transport.Reader;

import java.util.Arrays;

public abstract class Renderer {
    protected Reader reader;
    protected int width;
    protected int height;
    protected int bytesPerPixel;
    protected int bytesPerPixelSignificant;
    protected int[] pixels;
    protected SoftCursor cursor;
    protected PixelFormat pixelFormat;
    private ColorDecoder colorDecoder;

    public Renderer() {
    }

    public abstract void drawJpegImage(byte[] var1, int var2, int var3, FramebufferUpdateRectangle var4);

    protected void init(Reader reader, int width, int height, PixelFormat pixelFormat) {
        this.reader = reader;
        this.width = width;
        this.height = height;
        this.initPixelFormat(pixelFormat);
        this.pixels = new int[width * height];
        Arrays.fill(this.pixels, 0);
    }

    public synchronized void initPixelFormat(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
        this.bytesPerPixel = pixelFormat.bitsPerPixel / 8;
        this.bytesPerPixelSignificant = 24 == pixelFormat.depth && 32 == pixelFormat.bitsPerPixel ? 3 : this.bytesPerPixel;
        this.colorDecoder = new ColorDecoder(pixelFormat);
    }

    public void drawBytes(byte[] bytes, int x, int y, int width, int height) {
        int i = 0;

        for (int ly = y; ly < y + height; ++ly) {
            int end = ly * this.width + x + width;

            for (int pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                this.pixels[pixelsOffset] = this.getPixelColor(bytes, i);
                i += this.bytesPerPixel;
            }
        }

    }

    public synchronized int drawCompactBytes(byte[] bytes, int offset, int x, int y, int width, int height) {
        int i = offset;

        for (int ly = y; ly < y + height; ++ly) {
            int end = ly * this.width + x + width;

            for (int pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                this.pixels[pixelsOffset] = this.getCompactPixelColor(bytes, i);
                i += this.bytesPerPixelSignificant;
            }
        }

        return i - offset;
    }

    public synchronized void drawColoredBitmap(int[] colors, int x, int y, int width, int height) {
        int i = 0;

        for (int ly = y; ly < y + height; ++ly) {
            int end = ly * this.width + x + width;

            for (int pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                this.pixels[pixelsOffset] = colors[i++];
            }
        }

    }

    public synchronized int drawTightBytes(byte[] bytes, int offset, int x, int y, int width, int height) {
        int i = offset;

        for (int ly = y; ly < y + height; ++ly) {
            int end = ly * this.width + x + width;

            for (int pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
                this.pixels[pixelsOffset] = this.colorDecoder.getTightColor(bytes, i);
                i += this.bytesPerPixelSignificant;
            }
        }

        return i - offset;
    }

    public synchronized void drawUncaliberedRGBLine(byte[] bytes, int x, int y, int width) {
        int end = y * this.width + x + width;
        int i = 3;

        for (int pixelsOffset = y * this.width + x; pixelsOffset < end; ++pixelsOffset) {
            this.pixels[pixelsOffset] = (255 & 255 * (this.colorDecoder.redMax & bytes[i++]) / this.colorDecoder.redMax) << 16 | (255 & 255 * (this.colorDecoder.greenMax & bytes[i++]) / this.colorDecoder.greenMax) << 8 | 255 & 255 * (this.colorDecoder.blueMax & bytes[i++]) / this.colorDecoder.blueMax;
        }

    }

    public synchronized void drawBytesWithPalette(byte[] buffer, FramebufferUpdateRectangle rect, int[] palette) {
        int dx;
        int dy;
        int n;
        int i;
        if (palette.length == 2) {
            i = rect.y * this.width + rect.x;
            int rowBytes = (rect.width + 7) / 8;

            for (dy = 0; dy < rect.height; ++dy) {
                for (dx = 0; dx < rect.width / 8; ++dx) {
                    byte b = buffer[dy * rowBytes + dx];

                    for (n = 7; n >= 0; --n) {
                        this.pixels[i++] = palette[b >> n & 1];
                    }
                }

                for (n = 7; n >= 8 - rect.width % 8; --n) {
                    this.pixels[i++] = palette[buffer[dy * rowBytes + dx] >> n & 1];
                }

                i += this.width - rect.width;
            }
        } else {
            dx = 0;

            for (dy = rect.y; dy < rect.y + rect.height; ++dy) {
                for (n = rect.x; n < rect.x + rect.width; ++n) {
                    i = dy * this.width + n;
                    this.pixels[i] = palette[buffer[dx++] & 255];
                }
            }
        }

    }

    public synchronized void copyRect(int srcX, int srcY, FramebufferUpdateRectangle dstRect) {
        int startSrcY;
        int endSrcY;
        int dstY;
        byte deltaY;
        if (srcY > dstRect.y) {
            startSrcY = srcY;
            endSrcY = srcY + dstRect.height;
            dstY = dstRect.y;
            deltaY = 1;
        } else {
            startSrcY = srcY + dstRect.height - 1;
            endSrcY = srcY - 1;
            dstY = dstRect.y + dstRect.height - 1;
            deltaY = -1;
        }

        for (int y = startSrcY; y != endSrcY; y += deltaY) {
            System.arraycopy(this.pixels, y * this.width + srcX, this.pixels, dstY * this.width + dstRect.x, dstRect.width);
            dstY += deltaY;
        }

    }

    public void fillRect(int color, FramebufferUpdateRectangle rect) {
        this.fillRect(color, rect.x, rect.y, rect.width, rect.height);
    }

    public synchronized void fillRect(int color, int x, int y, int width, int height) {
        int sy = y * this.width + x;
        int ey = sy + height * this.width;

        for (int i = sy; i < ey; i += this.width) {
            Arrays.fill(this.pixels, i, i + width, color);
        }

    }

    public int readPixelColor(Reader reader) throws TransportException {
        return this.colorDecoder.readColor(reader);
    }

    public int readTightPixelColor(Reader reader) throws TransportException {
        return this.colorDecoder.readTightColor(reader);
    }

    public ColorDecoder getColorDecoder() {
        return this.colorDecoder;
    }

    public int getCompactPixelColor(byte[] bytes, int offset) {
        return this.colorDecoder.getCompactColor(bytes, offset);
    }

    public int getPixelColor(byte[] bytes, int offset) {
        return this.colorDecoder.getColor(bytes, offset);
    }

    public int getBytesPerPixel() {
        return this.bytesPerPixel;
    }

    public int getBytesPerPixelSignificant() {
        return this.bytesPerPixelSignificant;
    }

    public void fillColorBitmapWithColor(int[] bitmapData, int decodedOffset, int rlength, int color) {
        while (rlength-- > 0) {
            bitmapData[decodedOffset++] = color;
        }

    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void createCursor(int[] cursorPixels, FramebufferUpdateRectangle rect) throws TransportException {
        synchronized (this.cursor) {
            this.cursor.createCursor(cursorPixels, rect.x, rect.y, rect.width, rect.height);
        }
    }

    public void decodeCursorPosition(FramebufferUpdateRectangle rect) {
        synchronized (this.cursor) {
            this.cursor.updatePosition(rect.x, rect.y);
        }
    }
}
