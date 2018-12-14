package com.glavsoft.viewer.swing;

import com.glavsoft.rfb.IRepaintController;
import com.glavsoft.rfb.client.PointerEventMessage;
import com.glavsoft.rfb.protocol.ProtocolContext;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.event.MouseInputAdapter;

public class MouseEventListener extends MouseInputAdapter implements MouseWheelListener {
    private static final byte BUTTON_LEFT = 1;
    private static final byte BUTTON_MIDDLE = 2;
    private static final byte BUTTON_RIGHT = 4;
    private static final byte WHEEL_UP = 8;
    private static final byte WHEEL_DOWN = 16;
    private final IRepaintController repaintController;
    private final ProtocolContext context;
    private volatile double scaleFactor;

    public MouseEventListener(IRepaintController repaintController, ProtocolContext context, double scaleFactor) {
        this.repaintController = repaintController;
        this.context = context;
        this.scaleFactor = scaleFactor;
    }

    public void processMouseEvent(MouseEvent mouseEvent, MouseWheelEvent mouseWheelEvent, boolean moved) {
        byte buttonMask = 0;
        if (null == mouseEvent && mouseWheelEvent != null) {
            mouseEvent = mouseWheelEvent;
        }

        assert mouseEvent != null;

        short x = (short) ((int) ((double) ((MouseEvent) mouseEvent).getX() / this.scaleFactor));
        short y = (short) ((int) ((double) ((MouseEvent) mouseEvent).getY() / this.scaleFactor));
        if (moved) {
            this.repaintController.updateCursorPosition(x, y);
        }

        int modifiersEx = ((MouseEvent) mouseEvent).getModifiersEx();
        buttonMask = (byte) (buttonMask | ((modifiersEx & 1024) != 0 ? 1 : 0));
        buttonMask = (byte) (buttonMask | ((modifiersEx & 2048) != 0 ? 2 : 0));
        buttonMask = (byte) (buttonMask | ((modifiersEx & 4096) != 0 ? 4 : 0));
        if (mouseWheelEvent != null) {
            int notches = mouseWheelEvent.getWheelRotation();
            int wheelMask = notches < 0 ? 8 : 16;
            notches = Math.abs(notches);

            for (int i = 1; i < notches; ++i) {
                this.context.sendMessage(new PointerEventMessage((byte) (buttonMask | wheelMask), x, y));
                this.context.sendMessage(new PointerEventMessage(buttonMask, x, y));
            }

            this.context.sendMessage(new PointerEventMessage((byte) (buttonMask | wheelMask), x, y));
        }

        this.context.sendMessage(new PointerEventMessage(buttonMask, x, y));
    }

    public void mousePressed(MouseEvent mouseEvent) {
        this.processMouseEvent(mouseEvent, (MouseWheelEvent) null, false);
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        this.processMouseEvent(mouseEvent, (MouseWheelEvent) null, false);
    }

    public void mouseEntered(MouseEvent mouseEvent) {
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        this.processMouseEvent(mouseEvent, (MouseWheelEvent) null, true);
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        this.processMouseEvent(mouseEvent, (MouseWheelEvent) null, true);
    }

    public void mouseWheelMoved(MouseWheelEvent emouseWheelEvent) {
        this.processMouseEvent((MouseEvent) null, emouseWheelEvent, false);
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }
}
