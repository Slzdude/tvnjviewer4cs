package com.glavsoft.transport;

import com.glavsoft.exceptions.ClosedConnectionException;
import com.glavsoft.exceptions.TransportException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Reader {
    static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    static final Charset UTF8 = Charset.forName("UTF-8");
    private final DataInputStream is;

    public Reader(InputStream is) {
        this.is = new DataInputStream(new BufferedInputStream(is));
    }

    public byte readByte() throws TransportException {
        try {
            byte readByte = this.is.readByte();
            return readByte;
        } catch (EOFException var2) {
            throw new ClosedConnectionException(var2);
        } catch (IOException var3) {
            throw new TransportException("Cannot read byte", var3);
        }
    }

    public int readUInt8() throws TransportException {
        return this.readByte() & 255;
    }

    public int readUInt16() throws TransportException {
        return this.readInt16() & '\uffff';
    }

    public short readInt16() throws TransportException {
        try {
            short readShort = this.is.readShort();
            return readShort;
        } catch (EOFException var2) {
            throw new ClosedConnectionException(var2);
        } catch (IOException var3) {
            throw new TransportException("Cannot read int16", var3);
        }
    }

    public long readUInt32() throws TransportException {
        return (long) this.readInt32() & 4294967295L;
    }

    public int readInt32() throws TransportException {
        try {
            int readInt = this.is.readInt();
            return readInt;
        } catch (EOFException var2) {
            throw new ClosedConnectionException(var2);
        } catch (IOException var3) {
            throw new TransportException("Cannot read int32", var3);
        }
    }

    public long readInt64() throws TransportException {
        try {
            return this.is.readLong();
        } catch (EOFException var2) {
            throw new ClosedConnectionException(var2);
        } catch (IOException var3) {
            throw new TransportException("Cannot read int32", var3);
        }
    }

    public String readString(int length) throws TransportException {
        return new String(this.readBytes(length));
    }

    public String readString() throws TransportException {
        int length = this.readInt32() & 2147483647;
        return this.readString(length);
    }

    public String readUtf8String() throws TransportException {
        int length = this.readInt32() & 2147483647;
        return new String(this.readBytes(length), UTF8);
    }

    public byte[] readBytes(int length) throws TransportException {
        byte[] b = new byte[length];
        return this.readBytes(b, 0, length);
    }

    public byte[] readBytes(byte[] b, int offset, int length) throws TransportException {
        try {
            this.is.readFully(b, offset, length);
            return b;
        } catch (EOFException var5) {
            throw new ClosedConnectionException(var5);
        } catch (IOException var6) {
            throw new TransportException("Cannot read " + length + " bytes array", var6);
        }
    }

    public void skip(int length) throws TransportException {
        try {
            this.is.skipBytes(length);
        } catch (EOFException var3) {
            throw new ClosedConnectionException(var3);
        } catch (IOException var4) {
            throw new TransportException("Cannot skip " + length + " bytes", var4);
        }
    }
}
