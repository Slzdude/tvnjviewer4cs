package com.glavsoft.rfb.protocol.state;

import com.glavsoft.exceptions.AuthenticationFailedException;
import com.glavsoft.exceptions.FatalException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedProtocolVersionException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;

import java.util.logging.Logger;

public abstract class ProtocolState {
    protected ProtocolContext context;
    protected Logger logger;
    protected Reader reader;
    protected Writer writer;

    public ProtocolState(ProtocolContext context) {
        this.context = context;
        this.logger = context.getLogger();
        this.reader = context.getReader();
        this.writer = context.getWriter();
    }

    protected void changeStateTo(ProtocolState state) {
        this.context.changeStateTo(state);
    }

    public abstract boolean next() throws UnsupportedProtocolVersionException, TransportException, UnsupportedSecurityTypeException, AuthenticationFailedException, FatalException;
}
