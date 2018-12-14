package com.glavsoft.rfb.protocol;

import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.rfb.CapabilityContainer;
import com.glavsoft.rfb.IChangeSettingsListener;
import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.protocol.auth.SecurityType;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class ProtocolSettings implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final EncodingType DEFAULT_PREFERRED_ENCODING;
    public static final int DEFAULT_JPEG_QUALITY = 5;
    private static final int DEFAULT_COMPRESSION_LEVEL = 9;
    public static final int BPP_32 = 32;
    public static final int BPP_16 = 16;
    public static final int BPP_8 = 8;
    public static final int BPP_6 = 6;
    public static final int BPP_3 = 3;
    public static final int BPP_SERVER_SETTINGS = 0;
    private static final int DEFAULT_BITS_PER_PIXEL = 32;
    public static final int CHANGED_VIEW_ONLY = 1;
    public static final int CHANGED_ENCODINGS = 2;
    public static final int CHANGED_ALLOW_COPY_RECT = 4;
    public static final int CHANGED_SHOW_REMOTE_CURSOR = 8;
    public static final int CHANGED_MOUSE_CURSOR_TRACK = 16;
    public static final int CHANGED_COMPRESSION_LEVEL = 32;
    public static final int CHANGED_JPEG_QUALITY = 64;
    public static final int CHANGED_ALLOW_CLIPBOARD_TRANSFER = 128;
    public static final int CHANGED_CONVERT_TO_ASCII = 256;
    public static final int CHANGED_BITS_PER_PIXEL = 512;
    private transient int changedSettingsMask;
    private boolean sharedFlag;
    private boolean viewOnly;
    private EncodingType preferredEncoding;
    private boolean allowCopyRect;
    private boolean showRemoteCursor;
    private LocalPointer mouseCursorTrack;
    private int compressionLevel;
    private int jpegQuality;
    private boolean allowClipboardTransfer;
    private boolean convertToAscii;
    private int bitsPerPixel;
    public transient LinkedHashSet encodings;
    private final transient List listeners;
    public transient CapabilityContainer tunnelingCapabilities;
    public transient CapabilityContainer authCapabilities;
    public transient CapabilityContainer serverMessagesCapabilities;
    public transient CapabilityContainer clientMessagesCapabilities;
    public transient CapabilityContainer encodingTypesCapabilities;
    private transient String remoteCharsetName;

    public static ProtocolSettings getDefaultSettings() {
        ProtocolSettings settings = new ProtocolSettings();
        settings.initKnownAuthCapabilities(settings.authCapabilities);
        settings.initKnownEncodingTypesCapabilities(settings.encodingTypesCapabilities);
        return settings;
    }

    public static ProtocolSettings getHighQualitySettings() {
        ProtocolSettings settings = new ProtocolSettings();
        settings.initKnownAuthCapabilities(settings.authCapabilities);
        settings.initKnownEncodingTypesCapabilities(settings.encodingTypesCapabilities);
        settings.bitsPerPixel = 16;
        return settings;
    }

    public static ProtocolSettings getLowQualitySettings() {
        ProtocolSettings settings = new ProtocolSettings();
        settings.initKnownAuthCapabilities(settings.authCapabilities);
        settings.initKnownEncodingTypesCapabilities(settings.encodingTypesCapabilities);
        settings.bitsPerPixel = 3;
        return settings;
    }

    private ProtocolSettings() {
        this.sharedFlag = true;
        this.viewOnly = false;
        this.showRemoteCursor = true;
        this.mouseCursorTrack = LocalPointer.ON;
        this.preferredEncoding = DEFAULT_PREFERRED_ENCODING;
        this.allowCopyRect = true;
        this.compressionLevel = 9;
        this.jpegQuality = 5;
        this.convertToAscii = false;
        this.allowClipboardTransfer = true;
        this.bitsPerPixel = 0;
        this.refine();
        this.listeners = new LinkedList();
        this.tunnelingCapabilities = new CapabilityContainer();
        this.authCapabilities = new CapabilityContainer();
        this.serverMessagesCapabilities = new CapabilityContainer();
        this.clientMessagesCapabilities = new CapabilityContainer();
        this.encodingTypesCapabilities = new CapabilityContainer();
        this.changedSettingsMask = 0;
    }

    public ProtocolSettings(ProtocolSettings s) {
        this();
        this.copySerializedFieldsFrom(s);
        this.changedSettingsMask = s.changedSettingsMask;
        this.encodings = s.encodings;
    }

    public void copySerializedFieldsFrom(ProtocolSettings s) {
        if (null != s) {
            this.setSharedFlag(s.sharedFlag);
            this.setViewOnly(s.viewOnly);
            this.setAllowCopyRect(s.allowCopyRect);
            this.setShowRemoteCursor(s.showRemoteCursor);
            this.setAllowClipboardTransfer(s.allowClipboardTransfer);
            this.setMouseCursorTrack(s.mouseCursorTrack);
            this.setCompressionLevel(s.compressionLevel);
            this.setJpegQuality(s.jpegQuality);
            this.setConvertToAscii(s.convertToAscii);
            this.setBitsPerPixel(s.bitsPerPixel);
            this.setPreferredEncoding(s.preferredEncoding);
        }
    }

    private void initKnownAuthCapabilities(CapabilityContainer cc) {
        cc.addEnabled(SecurityType.NONE_AUTHENTICATION.getId(), "STDV", "NOAUTH__");
        cc.addEnabled(SecurityType.VNC_AUTHENTICATION.getId(), "STDV", "VNCAUTH_");
    }

    private void initKnownEncodingTypesCapabilities(CapabilityContainer cc) {
        cc.add(EncodingType.COPY_RECT.getId(), "STDV", "COPYRECT");
        cc.add(EncodingType.HEXTILE.getId(), "STDV", "HEXTILE_");
        cc.add(EncodingType.ZLIB.getId(), "TRDV", "ZLIB____");
        cc.add(EncodingType.ZRLE.getId(), "TRDV", "ZRLE____");
        cc.add(EncodingType.RRE.getId(), "STDV", "RRE_____");
        cc.add(EncodingType.TIGHT.getId(), "TGHT", "TIGHT___");
        cc.add(EncodingType.RICH_CURSOR.getId(), "TGHT", "RCHCURSR");
        cc.add(EncodingType.CURSOR_POS.getId(), "TGHT", "POINTPOS");
        cc.add(EncodingType.DESKTOP_SIZE.getId(), "TGHT", "NEWFBSIZ");
    }

    public void addListener(IChangeSettingsListener listener) {
        this.listeners.add(listener);
    }

    public byte getSharedFlag() {
        return (byte) (this.sharedFlag ? 1 : 0);
    }

    public boolean isShared() {
        return this.sharedFlag;
    }

    public void setSharedFlag(boolean sharedFlag) {
        this.sharedFlag = sharedFlag;
    }

    public boolean isViewOnly() {
        return this.viewOnly;
    }

    public void setViewOnly(boolean viewOnly) {
        if (this.viewOnly != viewOnly) {
            this.viewOnly = viewOnly;
            this.changedSettingsMask |= 1;
        }

    }

    public void enableAllEncodingCaps() {
        this.encodingTypesCapabilities.setAllEnable(true);
    }

    public int getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    public void setBitsPerPixel(int bpp) {
        if (this.bitsPerPixel != bpp) {
            this.changedSettingsMask |= 512;
            switch (bpp) {
                case 0:
                case 3:
                case 6:
                case 8:
                case 16:
                case 32:
                    this.bitsPerPixel = bpp;
                    break;
                default:
                    this.bitsPerPixel = 32;
            }

            this.refine();
        }

    }

    public void refine() {
        LinkedHashSet encodings = new LinkedHashSet();
        if (EncodingType.RAW_ENCODING != this.preferredEncoding) {
            encodings.add(this.preferredEncoding);
            encodings.addAll(EncodingType.ordinaryEncodings);
            if (this.compressionLevel > 0 && this.compressionLevel < 10) {
                encodings.add(EncodingType.byId(EncodingType.COMPRESS_LEVEL_0.getId() + this.compressionLevel));
            }

            if (this.jpegQuality > 0 && this.jpegQuality < 10 && (this.bitsPerPixel == 32 || this.bitsPerPixel == 0)) {
                encodings.add(EncodingType.byId(EncodingType.JPEG_QUALITY_LEVEL_0.getId() + this.jpegQuality));
            }

            if (this.allowCopyRect) {
                encodings.add(EncodingType.COPY_RECT);
            }
        }

        switch (this.mouseCursorTrack) {
            case OFF:
                this.setShowRemoteCursor(false);
                break;
            case HIDE:
                this.setShowRemoteCursor(false);
                encodings.add(EncodingType.RICH_CURSOR);
                encodings.add(EncodingType.CURSOR_POS);
                break;
            case ON:
            default:
                this.setShowRemoteCursor(true);
                encodings.add(EncodingType.RICH_CURSOR);
                encodings.add(EncodingType.CURSOR_POS);
        }

        encodings.add(EncodingType.DESKTOP_SIZE);
        if (this.isEncodingsChanged(this.encodings, encodings) || this.isChangedEncodings()) {
            this.encodings = encodings;
            this.changedSettingsMask |= 2;
        }

    }

    private boolean isEncodingsChanged(LinkedHashSet encodings1, LinkedHashSet encodings2) {
        if (null != encodings1 && encodings1.size() == encodings2.size()) {
            Iterator it1 = encodings1.iterator();
            Iterator it2 = encodings2.iterator();

            EncodingType v1;
            EncodingType v2;
            do {
                if (!it1.hasNext()) {
                    return false;
                }

                v1 = (EncodingType) it1.next();
                v2 = (EncodingType) it2.next();
            } while (v1 == v2);

            return true;
        } else {
            return true;
        }
    }

    public void fireListeners() {
        SettingsChangedEvent event = new SettingsChangedEvent(new ProtocolSettings(this));
        this.changedSettingsMask = 0;
        Iterator i$ = this.listeners.iterator();

        while (i$.hasNext()) {
            IChangeSettingsListener listener = (IChangeSettingsListener) i$.next();
            listener.settingsChanged(event);
        }

    }

    public static boolean isRfbSettingsChangedFired(SettingsChangedEvent event) {
        return event.getSource() instanceof ProtocolSettings;
    }

    public void setPreferredEncoding(EncodingType preferredEncoding) {
        if (this.preferredEncoding != preferredEncoding) {
            this.preferredEncoding = preferredEncoding;
            this.changedSettingsMask |= 2;
            this.refine();
        }

    }

    public EncodingType getPreferredEncoding() {
        return this.preferredEncoding;
    }

    public void setAllowCopyRect(boolean allowCopyRect) {
        if (this.allowCopyRect != allowCopyRect) {
            this.allowCopyRect = allowCopyRect;
            this.changedSettingsMask |= 4;
            this.refine();
        }

    }

    public boolean isAllowCopyRect() {
        return this.allowCopyRect;
    }

    private void setShowRemoteCursor(boolean showRemoteCursor) {
        if (this.showRemoteCursor != showRemoteCursor) {
            this.showRemoteCursor = showRemoteCursor;
            this.changedSettingsMask |= 8;
        }

    }

    public boolean isShowRemoteCursor() {
        return this.showRemoteCursor;
    }

    public void setMouseCursorTrack(LocalPointer mouseCursorTrack) {
        if (this.mouseCursorTrack != mouseCursorTrack) {
            this.mouseCursorTrack = mouseCursorTrack;
            this.changedSettingsMask |= 16;
            this.refine();
        }

    }

    public LocalPointer getMouseCursorTrack() {
        return this.mouseCursorTrack;
    }

    public void setCompressionLevel(int compressionLevel) {
        if (this.compressionLevel != compressionLevel) {
            this.compressionLevel = compressionLevel;
            this.changedSettingsMask |= 32;
            this.refine();
        }

    }

    public int getCompressionLevel() {
        return this.compressionLevel;
    }

    public void setJpegQuality(int jpegQuality) {
        if (this.jpegQuality != jpegQuality) {
            this.jpegQuality = jpegQuality;
            this.changedSettingsMask |= 64;
            this.refine();
        }

    }

    public int getJpegQuality() {
        return this.jpegQuality;
    }

    public void setAllowClipboardTransfer(boolean enable) {
        if (this.allowClipboardTransfer != enable) {
            this.allowClipboardTransfer = enable;
            this.changedSettingsMask |= 128;
        }

    }

    public boolean isAllowClipboardTransfer() {
        return this.allowClipboardTransfer;
    }

    public boolean isConvertToAscii() {
        return this.convertToAscii;
    }

    public void setConvertToAscii(boolean convertToAscii) {
        if (this.convertToAscii != convertToAscii) {
            this.convertToAscii = convertToAscii;
            this.changedSettingsMask |= 256;
        }

    }

    public boolean isChangedEncodings() {
        return (this.changedSettingsMask & 2) == 2;
    }

    public boolean changedBitsPerPixel() {
        return (this.changedSettingsMask & 512) == 512;
    }

    public void setRemoteCharsetName(String remoteCharsetName) {
        this.remoteCharsetName = remoteCharsetName;
    }

    public String getRemoteCharsetName() {
        return this.remoteCharsetName;
    }

    static {
        DEFAULT_PREFERRED_ENCODING = EncodingType.TIGHT;
    }
}
