package com.glavsoft.viewer.swing;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.transport.Reader;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RendererImpl extends Renderer implements ImageObserver {
    private final Image offscreanImage;
    CyclicBarrier barier = new CyclicBarrier(2);

    public RendererImpl(Reader reader, int width, int height, PixelFormat pixelFormat) {
        if (0 == width) {
            width = 1;
        }

        if (0 == height) {
            height = 1;
        }

        this.init(reader, width, height, pixelFormat);
        ColorModel colorModel = new DirectColorModel(24, 16711680, 65280, 255);
        SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
        DataBuffer dataBuffer = new DataBufferInt(this.pixels, width * height);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, (Point) null);
        this.offscreanImage = new BufferedImage(colorModel, raster, false, (Hashtable) null);
        this.cursor = new SoftCursorImpl(0, 0, 0, 0);
    }

    public void drawJpegImage(byte[] bytes, int offset, int jpegBufferLength, FramebufferUpdateRectangle rect) {
        Image jpegImage = Toolkit.getDefaultToolkit().createImage(bytes, offset, jpegBufferLength);
        Toolkit.getDefaultToolkit().prepareImage(jpegImage, -1, -1, this);

        try {
            this.barier.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException var7) {
        } catch (BrokenBarrierException var8) {
        } catch (TimeoutException var9) {
        }

        Graphics graphics = this.offscreanImage.getGraphics();
        graphics.drawImage(jpegImage, rect.x, rect.y, rect.width, rect.height, this);
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        boolean isReady = (infoflags & 160) != 0;
        if (isReady) {
            try {
                this.barier.await();
            } catch (InterruptedException var9) {
            } catch (BrokenBarrierException var10) {
            }
        }

        return !isReady;
    }

    public Image getOffscreenImage() {
        return this.offscreanImage;
    }

    public SoftCursorImpl getCursor() {
        return (SoftCursorImpl) this.cursor;
    }
}
