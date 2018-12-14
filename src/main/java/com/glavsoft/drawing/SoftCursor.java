package com.glavsoft.drawing;

public abstract class SoftCursor {
    protected int hotX;
    protected int hotY;
    private int x;
    private int y;
    public int width;
    public int height;
    public int rX;
    public int rY;
    public int oldRX;
    public int oldRY;
    public int oldWidth;
    public int oldHeight;

    public SoftCursor(int hotX, int hotY, int width, int height) {
        this.hotX = hotX;
        this.hotY = hotY;
        this.oldWidth = this.width = width;
        this.oldHeight = this.height = height;
        this.oldRX = this.rX = 0;
        this.oldRY = this.rY = 0;
    }

    public void updatePosition(int newX, int newY) {
        this.oldRX = this.rX;
        this.oldRY = this.rY;
        this.oldWidth = this.width;
        this.oldHeight = this.height;
        this.x = newX;
        this.y = newY;
        this.rX = this.x - this.hotX;
        this.rY = this.y - this.hotY;
    }

    public void setNewDimensions(int hotX, int hotY, int width, int height) {
        this.hotX = hotX;
        this.hotY = hotY;
        this.oldWidth = this.width;
        this.oldHeight = this.height;
        this.oldRX = this.rX;
        this.oldRY = this.rY;
        this.rX = this.x - hotX;
        this.rY = this.y - hotY;
        this.width = width;
        this.height = height;
    }

    public void createCursor(int[] cursorPixels, int hotX, int hotY, int width, int height) {
        this.createNewCursorImage(cursorPixels, hotX, hotY, width, height);
        this.setNewDimensions(hotX, hotY, width, height);
    }

    protected abstract void createNewCursorImage(int[] var1, int var2, int var3, int var4, int var5);
}
