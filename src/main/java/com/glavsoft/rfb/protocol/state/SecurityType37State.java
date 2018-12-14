package com.glavsoft.rfb.protocol.state;

import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.auth.AuthHandler;
import com.glavsoft.rfb.protocol.auth.SecurityType;

public class SecurityType37State extends SecurityTypeState {
    public SecurityType37State(ProtocolContext context) {
        super(context);
    }

    protected void setUseSecurityResult(AuthHandler type) {
        if (SecurityType.NONE_AUTHENTICATION == type.getType()) {
            type.setUseSecurityResult(false);
        }

    }
}
