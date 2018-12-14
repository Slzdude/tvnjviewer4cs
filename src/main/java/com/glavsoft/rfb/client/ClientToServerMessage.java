package com.glavsoft.rfb.client;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Writer;

public interface ClientToServerMessage {
    byte SET_PIXEL_FORMAT = 0;
    byte SET_ENCODINGS = 2;
    byte FRAMEBUFFER_UPDATE_REQUEST = 3;
    byte KEY_EVENT = 4;
    byte POINTER_EVENT = 5;
    byte CLIENT_CUT_TEXT = 6;

    void send(Writer var1) throws TransportException;
}
