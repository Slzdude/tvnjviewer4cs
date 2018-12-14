package com.glavsoft.rfb.encoding.decoder;

public class ByteBuffer {
    private static ByteBuffer instance = new ByteBuffer();
    private byte[] buffer = new byte[0];

    private ByteBuffer() {
    }

    public static ByteBuffer getInstance() {
        return instance;
    }

    public void correctBufferCapacity(int length) {
        assert this.buffer != null;

        if (this.buffer.length < length) {
            this.buffer = new byte[length];
        }

    }

    public byte[] getBuffer(int length) {
        this.correctBufferCapacity(length);
        return this.buffer;
    }
}
