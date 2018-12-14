package com.glavsoft.rfb.protocol.auth;

import com.glavsoft.exceptions.FatalException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.CapabilityContainer;
import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.rfb.RfbCapabilityInfo;
import com.glavsoft.rfb.protocol.state.SecurityTypeState;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;

import java.util.logging.Logger;

public class TightAuthentication extends AuthHandler {
    public TightAuthentication() {
    }

    public SecurityType getType() {
        return SecurityType.TIGHT_AUTHENTICATION;
    }

    public boolean authenticate(Reader reader, Writer writer, CapabilityContainer authCaps, IPasswordRetriever passwordRetriever) throws TransportException, FatalException, UnsupportedSecurityTypeException {
        this.initTunnelling(reader, writer);
        this.initAuthorization(reader, writer, authCaps, passwordRetriever);
        return true;
    }

    private void initTunnelling(Reader reader, Writer writer) throws TransportException {
        long tunnelsCount = reader.readUInt32();
        if (tunnelsCount > 0L) {
            for (int i = 0; (long) i < tunnelsCount; ++i) {
                RfbCapabilityInfo rfbCapabilityInfo = new RfbCapabilityInfo(reader);
                Logger.getLogger("com.glavsoft.rfb.protocol.auth").fine(rfbCapabilityInfo.toString());
            }

            writer.writeInt32(0);
        }

    }

    private void initAuthorization(Reader reader, Writer writer, CapabilityContainer authCaps, IPasswordRetriever passwordRetriever) throws UnsupportedSecurityTypeException, TransportException, FatalException {
        int authCount = reader.readInt32();
        byte[] cap = new byte[authCount];

        for (int i = 0; i < authCount; ++i) {
            RfbCapabilityInfo rfbCapabilityInfo = new RfbCapabilityInfo(reader);
            cap[i] = (byte) rfbCapabilityInfo.getCode();
            Logger.getLogger("com.glavsoft.rfb.protocol.auth").fine(rfbCapabilityInfo.toString());
        }

        AuthHandler authHandler = null;
        if (authCount > 0) {
            authHandler = SecurityTypeState.selectAuthHandler(cap, authCaps);

            for (int i = 0; i < authCount; ++i) {
                if (authCaps.isSupported(cap[i])) {
                    writer.writeInt32(cap[i]);
                    break;
                }
            }
        } else {
            authHandler = SecurityType.getAuthHandlerById(SecurityType.NONE_AUTHENTICATION.getId());
        }

        Logger.getLogger("com.glavsoft.rfb.protocol.auth").info("Auth capability accepted: " + authHandler.getName());
        authHandler.authenticate(reader, writer, authCaps, passwordRetriever);
    }
}
