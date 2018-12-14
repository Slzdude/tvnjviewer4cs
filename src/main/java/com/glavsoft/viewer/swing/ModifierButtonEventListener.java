package com.glavsoft.viewer.swing;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JToggleButton;

public class ModifierButtonEventListener {
    Map buttons = new HashMap();

    public ModifierButtonEventListener() {
    }

    public void addButton(int keyCode, JToggleButton button) {
        this.buttons.put(keyCode, button);
    }

    public void fireEvent(KeyEvent e) {
        int code = e.getKeyCode();
        if (this.buttons.containsKey(code)) {
            ((JToggleButton) this.buttons.get(code)).setSelected(e.getID() == 401);
        }

    }
}
