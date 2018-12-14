package com.glavsoft.rfb.protocol.auth;

import com.glavsoft.rfb.CapabilityContainer;
import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;

public class NoneAuthentication extends AuthHandler {
    public NoneAuthentication() {
    }

    public boolean authenticate(Reader reader, Writer writer, CapabilityContainer authCaps, IPasswordRetriever passwordRetriever) {
        return false;
    }

    public SecurityType getType() {
        return SecurityType.NONE_AUTHENTICATION;
    }
}
