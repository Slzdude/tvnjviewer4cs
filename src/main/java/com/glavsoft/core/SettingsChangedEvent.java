package com.glavsoft.core;

public class SettingsChangedEvent {
    private final Object source;

    public SettingsChangedEvent(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return this.source;
    }
}
