package com.glavsoft.rfb.protocol.auth;

import com.glavsoft.exceptions.FatalException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.CapabilityContainer;
import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;

public abstract class AuthHandler {
    protected boolean useSecurityResult = true;

    public AuthHandler() {
    }

    public abstract boolean authenticate(Reader var1, Writer var2, CapabilityContainer var3, IPasswordRetriever var4) throws TransportException, FatalException, UnsupportedSecurityTypeException;

    public abstract SecurityType getType();

    public int getId() {
        return this.getType().getId();
    }

    public String getName() {
        return this.getType().name();
    }

    public boolean useSecurityResult() {
        return this.useSecurityResult;
    }

    public void setUseSecurityResult(boolean enabled) {
        this.useSecurityResult = enabled;
    }
}
