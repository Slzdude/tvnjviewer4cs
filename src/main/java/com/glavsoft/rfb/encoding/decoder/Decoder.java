package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public abstract class Decoder {
    public Decoder() {
    }

    public abstract void decode(Reader var1, Renderer var2, FramebufferUpdateRectangle var3) throws TransportException;

    public void reset() {
    }
}
