package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public class HextileDecoder extends Decoder {
    private static final int DEFAULT_TILE_SIZE = 16;
    private static final int RAW_MASK = 1;
    private static final int BACKGROUND_SPECIFIED_MASK = 2;
    private static final int FOREGROUND_SPECIFIED_MASK = 4;
    private static final int ANY_SUBRECTS_MASK = 8;
    private static final int SUBRECTS_COLOURED_MASK = 16;
    private static final int FG_COLOR_INDEX = 0;
    private static final int BG_COLOR_INDEX = 1;

    public HextileDecoder() {
    }

    public void decode(Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        if (rect.width != 0 && rect.height != 0) {
            int[] colors = new int[]{-1, -1};
            int maxX = rect.x + rect.width;
            int maxY = rect.y + rect.height;

            for (int tileY = rect.y; tileY < maxY; tileY += 16) {
                int tileHeight = Math.min(maxY - tileY, 16);

                for (int tileX = rect.x; tileX < maxX; tileX += 16) {
                    int tileWidth = Math.min(maxX - tileX, 16);
                    this.decodeHextileSubrectangle(reader, renderer, colors, tileX, tileY, tileWidth, tileHeight);
                }
            }

        }
    }

    private void decodeHextileSubrectangle(Reader reader, Renderer renderer, int[] colors, int tileX, int tileY, int tileWidth, int tileHeight) throws TransportException {
        int subencoding = reader.readUInt8();
        if ((subencoding & 1) != 0) {
            RawDecoder.getInstance().decode(reader, renderer, tileX, tileY, tileWidth, tileHeight);
        } else {
            if ((subencoding & 2) != 0) {
                colors[1] = renderer.readPixelColor(reader);
            }

            assert colors[1] != -1;

            renderer.fillRect(colors[1], tileX, tileY, tileWidth, tileHeight);
            if ((subencoding & 4) != 0) {
                colors[0] = renderer.readPixelColor(reader);
            }

            if ((subencoding & 8) != 0) {
                int numberOfSubrectangles = reader.readUInt8();
                boolean colorSpecified = (subencoding & 16) != 0;

                for (int i = 0; i < numberOfSubrectangles; ++i) {
                    if (colorSpecified) {
                        colors[0] = renderer.readPixelColor(reader);
                    }

                    byte dimensions = reader.readByte();
                    int subtileX = dimensions >> 4 & 15;
                    int subtileY = dimensions & 15;
                    dimensions = reader.readByte();
                    int subtileWidth = 1 + (dimensions >> 4 & 15);
                    int subtileHeight = 1 + (dimensions & 15);

                    assert colors[0] != -1;

                    renderer.fillRect(colors[0], tileX + subtileX, tileY + subtileY, subtileWidth, subtileHeight);
                }

            }
        }
    }
}
