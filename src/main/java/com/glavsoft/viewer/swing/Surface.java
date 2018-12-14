package com.glavsoft.viewer.swing;

import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.drawing.Renderer;
import com.glavsoft.rfb.IChangeSettingsListener;
import com.glavsoft.rfb.IRepaintController;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.transport.Reader;
import com.glavsoft.viewer.Viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.ImageObserver;
import javax.swing.JPanel;

public class Surface extends JPanel implements IRepaintController, IChangeSettingsListener {
    private int width;
    private int height;
    private SoftCursorImpl cursor;
    private RendererImpl renderer;
    private MouseEventListener mouseEventListener;
    private KeyEventListener keyEventListener;
    private boolean showCursor;
    private ModifierButtonEventListener modifierButtonListener;
    private boolean isUserInputEnabled = false;
    private final ProtocolContext context;
    private double scaleFactor;
    private final Viewer viewer;
    public Dimension oldSize;

    public boolean isDoubleBuffered() {
        return false;
    }

    public Surface(ProtocolContext context, Viewer viewer, double scaleFactor) {
        this.context = context;
        this.viewer = viewer;
        this.scaleFactor = scaleFactor;
        this.init(context.getFbWidth(), context.getFbHeight());
        this.oldSize = this.getPreferredSize();
        this.showCursor = context.getSettings().isShowRemoteCursor();
    }

    public void setViewOnly(boolean really) {
        this.setUserInputEnabled(!really, this.context.getSettings().isConvertToAscii());
    }

    private void setUserInputEnabled(boolean enable, boolean convertToAscii) {
        if (enable != this.isUserInputEnabled) {
            this.isUserInputEnabled = enable;
            if (enable) {
                if (null == this.mouseEventListener) {
                    this.mouseEventListener = new MouseEventListener(this, this.context, this.scaleFactor);
                }

                this.addMouseListener(this.mouseEventListener);
                this.addMouseMotionListener(this.mouseEventListener);
                this.addMouseWheelListener(this.mouseEventListener);
                this.setFocusTraversalKeysEnabled(false);
                if (null == this.keyEventListener) {
                    this.keyEventListener = new KeyEventListener(this.context);
                    if (this.modifierButtonListener != null) {
                        this.keyEventListener.addModifierListener(this.modifierButtonListener);
                    }
                }

                this.keyEventListener.setConvertToAscii(convertToAscii);
                this.addKeyListener(this.keyEventListener);
                this.enableInputMethods(false);
            } else {
                this.removeMouseListener(this.mouseEventListener);
                this.removeMouseMotionListener(this.mouseEventListener);
                this.removeMouseWheelListener(this.mouseEventListener);
                this.removeKeyListener(this.keyEventListener);
            }

        }
    }

    public Renderer createRenderer(Reader reader, int width, int height, PixelFormat pixelFormat) {
        this.renderer = new RendererImpl(reader, width, height, pixelFormat);
        synchronized (this.renderer) {
            this.cursor = this.renderer.getCursor();
        }

        this.init(this.renderer.getWidth(), this.renderer.getHeight());
        this.updateFrameSize();
        return this.renderer;
    }

    private void init(int width, int height) {
        this.width = width;
        this.height = height;
        this.setSize(this.getPreferredSize());
    }

    private void updateFrameSize() {
        this.setSize(this.getPreferredSize());
        this.viewer.packContainer();
        this.requestFocus();
    }

    public void paintComponent(Graphics g) {
        if (this.renderer != null) {
            ((Graphics2D) g).scale(this.scaleFactor, this.scaleFactor);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            Image cursorImage;
            synchronized (this.renderer) {
                cursorImage = this.renderer.getOffscreenImage();
                if (cursorImage != null) {
                    g.drawImage(cursorImage, 0, 0, (ImageObserver) null);
                }
            }

            synchronized (this.cursor) {
                cursorImage = this.cursor.getImage();
                if (this.showCursor && cursorImage != null && (this.scaleFactor != 1.0D || g.getClipBounds().intersects((double) this.cursor.rX, (double) this.cursor.rY, (double) this.cursor.width, (double) this.cursor.height))) {
                    g.drawImage(cursorImage, this.cursor.rX, this.cursor.rY, (ImageObserver) null);
                }

            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension((int) ((double) this.width * this.scaleFactor), (int) ((double) this.height * this.scaleFactor));
    }

    public Dimension getMinimumSize() {
        return this.getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return this.getPreferredSize();
    }

    public void repaintBitmap(FramebufferUpdateRectangle rect) {
        this.repaintBitmap(rect.x, rect.y, rect.width, rect.height);
    }

    public void repaintBitmap(int x, int y, int width, int height) {
        this.repaint((int) ((double) x * this.scaleFactor), (int) ((double) y * this.scaleFactor), (int) Math.ceil((double) width * this.scaleFactor), (int) Math.ceil((double) height * this.scaleFactor));
    }

    public void repaintCursor() {
        synchronized (this.cursor) {
            this.repaint((int) ((double) this.cursor.oldRX * this.scaleFactor), (int) ((double) this.cursor.oldRY * this.scaleFactor), (int) Math.ceil((double) this.cursor.oldWidth * this.scaleFactor) + 1, (int) Math.ceil((double) this.cursor.oldHeight * this.scaleFactor) + 1);
            this.repaint((int) ((double) this.cursor.rX * this.scaleFactor), (int) ((double) this.cursor.rY * this.scaleFactor), (int) Math.ceil((double) this.cursor.width * this.scaleFactor) + 1, (int) Math.ceil((double) this.cursor.height * this.scaleFactor) + 1);
        }
    }

    public void updateCursorPosition(short x, short y) {
        synchronized (this.cursor) {
            this.cursor.updatePosition(x, y);
            this.repaintCursor();
        }
    }

    private void showCursor(boolean show) {
        synchronized (this.cursor) {
            this.showCursor = show;
        }
    }

    public void addModifierListener(ModifierButtonEventListener modifierButtonListener) {
        this.modifierButtonListener = modifierButtonListener;
        if (this.keyEventListener != null) {
            this.keyEventListener.addModifierListener(modifierButtonListener);
        }

    }

    public void settingsChanged(SettingsChangedEvent e) {
        if (ProtocolSettings.isRfbSettingsChangedFired(e)) {
            ProtocolSettings settings = (ProtocolSettings) e.getSource();
            this.setUserInputEnabled(!settings.isViewOnly(), settings.isConvertToAscii());
            this.showCursor(settings.isShowRemoteCursor());
        } else if (UiSettings.isUiSettingsChangedFired(e)) {
            UiSettings settings = (UiSettings) e.getSource();
            this.oldSize = this.getPreferredSize();
            this.scaleFactor = settings.getScaleFactor();
        }

        if (this.mouseEventListener != null) {
            this.mouseEventListener.setScaleFactor(this.scaleFactor);
        }

        this.updateFrameSize();
    }

    public void setPixelFormat(PixelFormat pixelFormat) {
        if (this.renderer != null) {
            this.renderer.initPixelFormat(pixelFormat);
        }

    }
}
