package com.glavsoft.rfb;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.transport.Reader;

public interface IRepaintController extends IChangeSettingsListener {
    void repaintBitmap(FramebufferUpdateRectangle var1);

    void repaintBitmap(int var1, int var2, int var3, int var4);

    void repaintCursor();

    void updateCursorPosition(short var1, short var2);

    Renderer createRenderer(Reader var1, int var2, int var3, PixelFormat var4);

    void setPixelFormat(PixelFormat var1);
}
