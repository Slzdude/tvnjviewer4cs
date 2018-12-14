package com.glavsoft.rfb.protocol.state;

import com.glavsoft.exceptions.AuthenticationFailedException;
import com.glavsoft.exceptions.ClosedConnectionException;
import com.glavsoft.exceptions.FatalException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedProtocolVersionException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.auth.AuthHandler;

public class AuthenticationState extends ProtocolState {
    private static final int AUTH_RESULT_OK = 0;
    private final AuthHandler authHandler;

    public AuthenticationState(ProtocolContext context, AuthHandler authHandler) {
        super(context);
        this.authHandler = authHandler;
    }

    public boolean next() throws UnsupportedProtocolVersionException, TransportException, UnsupportedSecurityTypeException, AuthenticationFailedException, FatalException {
        this.authenticate();
        return true;
    }

    private void authenticate() throws TransportException, AuthenticationFailedException, FatalException, UnsupportedSecurityTypeException {
        boolean isTight = this.authHandler.authenticate(this.reader, this.writer, this.context.getSettings().authCapabilities, this.context.getPasswordRetriever());
        if (this.authHandler.useSecurityResult()) {
            this.checkSecurityResult();
        }

        this.changeStateTo((ProtocolState) (isTight ? new InitTightState(this.context) : new InitState(this.context)));
        this.context.setTight(isTight);
    }

    protected void checkSecurityResult() throws TransportException, AuthenticationFailedException {
        if (this.reader.readInt32() != 0) {
            try {
                String reason = this.reader.readString();
                throw new AuthenticationFailedException(reason);
            } catch (ClosedConnectionException var2) {
                throw new AuthenticationFailedException("Authentication failed");
            }
        }
    }
}
