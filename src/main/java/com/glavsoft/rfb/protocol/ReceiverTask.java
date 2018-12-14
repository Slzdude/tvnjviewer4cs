package com.glavsoft.rfb.protocol;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.CommonException;
import com.glavsoft.exceptions.ProtocolException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.ClipboardController;
import com.glavsoft.rfb.IRepaintController;
import com.glavsoft.rfb.client.FramebufferUpdateRequestMessage;
import com.glavsoft.rfb.client.SetPixelFormatMessage;
import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.Decoder;
import com.glavsoft.rfb.encoding.decoder.DecodersContainer;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.rfb.encoding.decoder.RichCursorDecoder;
import com.glavsoft.transport.Reader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class ReceiverTask implements Runnable {
    private static final byte FRAMEBUFFER_UPDATE = 0;
    private static final byte SET_COLOR_MAP_ENTRIES = 1;
    private static final byte BELL = 2;
    private static final byte SERVER_CUT_TEXT = 3;
    private static Logger logger = Logger.getLogger("com.glavsoft.rfb.protocol.ReceiverTask");
    private final Reader reader;
    private volatile boolean isRunning = false;
    private Renderer renderer;
    private final IRepaintController repaintController;
    private final ClipboardController clipboardController;
    private final DecodersContainer decoders;
    private FramebufferUpdateRequestMessage fullscreenFbUpdateIncrementalRequest;
    private final ProtocolContext context;
    private PixelFormat pixelFormat;
    private boolean needSendPixelFormat;

    public ReceiverTask(Reader reader, IRepaintController repaintController, ClipboardController clipboardController, DecodersContainer decoders, ProtocolContext context) {
        this.reader = reader;
        this.repaintController = repaintController;
        this.clipboardController = clipboardController;
        this.context = context;
        this.decoders = decoders;
        this.renderer = repaintController.createRenderer(reader, context.getFbWidth(), context.getFbHeight(), context.getPixelFormat());
        this.fullscreenFbUpdateIncrementalRequest = new FramebufferUpdateRequestMessage(0, 0, context.getFbWidth(), context.getFbHeight(), true);
    }

    public void run() {
        this.isRunning = true;

        while (this.isRunning) {
            try {
                byte messageId = this.reader.readByte();
                switch (messageId) {
                    case 0:
                        this.framebufferUpdateMessage();
                        break;
                    case 1:
                        logger.severe("Server message SetColorMapEntries is not implemented. Skip.");
                        this.setColorMapEntries();
                        break;
                    case 2:
                        logger.fine("Server message: Bell");
                        System.out.print("\u00007");
                        System.out.flush();
                        break;
                    case 3:
                        logger.fine("Server message: CutText (3)");
                        this.serverCutText();
                        break;
                    default:
                        logger.severe("Unsupported server message. Id = " + messageId);
                }
            } catch (TransportException var4) {
                logger.severe("Close session: " + var4.getMessage());
                if (this.isRunning) {
                    this.context.cleanUpSession("Connection closed.");
                }

                this.stopTask();
            } catch (ProtocolException var5) {
                logger.severe(var5.getMessage());
                if (this.isRunning) {
                    this.context.cleanUpSession(var5.getMessage() + "\nConnection closed.");
                }

                this.stopTask();
            } catch (CommonException var6) {
                logger.severe(var6.getMessage());
                if (this.isRunning) {
                    this.context.cleanUpSession("Connection closed.");
                }

                this.stopTask();
            } catch (Throwable var7) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                var7.printStackTrace(pw);
                if (this.isRunning) {
                    this.context.cleanUpSession(var7.getMessage() + "\n" + sw.toString());
                }

                this.stopTask();
            }
        }

    }

    private void setColorMapEntries() throws TransportException {
        this.reader.readByte();
        this.reader.readUInt16();
        int var1 = this.reader.readUInt16();

        while (var1-- > 0) {
            this.reader.readUInt16();
            this.reader.readUInt16();
            this.reader.readUInt16();
        }

    }

    private void serverCutText() throws TransportException {
        this.reader.readByte();
        this.reader.readInt16();
        int length = this.reader.readInt32() & 2147483647;
        this.clipboardController.updateSystemClipboard(this.reader.readBytes(length));
    }

    public void framebufferUpdateMessage() throws CommonException {
        this.reader.readByte();
        int numberOfRectangles = this.reader.readUInt16();

        while (numberOfRectangles-- > 0) {
            FramebufferUpdateRectangle rect = new FramebufferUpdateRectangle();
            rect.fill(this.reader);
            Decoder decoder = this.decoders.getDecoderByType(rect.getEncodingType());
            logger.finest(rect.toString() + (0 == numberOfRectangles ? "\n---" : ""));
            if (decoder != null) {
                decoder.decode(this.reader, this.renderer, rect);
                this.repaintController.repaintBitmap(rect);
            } else if (rect.getEncodingType() == EncodingType.RICH_CURSOR) {
                RichCursorDecoder.getInstance().decode(this.reader, this.renderer, rect);
                this.repaintController.repaintCursor();
            } else if (rect.getEncodingType() == EncodingType.CURSOR_POS) {
                this.renderer.decodeCursorPosition(rect);
                this.repaintController.repaintCursor();
            } else {
                if (rect.getEncodingType() != EncodingType.DESKTOP_SIZE) {
                    throw new CommonException("Unprocessed encoding: " + rect.toString());
                }

                this.fullscreenFbUpdateIncrementalRequest = new FramebufferUpdateRequestMessage(0, 0, rect.width, rect.height, true);
                synchronized (this.renderer) {
                    this.renderer = this.repaintController.createRenderer(this.reader, rect.width, rect.height, this.context.getPixelFormat());
                }

                this.context.sendMessage(new FramebufferUpdateRequestMessage(0, 0, rect.width, rect.height, false));
            }
        }

        synchronized (this) {
            if (this.needSendPixelFormat) {
                this.needSendPixelFormat = false;
                this.context.setPixelFormat(this.pixelFormat);
                this.context.sendMessage(new SetPixelFormatMessage(this.pixelFormat));
                logger.fine("sent: " + this.pixelFormat);
                this.context.sendRefreshMessage();
                logger.fine("sent: nonincremental fb update");
            } else {
                this.context.sendMessage(this.fullscreenFbUpdateIncrementalRequest);
            }

        }
    }

    public synchronized void queueUpdatePixelFormat(PixelFormat pf) {
        this.pixelFormat = pf;
        this.needSendPixelFormat = true;
    }

    public void stopTask() {
        this.isRunning = false;
    }
}
