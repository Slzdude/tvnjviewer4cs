package com.glavsoft.rfb.protocol.state;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedProtocolVersionException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.CapabilityContainer;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.auth.AuthHandler;
import com.glavsoft.rfb.protocol.auth.SecurityType;
import com.glavsoft.utils.Strings;

public class SecurityTypeState extends ProtocolState {
    public SecurityTypeState(ProtocolContext context) {
        super(context);
    }

    public boolean next() throws UnsupportedProtocolVersionException, TransportException, UnsupportedSecurityTypeException {
        this.negotiateAboutSecurityType();
        return true;
    }

    protected void negotiateAboutSecurityType() throws TransportException, UnsupportedSecurityTypeException {
        int secTypesNum = this.reader.readUInt8();
        if (0 == secTypesNum) {
            throw new UnsupportedSecurityTypeException(this.reader.readString());
        } else {
            byte[] secTypes = this.reader.readBytes(secTypesNum);
            this.logger.info("Security Types received (" + secTypesNum + "): " + Strings.toString(secTypes));
            AuthHandler typeSelected = selectAuthHandler(secTypes, this.context.getSettings().authCapabilities);
            this.setUseSecurityResult(typeSelected);
            this.writer.writeByte(typeSelected.getId());
            this.logger.info("Security Type accepted: " + typeSelected.getName());
            this.changeStateTo(new AuthenticationState(this.context, typeSelected));
        }
    }

    public static AuthHandler selectAuthHandler(byte[] secTypes, CapabilityContainer authCapabilities) throws UnsupportedSecurityTypeException {
        AuthHandler typeSelected = null;
        byte[] arr$ = secTypes;
        int len$ = secTypes.length;

        int i$;
        byte type;
        for (i$ = 0; i$ < len$; ++i$) {
            type = arr$[i$];
            if (SecurityType.TIGHT_AUTHENTICATION.getId() == (255 & type)) {
                typeSelected = (AuthHandler) SecurityType.implementedSecurityTypes.get(SecurityType.TIGHT_AUTHENTICATION.getId());
                if (typeSelected != null) {
                    return typeSelected;
                }
            }
        }

        arr$ = secTypes;
        len$ = secTypes.length;

        for (i$ = 0; i$ < len$; ++i$) {
            type = arr$[i$];
            typeSelected = (AuthHandler) SecurityType.implementedSecurityTypes.get(255 & type);
            if (typeSelected != null && authCapabilities.isSupported(typeSelected.getId())) {
                return typeSelected;
            }
        }

        throw new UnsupportedSecurityTypeException("No security types supported. Server sent '" + Strings.toString(secTypes) + "' security types, but we do not support any of their.");
    }

    protected void setUseSecurityResult(AuthHandler typeSelected) {
    }
}
