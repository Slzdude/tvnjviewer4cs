package com.glavsoft.rfb.protocol.auth;

import com.glavsoft.exceptions.UnsupportedSecurityTypeException;

import java.util.LinkedHashMap;
import java.util.Map;

public enum SecurityType {
    NONE_AUTHENTICATION(1),
    VNC_AUTHENTICATION(2),
    TIGHT_AUTHENTICATION(16);

    private int id;
    public static Map implementedSecurityTypes = new LinkedHashMap() {
        {
            this.put(SecurityType.TIGHT_AUTHENTICATION.getId(), new TightAuthentication());
            this.put(SecurityType.VNC_AUTHENTICATION.getId(), new VncAuthentication());
            this.put(SecurityType.NONE_AUTHENTICATION.getId(), new NoneAuthentication());
        }
    };

    private SecurityType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static AuthHandler getAuthHandlerById(int id) throws UnsupportedSecurityTypeException {
        AuthHandler typeSelected = null;
        typeSelected = (AuthHandler) implementedSecurityTypes.get(id);
        if (null == typeSelected) {
            throw new UnsupportedSecurityTypeException("Not supported: " + id);
        } else {
            return typeSelected;
        }
    }
}
