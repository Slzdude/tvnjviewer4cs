/*
 * Decompiled with CFR 0.137.
 */
package com.glavsoft.rfb.protocol.state;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedProtocolVersionException;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.state.ProtocolState;
import com.glavsoft.rfb.protocol.state.SecurityType33State;
import com.glavsoft.rfb.protocol.state.SecurityType37State;
import com.glavsoft.rfb.protocol.state.SecurityTypeState;
import com.glavsoft.transport.Reader;
import com.glavsoft.transport.Writer;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandshakeState extends ProtocolState {
    public static final String PROTOCOL_VERSION_3_8 = "3.8";
    public static final String PROTOCOL_VERSION_3_7 = "3.7";
    public static final String PROTOCOL_VERSION_3_3 = "3.3";
    private static final int PROTOCOL_STRING_LENGTH = 12;
    private static final String PROTOCOL_STRING_REGEXP = "^RFB (\\d\\d\\d).(\\d\\d\\d)\n$";
    private static final int MIN_SUPPORTED_VERSION_MAJOR = 3;
    private static final int MIN_SUPPORTED_VERSION_MINOR = 3;
    private static final int MAX_SUPPORTED_VERSION_MAJOR = 3;
    private static final int MAX_SUPPORTED_VERSION_MINOR = 8;

    public HandshakeState(ProtocolContext context) {
        super(context);
    }

    @Override
    public boolean next() throws UnsupportedProtocolVersionException, TransportException {
        this.handshake();
        return true;
    }

    private void handshake() throws TransportException, UnsupportedProtocolVersionException {
        this.logger.info("Waiting to receive protocol string");
        String protocolString = this.reader.readString(12);
        this.logger.info("Server sent protocol string: " + protocolString.substring(0, protocolString.length() - 1));
        Pattern pattern = Pattern.compile(PROTOCOL_STRING_REGEXP);
        Matcher matcher = pattern.matcher(protocolString);
        if (!matcher.matches()) {
            throw new UnsupportedProtocolVersionException("Unsupported protocol version: " + protocolString);
        }
        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        if (major < 3 || 3 == major && minor < 3) {
            throw new UnsupportedProtocolVersionException("Unsupported protocol version: " + major + "." + minor);
        }
        if (major > 3) {
            major = 3;
            minor = 8;
        }
        if (minor >= 3 && minor < 7) {
            this.changeStateTo(new SecurityType33State(this.context));
            this.context.setProtocolVersion(PROTOCOL_VERSION_3_3);
            minor = 3;
        } else if (7 == minor) {
            this.changeStateTo(new SecurityType37State(this.context));
            this.context.setProtocolVersion(PROTOCOL_VERSION_3_7);
            minor = 7;
        } else if (minor >= 8) {
            this.changeStateTo(new SecurityTypeState(this.context));
            this.context.setProtocolVersion(PROTOCOL_VERSION_3_8);
            minor = 8;
        } else {
            throw new UnsupportedProtocolVersionException("Unsupported protocol version: " + protocolString);
        }
        this.writer.write(("RFB 00" + major + ".00" + minor + "\n").getBytes());
        this.logger.info("Set protocol version to: " + this.context.getProtocolVersion());
    }
}

