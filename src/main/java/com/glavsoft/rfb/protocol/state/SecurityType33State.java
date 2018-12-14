package com.glavsoft.rfb.protocol.state;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.auth.AuthHandler;

public class SecurityType33State extends SecurityType37State {
    public SecurityType33State(ProtocolContext context) {
        super(context);
    }

    protected void negotiateAboutSecurityType() throws TransportException, UnsupportedSecurityTypeException {
        this.logger.info("Get Security Type");
        int type = this.reader.readInt32();
        this.logger.info("Type received: " + type);
        if (0 == type) {
            throw new UnsupportedSecurityTypeException(this.reader.readString());
        } else {
            AuthHandler typeSelected = selectAuthHandler(new byte[]{(byte) (255 & type)}, this.context.getSettings().authCapabilities);
            if (typeSelected != null) {
                this.setUseSecurityResult(typeSelected);
                this.logger.info("Type accepted: " + typeSelected.getName());
                this.changeStateTo(new AuthenticationState(this.context, typeSelected));
            } else {
                throw new UnsupportedSecurityTypeException("No security types supported. Server sent '" + type + "' security type, but we do not support it.");
            }
        }
    }
}
