/*
 * Decompiled with CFR 0.137.
 */
package com.glavsoft.viewer.swing;

import com.glavsoft.rfb.client.ClientToServerMessage;
import com.glavsoft.rfb.client.KeyEventMessage;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.utils.Keymap;
import com.glavsoft.viewer.swing.KeyboardConvertor;
import com.glavsoft.viewer.swing.ModifierButtonEventListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyEventListener
        implements KeyListener {
    private ModifierButtonEventListener modifierButtonListener;
    private boolean convertToAscii;
    private final ProtocolContext context;
    private KeyboardConvertor convertor;

    public KeyEventListener(ProtocolContext context) {
        this.context = context;
        this.convertToAscii = false;
    }

    private void processKeyEvent(KeyEvent e) {
        if (this.processModifierKeys(e)) {
            return;
        }
        if (this.processSpecialKeys(e)) {
            return;
        }
        if (this.processActionKey(e)) {
            return;
        }
        int keyChar = e.getKeyChar();
        int location = e.getKeyLocation();
        if (65535 == keyChar) {
            int n = keyChar = this.convertToAscii ? this.convertor.convert(keyChar, e) : 0;
        }
        if (keyChar < 32) {
            if (e.isControlDown()) {
                keyChar += 96;
            } else {
                switch (keyChar) {
                    case 8: {
                        keyChar = 65288;
                        break;
                    }
                    case 9: {
                        keyChar = 65289;
                        break;
                    }
                    case 27: {
                        keyChar = 65307;
                        break;
                    }
                    case 10: {
                        keyChar = 4 == location ? 65421 : 65293;
                    }
                }
            }
        } else {
            keyChar = 127 == keyChar ? 65535 : (this.convertToAscii ? this.convertor.convert(keyChar, e) : Keymap.unicode2keysym(keyChar));
        }
        this.sendKeyEvent(keyChar, e);
    }

    private boolean processSpecialKeys(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (65406 == keyCode) {
            this.sendKeyEvent(65507, e);
            this.sendKeyEvent(65513, e);
            return true;
        }
        switch (keyCode) {
            case 96: {
                keyCode = 65456;
                break;
            }
            case 97: {
                keyCode = 65457;
                break;
            }
            case 98: {
                keyCode = 65458;
                break;
            }
            case 99: {
                keyCode = 65459;
                break;
            }
            case 100: {
                keyCode = 65460;
                break;
            }
            case 101: {
                keyCode = 65461;
                break;
            }
            case 102: {
                keyCode = 65462;
                break;
            }
            case 103: {
                keyCode = 65463;
                break;
            }
            case 104: {
                keyCode = 65464;
                break;
            }
            case 105: {
                keyCode = 65465;
                break;
            }
            case 106: {
                keyCode = 65450;
                break;
            }
            case 107: {
                keyCode = 65451;
                break;
            }
            case 108: {
                keyCode = 65452;
                break;
            }
            case 109: {
                keyCode = 65453;
                break;
            }
            case 110: {
                keyCode = 65454;
                break;
            }
            case 111: {
                keyCode = 65455;
                break;
            }
            default: {
                return false;
            }
        }
        this.sendKeyEvent(keyCode, e);
        return true;
    }

    private boolean processActionKey(KeyEvent e) {
        int keyCode = e.getKeyCode();
        int location = e.getKeyLocation();
        if (e.isActionKey()) {
            switch (keyCode) {
                case 36: {
                    keyCode = 4 == location ? 65429 : 65360;
                    break;
                }
                case 37: {
                    keyCode = 4 == location ? 65430 : 65361;
                    break;
                }
                case 38: {
                    keyCode = 4 == location ? 65431 : 65362;
                    break;
                }
                case 39: {
                    keyCode = 4 == location ? 65432 : 65363;
                    break;
                }
                case 40: {
                    keyCode = 4 == location ? 65433 : 65364;
                    break;
                }
                case 33: {
                    keyCode = 4 == location ? 65434 : 65365;
                    break;
                }
                case 34: {
                    keyCode = 4 == location ? 65435 : 65366;
                    break;
                }
                case 35: {
                    keyCode = 4 == location ? 65436 : 65367;
                    break;
                }
                case 155: {
                    keyCode = 4 == location ? 65438 : 65379;
                    break;
                }
                case 112: {
                    keyCode = 65470;
                    break;
                }
                case 113: {
                    keyCode = 65471;
                    break;
                }
                case 114: {
                    keyCode = 65472;
                    break;
                }
                case 115: {
                    keyCode = 65473;
                    break;
                }
                case 116: {
                    keyCode = 65474;
                    break;
                }
                case 117: {
                    keyCode = 65475;
                    break;
                }
                case 118: {
                    keyCode = 65476;
                    break;
                }
                case 119: {
                    keyCode = 65477;
                    break;
                }
                case 120: {
                    keyCode = 65478;
                    break;
                }
                case 121: {
                    keyCode = 65479;
                    break;
                }
                case 122: {
                    keyCode = 65480;
                    break;
                }
                case 123: {
                    keyCode = 65481;
                    break;
                }
                case 226: {
                    keyCode = 65430;
                    break;
                }
                case 224: {
                    keyCode = 65431;
                    break;
                }
                case 227: {
                    keyCode = 65432;
                    break;
                }
                case 225: {
                    keyCode = 65433;
                    break;
                }
                default: {
                    return false;
                }
            }
            this.sendKeyEvent(keyCode, e);
            return true;
        }
        return false;
    }

    private boolean processModifierKeys(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case 17: {
                keyCode = 65507;
                break;
            }
            case 16: {
                keyCode = 65505;
                break;
            }
            case 18: {
                keyCode = 65513;
                break;
            }
            case 157: {
                keyCode = 65511;
                break;
            }
            case 524: {
                keyCode = 65515;
                break;
            }
            case 525: {
                keyCode = 65517;
                break;
            }
            default: {
                return false;
            }
        }
        if (this.modifierButtonListener != null) {
            this.modifierButtonListener.fireEvent(e);
        }
        this.sendKeyEvent(keyCode + (e.getKeyLocation() == 3 ? 1 : 0), e);
        return true;
    }

    private void sendKeyEvent(int keyChar, KeyEvent e) {
        this.context.sendMessage(new KeyEventMessage(keyChar, e.getID() == 401));
    }

    @Override
    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        this.processKeyEvent(e);
        e.consume();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        this.processKeyEvent(e);
        e.consume();
    }

    public void addModifierListener(ModifierButtonEventListener modifierButtonListener) {
        this.modifierButtonListener = modifierButtonListener;
    }

    public void setConvertToAscii(boolean convertToAscii) {
        this.convertToAscii = convertToAscii;
        if (convertToAscii && null == this.convertor) {
            this.convertor = new KeyboardConvertor();
        }
    }
}

