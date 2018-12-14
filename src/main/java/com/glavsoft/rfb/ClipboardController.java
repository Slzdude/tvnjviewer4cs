package com.glavsoft.rfb;

public interface ClipboardController extends IChangeSettingsListener {
    void updateSystemClipboard(byte[] var1);

    String getRenewedClipboardText();

    String getClipboardText();

    void setEnabled(boolean var1);
}
