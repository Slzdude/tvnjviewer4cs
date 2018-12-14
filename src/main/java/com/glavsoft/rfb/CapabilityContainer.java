package com.glavsoft.rfb;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.transport.Reader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

public class CapabilityContainer {
    private final Map caps = new HashMap();

    public CapabilityContainer() {
    }

    public void add(RfbCapabilityInfo capabilityInfo) {
        this.caps.put(capabilityInfo.getCode(), capabilityInfo);
    }

    public void add(int code, String vendor, String name) {
        this.caps.put(code, new RfbCapabilityInfo(code, vendor, name));
    }

    public void addEnabled(int code, String vendor, String name) {
        RfbCapabilityInfo capability = new RfbCapabilityInfo(code, vendor, name);
        capability.setEnable(true);
        this.caps.put(code, capability);
    }

    public void setEnable(int id, boolean enable) {
        RfbCapabilityInfo c = (RfbCapabilityInfo) this.caps.get(id);
        if (c != null) {
            c.setEnable(enable);
        }

    }

    public void setAllEnable(boolean enable) {
        Iterator i$ = this.caps.values().iterator();

        while (i$.hasNext()) {
            RfbCapabilityInfo c = (RfbCapabilityInfo) i$.next();
            c.setEnable(enable);
        }

    }

    public Collection getEnabledEncodingTypes() {
        Collection types = new LinkedList();
        Iterator i$ = this.caps.values().iterator();

        while (i$.hasNext()) {
            RfbCapabilityInfo c = (RfbCapabilityInfo) i$.next();
            if (c.isEnabled()) {
                types.add(EncodingType.byId(c.getCode()));
            }
        }

        return types;
    }

    public void read(Reader reader, int count) throws TransportException {
        while (count-- > 0) {
            RfbCapabilityInfo capInfoReceived = new RfbCapabilityInfo(reader);
            Logger.getLogger("com.glavsoft.rfb").fine(capInfoReceived.toString());
            RfbCapabilityInfo myCapInfo = (RfbCapabilityInfo) this.caps.get(capInfoReceived.getCode());
            if (myCapInfo != null) {
                myCapInfo.setEnable(true);
            }
        }

    }

    public boolean isSupported(int code) {
        RfbCapabilityInfo myCapInfo = (RfbCapabilityInfo) this.caps.get(code);
        return myCapInfo != null ? myCapInfo.isEnabled() : false;
    }

    public boolean isSupported(RfbCapabilityInfo rfbCapabilityInfo) {
        return this.isSupported(rfbCapabilityInfo.getCode());
    }
}
