package com.glavsoft.transport;

import com.glavsoft.exceptions.TransportException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Writer {
    private final DataOutputStream os;

    public Writer(OutputStream os) {
        this.os = new DataOutputStream(os);
    }

    public void flush() throws TransportException {
        try {
            this.os.flush();
        } catch (IOException var2) {
            throw new TransportException("Cannot flush output stream", var2);
        }
    }

    public void writeByte(int b) throws TransportException {
        this.write((byte) (b & 255));
    }

    public void write(byte b) throws TransportException {
        try {
            this.os.writeByte(b);
        } catch (IOException var3) {
            throw new TransportException("Cannot write byte", var3);
        }
    }

    public void writeInt16(int sh) throws TransportException {
        this.write((short) (sh & '\uffff'));
    }

    public void write(short sh) throws TransportException {
        try {
            this.os.writeShort(sh);
        } catch (IOException var3) {
            throw new TransportException("Cannot write short", var3);
        }
    }

    public void writeInt32(int i) throws TransportException {
        this.write(i);
    }

    public void writeInt64(long i) throws TransportException {
        try {
            this.os.writeLong(i);
        } catch (IOException var4) {
            throw new TransportException("Cannot write long", var4);
        }
    }

    public void write(int i) throws TransportException {
        try {
            this.os.writeInt(i);
        } catch (IOException var3) {
            throw new TransportException("Cannot write int", var3);
        }
    }

    public void write(byte[] b) throws TransportException {
        this.write(b, 0, b.length);
    }

    public void write(byte[] b, int length) throws TransportException {
        this.write(b, 0, length);
    }

    public void write(byte[] b, int offset, int length) throws TransportException {
        try {
            this.os.write(b, offset, length <= b.length ? length : b.length);
        } catch (IOException var5) {
            throw new TransportException("Cannot write " + length + " bytes", var5);
        }
    }
}
