package com.glavsoft.viewer.swing;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

public class Utils {
    private static List icons;

    public Utils() {
    }

    private static List getApplicationIcons() {
        if (icons != null) {
            return icons;
        } else {
            icons = new LinkedList();
            URL resource = Utils.class.getResource("/com/glavsoft/viewer/images/tightvnc-logo-16x16.png");
            Image image = resource != null ? Toolkit.getDefaultToolkit().getImage(resource) : null;
            if (image != null) {
                icons.add(image);
            }

            resource = Utils.class.getResource("/com/glavsoft/viewer/images/tightvnc-logo-32x32.png");
            image = resource != null ? Toolkit.getDefaultToolkit().getImage(resource) : null;
            if (image != null) {
                icons.add(image);
            }

            return icons;
        }
    }

    public static ImageIcon getButtonIcon(String name) {
        URL resource = Utils.class.getResource("/com/glavsoft/viewer/images/button-" + name + ".png");
        return resource != null ? new ImageIcon(resource) : null;
    }

    public static void decorateDialog(JDialog dialog) {
        try {
            dialog.setAlwaysOnTop(true);
        } catch (SecurityException var2) {
        }

        dialog.pack();
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.toFront();
        setApplicationIconsForWindow(dialog);
    }

    public static void setApplicationIconsForWindow(Window window) {
        List icons = getApplicationIcons();
        if (icons.size() != 0) {
            window.setIconImages(icons);
        }

    }

    public static void centerWindow(Window window) {
        Point locationPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Rectangle bounds = window.getBounds();
        locationPoint.setLocation(locationPoint.x - bounds.width / 2, locationPoint.y - bounds.height / 2);
        window.setLocation(locationPoint);
    }
}
