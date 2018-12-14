package com.glavsoft.viewer.swing;

import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.rfb.IChangeSettingsListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class UiSettings {
    public static final int MIN_SCALE_PERCENT = 10;
    public static final int MAX_SCALE_PERCENT = 500;
    private static final int SCALE_PERCENT_ZOOMING_STEP = 10;
    public static final int CHANGED_SCALE_FACTOR = 1;
    public static final int CHANGED_SYSTEM_CURSOR = 2;
    private int changedSettingsMask = 0;
    private double scalePercent = 100.0D;
    private final List listeners = new LinkedList();

    public UiSettings() {
        this.scalePercent = 100.0D;
        this.changedSettingsMask = 0;
    }

    private UiSettings(UiSettings uiSettings) {
        this.scalePercent = uiSettings.scalePercent;
        this.changedSettingsMask = uiSettings.changedSettingsMask;
    }

    public double getScaleFactor() {
        return this.scalePercent / 100.0D;
    }

    public void setScalePercent(double scalePercent) {
        this.scalePercent = scalePercent;
        this.changedSettingsMask |= 1;
    }

    public void addListener(IChangeSettingsListener listener) {
        this.listeners.add(listener);
    }

    public void fireListeners() {
        SettingsChangedEvent event = new SettingsChangedEvent(new UiSettings(this));
        this.changedSettingsMask = 0;
        Iterator i$ = this.listeners.iterator();

        while (i$.hasNext()) {
            IChangeSettingsListener listener = (IChangeSettingsListener) i$.next();
            listener.settingsChanged(event);
        }

    }

    public void zoomOut() {
        double oldScaleFactor = this.scalePercent;
        double scaleFactor = (double) ((int) (this.scalePercent / 10.0D) * 10);
        if (scaleFactor == oldScaleFactor) {
            scaleFactor -= 10.0D;
        }

        if (scaleFactor < 10.0D) {
            scaleFactor = 10.0D;
        }

        this.setScalePercent(scaleFactor);
        this.fireListeners();
    }

    public void zoomIn() {
        double scaleFactor = (double) ((int) (this.scalePercent / 10.0D) * 10 + 10);
        if (scaleFactor > 500.0D) {
            scaleFactor = 500.0D;
        }

        this.setScalePercent(scaleFactor);
        this.fireListeners();
    }

    public void zoomAsIs() {
        this.setScalePercent(100.0D);
        this.fireListeners();
    }

    public void zoomToFit(int containerWidth, int containerHeight, int fbWidth, int fbHeight) {
        int scalePromille;
        for (scalePromille = Math.min(1000 * containerWidth / fbWidth, 1000 * containerHeight / fbHeight); (double) (fbWidth * scalePromille) / 1000.0D > (double) containerWidth || (double) (fbHeight * scalePromille) / 1000.0D > (double) containerHeight; --scalePromille) {
        }

        this.setScalePercent((double) scalePromille / 10.0D);
        this.fireListeners();
    }

    public boolean isChangedScaleFactor() {
        return (this.changedSettingsMask & 1) == 1;
    }

    public boolean isChangedSystemCursor() {
        return (this.changedSettingsMask & 2) == 2;
    }

    public static boolean isUiSettingsChangedFired(SettingsChangedEvent event) {
        return event.getSource() instanceof UiSettings;
    }

    public double getScalePercent() {
        return this.scalePercent;
    }

    public String getScalePercentFormatted() {
        NumberFormat numberFormat = new DecimalFormat("###.#");
        return numberFormat.format(this.scalePercent);
    }
}
