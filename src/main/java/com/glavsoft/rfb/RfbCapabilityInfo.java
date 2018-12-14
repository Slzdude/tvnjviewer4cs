package com.glavsoft.rfb;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Reader;

public class RfbCapabilityInfo {
    public static final String VENDOR_STANDARD = "STDV";
    public static final String VENDOR_TRIADA = "TRDV";
    public static final String VENDOR_TIGHT = "TGHT";
    public static final String TUNNELING_NO_TUNNELING = "NOTUNNEL";
    public static final String AUTHENTICATION_NO_AUTH = "NOAUTH__";
    public static final String AUTHENTICATION_VNC_AUTH = "VNCAUTH_";
    public static final String ENCODING_COPYRECT = "COPYRECT";
    public static final String ENCODING_HEXTILE = "HEXTILE_";
    public static final String ENCODING_ZLIB = "ZLIB____";
    public static final String ENCODING_ZRLE = "ZRLE____";
    public static final String ENCODING_RRE = "RRE_____";
    public static final String ENCODING_TIGHT = "TIGHT___";
    public static final String ENCODING_RICH_CURSOR = "RCHCURSR";
    public static final String ENCODING_CURSOR_POS = "POINTPOS";
    public static final String ENCODING_DESKTOP_SIZE = "NEWFBSIZ";
    private int code;
    private String vendorSignature;
    private String nameSignature;
    private boolean enable;

    public RfbCapabilityInfo(int code, String vendorSignature, String nameSignature) {
        this.code = code;
        this.vendorSignature = vendorSignature;
        this.nameSignature = nameSignature;
        this.enable = true;
    }

    public RfbCapabilityInfo(Reader reader) throws TransportException {
        this.code = reader.readInt32();
        this.vendorSignature = reader.readString(4);
        this.nameSignature = reader.readString(8);
    }

    public boolean equals(Object otherObj) {
        if (this == otherObj) {
            return true;
        } else if (null == otherObj) {
            return false;
        } else if (this.getClass() != otherObj.getClass()) {
            return false;
        } else {
            RfbCapabilityInfo other = (RfbCapabilityInfo) otherObj;
            return this.code == other.code && this.vendorSignature.equals(other.vendorSignature) && this.nameSignature.equals(other.nameSignature);
        }
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getCode() {
        return this.code;
    }

    public String getVendorSignature() {
        return this.vendorSignature;
    }

    public String getNameSignature() {
        return this.nameSignature;
    }

    public boolean isEnabled() {
        return this.enable;
    }

    public String toString() {
        return "RfbCapabilityInfo: [code: " + this.code + ", vendor: " + this.vendorSignature + ", name: " + this.nameSignature + "]";
    }
}
