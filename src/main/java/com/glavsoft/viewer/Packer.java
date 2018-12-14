package com.glavsoft.viewer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packer {
    protected ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    protected DataOutputStream data;
    protected byte[] bdata = new byte[8];
    protected ByteBuffer buffer = null;

    public Packer() {
        this.data = new DataOutputStream(this.out);
        this.buffer = ByteBuffer.wrap(this.bdata);
    }

    public void little() {
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void big() {
        this.buffer.order(ByteOrder.BIG_ENDIAN);
    }

    public void addInteger(int x) {
        this.addInt(x);
    }

    public void addInt(int x) {
        this.buffer.putInt(0, x);
        this.write(this.bdata, 0, 4);
    }

    public void addIntWithMask(int x, int mask) {
        this.buffer.putInt(0, x);
        ByteOrder current = this.buffer.order();
        this.big();
        int temp = this.buffer.getInt(0);
        this.buffer.putInt(0, temp ^ mask);
        this.write(this.bdata, 0, 4);
        this.buffer.order(current);
    }

    public void addUnicodeString(String text, int max) {
        try {
            this.addShort(text.length());
            this.addShort(max);

            for (int x = 0; x < text.length(); ++x) {
                this.data.writeChar(text.charAt(x));
            }
        } catch (IOException var4) {
        }

    }

    public void addByte(int b) {
        try {
            this.data.write((byte) b);
        } catch (IOException var3) {
        }

    }

    public void addHex(String dataz) {
        try {
            char[] tempchars = dataz.toCharArray();
            StringBuffer number = new StringBuffer("FF");

            for (int y = 0; y < tempchars.length; y += 2) {
                number.setCharAt(0, tempchars[y]);
                number.setCharAt(1, tempchars[y + 1]);
                this.data.writeByte(Integer.parseInt(number.toString(), 16));
            }
        } catch (IOException var5) {
        }

    }

    protected void write(byte[] src, int start, int len) {
        try {
            this.data.write(src, start, len);
        } catch (IOException var5) {
        }

    }

    public void addShort(int x) {
        this.buffer.putShort(0, (short) x);
        this.write(this.bdata, 0, 2);
    }

    public byte[] getBytes() {
        byte[] result = this.out.toByteArray();

        try {
            this.data.close();
        } catch (IOException var3) {
        }

        return result;
    }

    public long size() {
        return (long) this.out.size();
    }
}
