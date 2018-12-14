package com.glavsoft.rfb.client;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.transport.Writer;

import java.util.Iterator;
import java.util.Set;

public class SetEncodingsMessage implements ClientToServerMessage {
    private final Set encodings;

    public SetEncodingsMessage(Set set) {
        this.encodings = set;
    }

    public void send(Writer writer) throws TransportException {
        writer.writeByte(2);
        writer.writeByte(0);
        writer.writeInt16(this.encodings.size());
        Iterator i$ = this.encodings.iterator();

        while (i$.hasNext()) {
            EncodingType enc = (EncodingType) i$.next();
            writer.writeInt32(enc.getId());
        }

        writer.flush();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("SetEncodingsMessage: [encodings: ");
        Iterator i$ = this.encodings.iterator();

        while (i$.hasNext()) {
            EncodingType enc = (EncodingType) i$.next();
            sb.append(enc.name()).append(',');
        }

        sb.setLength(sb.length() - 1);
        return sb.append(']').toString();
    }
}
