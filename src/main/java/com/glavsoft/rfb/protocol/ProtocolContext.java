package com.glavsoft.rfb.protocol;

import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.rfb.client.ClientToServerMessage;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.protocol.state.ProtocolState;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;

import java.util.logging.Logger;

public interface ProtocolContext {
    void changeStateTo(ProtocolState var1);

    IPasswordRetriever getPasswordRetriever();

    ProtocolSettings getSettings();

    Logger getLogger();

    Writer getWriter();

    Reader getReader();

    int getFbWidth();

    void setFbWidth(int var1);

    int getFbHeight();

    void setFbHeight(int var1);

    PixelFormat getPixelFormat();

    void setPixelFormat(PixelFormat var1);

    void setRemoteDesktopName(String var1);

    void sendMessage(ClientToServerMessage var1);

    String getRemoteDesktopName();

    void sendRefreshMessage();

    void cleanUpSession(String var1);

    void setTight(boolean var1);

    boolean isTight();

    void setProtocolVersion(String var1);

    String getProtocolVersion();
}
