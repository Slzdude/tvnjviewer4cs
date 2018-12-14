package com.glavsoft.rfb.encoding.decoder;

import com.glavsoft.rfb.encoding.EncodingType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class DecodersContainer {
    private static Map knownDecoders = new HashMap();
    private final Map decoders = new HashMap();

    public DecodersContainer() {
        this.decoders.put(EncodingType.RAW_ENCODING, RawDecoder.getInstance());
    }

    public void instantiateDecodersWhenNeeded(Collection encodings) {
        Iterator i$ = encodings.iterator();

        while (i$.hasNext()) {
            EncodingType enc = (EncodingType) i$.next();
            if (EncodingType.ordinaryEncodings.contains(enc) && !this.decoders.containsKey(enc)) {
                try {
                    this.decoders.put(enc, ((Class) knownDecoders.get(enc)).newInstance());
                } catch (InstantiationException var5) {
                    this.logError(enc, var5);
                } catch (IllegalAccessException var6) {
                    this.logError(enc, var6);
                }
            }
        }

    }

    private void logError(EncodingType enc, Exception e) {
        Logger.getLogger(this.getClass().getName()).severe("Can not instantiate decoder for encoding type '" + enc.getName() + "' " + e.getMessage());
    }

    public Decoder getDecoderByType(EncodingType type) {
        return (Decoder) this.decoders.get(type);
    }

    public void resetDecoders() {
        Iterator i$ = this.decoders.values().iterator();

        while (i$.hasNext()) {
            Decoder decoder = (Decoder) i$.next();
            if (decoder != null) {
                decoder.reset();
            }
        }

    }

    static {
        knownDecoders.put(EncodingType.TIGHT, TightDecoder.class);
        knownDecoders.put(EncodingType.HEXTILE, HextileDecoder.class);
        knownDecoders.put(EncodingType.ZRLE, ZRLEDecoder.class);
        knownDecoders.put(EncodingType.ZLIB, ZlibDecoder.class);
        knownDecoders.put(EncodingType.RRE, RREDecoder.class);
        knownDecoders.put(EncodingType.COPY_RECT, CopyRectDecoder.class);
    }
}
