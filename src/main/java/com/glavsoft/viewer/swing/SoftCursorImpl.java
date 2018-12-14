package com.glavsoft.viewer.swing;

import com.glavsoft.drawing.SoftCursor;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;

public class SoftCursorImpl extends SoftCursor {
    private Image cursorImage;

    public SoftCursorImpl(int hotX, int hotY, int width, int height) {
        super(hotX, hotY, width, height);
    }

    public Image getImage() {
        return this.cursorImage;
    }

    protected void createNewCursorImage(int[] cursorPixels, int hotX, int hotY, int width, int height) {
        this.cursorImage = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, cursorPixels, 0, width));
    }
}
