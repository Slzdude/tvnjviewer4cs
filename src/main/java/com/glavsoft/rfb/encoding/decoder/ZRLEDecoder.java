package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public class ZRLEDecoder extends ZlibDecoder {
    private static final int DEFAULT_TILE_SIZE = 64;

    public ZRLEDecoder() {
    }

    public void decode(Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int zippedLength = (int) reader.readUInt32();
        if (0 != zippedLength) {
            int length = rect.width * rect.height * renderer.getBytesPerPixel();
            byte[] bytes = this.unzip(reader, zippedLength, length);
            int offset = zippedLength;
            int maxX = rect.x + rect.width;
            int maxY = rect.y + rect.height;
            int[] palette = new int[128];

            for (int tileY = rect.y; tileY < maxY; tileY += 64) {
                int tileHeight = Math.min(maxY - tileY, 64);

                for (int tileX = rect.x; tileX < maxX; tileX += 64) {
                    int tileWidth = Math.min(maxX - tileX, 64);
                    int subencoding = bytes[offset++] & 255;
                    boolean isRle = (subencoding & 128) != 0;
                    int paletteSize = subencoding & 127;
                    offset += this.readPalette(bytes, offset, renderer, palette, paletteSize);
                    if (1 == subencoding) {
                        renderer.fillRect(palette[0], tileX, tileY, tileWidth, tileHeight);
                    } else if (isRle) {
                        if (0 == paletteSize) {
                            offset += this.decodePlainRle(bytes, offset, renderer, tileX, tileY, tileWidth, tileHeight);
                        } else {
                            offset += this.decodePaletteRle(bytes, offset, renderer, palette, tileX, tileY, tileWidth, tileHeight, paletteSize);
                        }
                    } else if (0 == paletteSize) {
                        offset += this.decodeRaw(bytes, offset, renderer, tileX, tileY, tileWidth, tileHeight);
                    } else {
                        offset += this.decodePacked(bytes, offset, renderer, palette, paletteSize, tileX, tileY, tileWidth, tileHeight);
                    }
                }
            }

        }
    }

    private int decodePlainRle(byte[] bytes, int offset, Renderer renderer, int tileX, int tileY, int tileWidth, int tileHeight) {
        int bytesPerCPixel = renderer.getBytesPerPixelSignificant();
        int[] decodedBitmap = new int[tileWidth * tileHeight];
        int decodedOffset = 0;
        int decodedEnd = tileWidth * tileHeight;

        int index;
        int rlength;
        for (index = offset; decodedOffset < decodedEnd; decodedOffset += rlength) {
            int color = renderer.getCompactPixelColor(bytes, index);
            index += bytesPerCPixel;
            rlength = 1;

            do {
                rlength += bytes[index] & 255;
            } while ((bytes[index++] & 255) == 255);

            assert rlength <= decodedEnd - decodedOffset;

            renderer.fillColorBitmapWithColor(decodedBitmap, decodedOffset, rlength, color);
        }

        renderer.drawColoredBitmap(decodedBitmap, tileX, tileY, tileWidth, tileHeight);
        return index - offset;
    }

    private int decodePaletteRle(byte[] bytes, int offset, Renderer renderer, int[] palette, int tileX, int tileY, int tileWidth, int tileHeight, int paletteSize) {
        int[] decodedBitmap = new int[tileWidth * tileHeight];
        int decodedOffset = 0;
        int decodedEnd = tileWidth * tileHeight;

        int index;
        int rlength;
        for (index = offset; decodedOffset < decodedEnd; decodedOffset += rlength) {
            int colorIndex = bytes[index++];
            int color = palette[colorIndex & 127];
            rlength = 1;
            if ((colorIndex & 128) != 0) {
                do {
                    rlength += bytes[index] & 255;
                } while (bytes[index++] == -1);
            }

            assert rlength <= decodedEnd - decodedOffset;

            renderer.fillColorBitmapWithColor(decodedBitmap, decodedOffset, rlength, color);
        }

        renderer.drawColoredBitmap(decodedBitmap, tileX, tileY, tileWidth, tileHeight);
        return index - offset;
    }

    private int decodePacked(byte[] bytes, int offset, Renderer renderer, int[] palette, int paletteSize, int tileX, int tileY, int tileWidth, int tileHeight) {
        int[] decodedBytes = new int[tileWidth * tileHeight];
        int bitsPerPalletedPixel = paletteSize > 16 ? 8 : (paletteSize > 4 ? 4 : (paletteSize > 2 ? 2 : 1));
        int packedOffset = offset;
        int decodedOffset = 0;

        for (int i = 0; i < tileHeight; ++i) {
            int decodedRowEnd = decodedOffset + tileWidth;
            int byteProcessed = 0;

            for (int bitsRemain = 0; decodedOffset < decodedRowEnd; ++decodedOffset) {
                if (bitsRemain == 0) {
                    byteProcessed = bytes[packedOffset++];
                    bitsRemain = 8;
                }

                bitsRemain -= bitsPerPalletedPixel;
                int index = byteProcessed >> bitsRemain & (1 << bitsPerPalletedPixel) - 1 & 127;
                int color = palette[index];
                renderer.fillColorBitmapWithColor(decodedBytes, decodedOffset, 1, color);
            }
        }

        renderer.drawColoredBitmap(decodedBytes, tileX, tileY, tileWidth, tileHeight);
        return packedOffset - offset;
    }

    private int decodeRaw(byte[] bytes, int offset, Renderer renderer, int tileX, int tileY, int tileWidth, int tileHeight) throws TransportException {
        return renderer.drawCompactBytes(bytes, offset, tileX, tileY, tileWidth, tileHeight);
    }

    private int readPalette(byte[] bytes, int offset, Renderer renderer, int[] palette, int paletteSize) {
        for (int i = 0; i < paletteSize; ++i) {
            palette[i] = renderer.getCompactPixelColor(bytes, offset + i * renderer.getBytesPerPixelSignificant());
        }

        return paletteSize * renderer.getBytesPerPixelSignificant();
    }
}
