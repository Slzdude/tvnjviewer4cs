package com.glavsoft.viewer.swing.gui;

import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.protocol.LocalPointer;
import com.glavsoft.rfb.protocol.ProtocolSettings;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;

public class OptionsDialog extends JDialog {
    private JSlider jpegQuality;
    private JSlider compressionLevel;
    private JCheckBox viewOnlyCheckBox;
    private ProtocolSettings settings;
    private JCheckBox sharedSession;
    private OptionsDialog.RadioButtonSelectedState mouseCursorTrackSelected;
    private Map mouseCursorTrackMap;
    private JCheckBox useCompressionLevel;
    private JCheckBox useJpegQuality;
    private JLabel jpegQualityPoorLabel;
    private JLabel jpegQualityBestLabel;
    private JLabel compressionLevelFastLabel;
    private JLabel compressionLevelBestLabel;
    private JCheckBox allowCopyRect;
    private JComboBox encodings;
    private JCheckBox disableClipboardTransfer;
    private JComboBox colorDepth;

    public OptionsDialog(Window owner) {
        super(owner, "Connection Options", ModalityType.DOCUMENT_MODAL);
        WindowAdapter onClose = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                OptionsDialog.this.setVisible(false);
            }
        };
        this.addWindowListener(onClose);
        JPanel optionsPane = new JPanel(new GridLayout(0, 2));
        this.add(optionsPane, "Center");
        optionsPane.add(this.createLeftPane());
        optionsPane.add(this.createRightPane());
        this.addButtons(onClose);
        this.pack();
    }

    public void initControlsFromSettings(ProtocolSettings settings, boolean isOnConnect) {
        this.settings = settings;
        this.viewOnlyCheckBox.setSelected(settings.isViewOnly());
        int i = 0;

        boolean isNotSetEncoding;
        for (isNotSetEncoding = true; this.encodings.getItemAt(i) != null; ++i) {
            EncodingType item = ((OptionsDialog.EncodingSelectItem) this.encodings.getItemAt(i)).type;
            if (item.equals(settings.getPreferredEncoding())) {
                this.encodings.setSelectedIndex(i);
                isNotSetEncoding = false;
                break;
            }
        }

        if (isNotSetEncoding) {
            this.encodings.setSelectedItem(0);
        }

        this.sharedSession.setSelected(settings.isShared());
        this.sharedSession.setEnabled(isOnConnect);
        ((JRadioButton) this.mouseCursorTrackMap.get(settings.getMouseCursorTrack())).setSelected(true);
        this.mouseCursorTrackSelected.setSelected(settings.getMouseCursorTrack());
        int bpp = settings.getBitsPerPixel();
        i = 0;

        boolean isNotSet;
        for (isNotSet = true; this.colorDepth.getItemAt(i) != null; ++i) {
            int itemBpp = ((OptionsDialog.ColorDepthSelectItem) this.colorDepth.getItemAt(i)).bpp;
            if (itemBpp == bpp) {
                this.colorDepth.setSelectedIndex(i);
                isNotSet = false;
                break;
            }
        }

        if (isNotSet) {
            this.colorDepth.setSelectedItem(0);
        }

        this.useCompressionLevel.setSelected(settings.getCompressionLevel() > 0);
        this.compressionLevel.setValue(Math.abs(settings.getCompressionLevel()));
        this.setCompressionLevelPaneEnable();
        this.useJpegQuality.setSelected(settings.getJpegQuality() > 0);
        this.jpegQuality.setValue(Math.abs(settings.getJpegQuality()));
        this.setJpegQualityPaneEnable();
        this.allowCopyRect.setSelected(settings.isAllowCopyRect());
        this.disableClipboardTransfer.setSelected(!settings.isAllowClipboardTransfer());
    }

    private void setSettingsFromControls() {
        this.settings.setViewOnly(this.viewOnlyCheckBox.isSelected());
        this.settings.setPreferredEncoding(((OptionsDialog.EncodingSelectItem) this.encodings.getSelectedItem()).type);
        this.settings.setSharedFlag(this.sharedSession.isSelected());
        this.settings.setMouseCursorTrack((LocalPointer) this.mouseCursorTrackSelected.getSelected());
        this.settings.setBitsPerPixel(((OptionsDialog.ColorDepthSelectItem) this.colorDepth.getSelectedItem()).bpp);
        this.settings.setCompressionLevel(this.useCompressionLevel.isSelected() ? this.compressionLevel.getValue() : -Math.abs(this.settings.getCompressionLevel()));
        this.settings.setJpegQuality(this.useJpegQuality.isSelected() ? this.jpegQuality.getValue() : -Math.abs(this.settings.getJpegQuality()));
        this.settings.setAllowCopyRect(this.allowCopyRect.isSelected());
        this.settings.setAllowClipboardTransfer(!this.disableClipboardTransfer.isSelected());
        this.settings.fireListeners();
    }

    private Component createLeftPane() {
        Box box = Box.createVerticalBox();
        box.setAlignmentX(0.0F);
        box.add(this.createEncodingsPanel());
        box.add(Box.createVerticalGlue());
        return box;
    }

    private Component createRightPane() {
        Box box = Box.createVerticalBox();
        box.setAlignmentX(0.0F);
        box.add(this.createRestrictionsPanel());
        box.add(this.createMouseCursorPanel());
        this.sharedSession = new JCheckBox("Request shared session");
        box.add((new JPanel(new FlowLayout(0))).add(this.sharedSession));
        box.add(Box.createVerticalGlue());
        return box;
    }

    private JPanel createRestrictionsPanel() {
        JPanel restrictionsPanel = new JPanel(new FlowLayout(0));
        restrictionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Restrictions"));
        Box restrictionsBox = Box.createVerticalBox();
        restrictionsBox.setAlignmentX(0.0F);
        restrictionsPanel.add(restrictionsBox);
        this.viewOnlyCheckBox = new JCheckBox("View only (inputs ignored)");
        this.viewOnlyCheckBox.setAlignmentX(0.0F);
        restrictionsBox.add(this.viewOnlyCheckBox);
        this.disableClipboardTransfer = new JCheckBox("Disable clipboard transfer");
        this.disableClipboardTransfer.setAlignmentX(0.0F);
        restrictionsBox.add(this.disableClipboardTransfer);
        return restrictionsPanel;
    }

    private JPanel createEncodingsPanel() {
        JPanel encodingsPanel = new JPanel();
        encodingsPanel.setAlignmentX(0.0F);
        encodingsPanel.setLayout(new BoxLayout(encodingsPanel, 1));
        encodingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Format and Encodings"));
        JPanel encPane = new JPanel(new FlowLayout(0));
        encPane.setAlignmentX(0.0F);
        encPane.add(new JLabel("Preferred encoding: "));
        this.encodings = new JComboBox();
        this.encodings.addItem(new OptionsDialog.EncodingSelectItem(EncodingType.TIGHT));
        this.encodings.addItem(new OptionsDialog.EncodingSelectItem(EncodingType.HEXTILE));
        this.encodings.addItem(new OptionsDialog.EncodingSelectItem(EncodingType.ZRLE));
        this.encodings.addItem(new OptionsDialog.EncodingSelectItem(EncodingType.RAW_ENCODING));
        encPane.add(this.encodings);
        encodingsPanel.add(encPane);
        encodingsPanel.add(this.createColorDepthPanel());
        this.addCompressionLevelPane(encodingsPanel);
        this.addJpegQualityLevelPane(encodingsPanel);
        this.allowCopyRect = new JCheckBox("Allow CopyRect encoding");
        this.allowCopyRect.setAlignmentX(0.0F);
        encodingsPanel.add(this.allowCopyRect);
        return encodingsPanel;
    }

    private JPanel createColorDepthPanel() {
        JPanel colorDepthPanel = new JPanel(new FlowLayout(0));
        colorDepthPanel.setAlignmentX(0.0F);
        colorDepthPanel.add(new JLabel("Color format: "));
        this.colorDepth = new JComboBox();
        this.colorDepth.addItem(new OptionsDialog.ColorDepthSelectItem(0, "Server's default"));
        this.colorDepth.addItem(new OptionsDialog.ColorDepthSelectItem(32, "16 777 216 colors"));
        this.colorDepth.addItem(new OptionsDialog.ColorDepthSelectItem(16, "65 536 colors"));
        this.colorDepth.addItem(new OptionsDialog.ColorDepthSelectItem(8, "256 colors"));
        this.colorDepth.addItem(new OptionsDialog.ColorDepthSelectItem(6, "64 colors"));
        this.colorDepth.addItem(new OptionsDialog.ColorDepthSelectItem(3, "8 colors"));
        colorDepthPanel.add(this.colorDepth);
        this.colorDepth.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                OptionsDialog.ColorDepthSelectItem selectedItem = (OptionsDialog.ColorDepthSelectItem) OptionsDialog.this.colorDepth.getSelectedItem();
                OptionsDialog.this.setEnabled(selectedItem.bpp > 8 || selectedItem.bpp == 0, OptionsDialog.this.useJpegQuality);
                OptionsDialog.this.setEnabled(OptionsDialog.this.useJpegQuality.isSelected() && (selectedItem.bpp > 8 || selectedItem.bpp == 0), OptionsDialog.this.jpegQuality, OptionsDialog.this.jpegQualityPoorLabel, OptionsDialog.this.jpegQualityBestLabel);
            }
        });
        return colorDepthPanel;
    }

    private void addJpegQualityLevelPane(JPanel encodingsPanel) {
        this.useJpegQuality = new JCheckBox("Allow JPEG, set quality level:");
        this.useJpegQuality.setAlignmentX(0.0F);
        encodingsPanel.add(this.useJpegQuality);
        JPanel jpegQualityPane = new JPanel();
        jpegQualityPane.setAlignmentX(0.0F);
        this.jpegQualityPoorLabel = new JLabel("poor");
        jpegQualityPane.add(this.jpegQualityPoorLabel);
        this.jpegQuality = new JSlider(1, 9, 9);
        jpegQualityPane.add(this.jpegQuality);
        this.jpegQuality.setPaintTicks(true);
        this.jpegQuality.setMinorTickSpacing(1);
        this.jpegQuality.setMajorTickSpacing(1);
        this.jpegQuality.setPaintLabels(true);
        this.jpegQuality.setSnapToTicks(true);
        this.jpegQuality.setFont(this.jpegQuality.getFont().deriveFont(8.0F));
        this.jpegQualityBestLabel = new JLabel("best");
        jpegQualityPane.add(this.jpegQualityBestLabel);
        encodingsPanel.add(jpegQualityPane);
        this.jpegQualityPoorLabel.setFont(this.jpegQualityPoorLabel.getFont().deriveFont(10.0F));
        this.jpegQualityBestLabel.setFont(this.jpegQualityBestLabel.getFont().deriveFont(10.0F));
        this.useJpegQuality.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OptionsDialog.this.setJpegQualityPaneEnable();
            }
        });
    }

    protected void setJpegQualityPaneEnable() {
        if (this.useJpegQuality != null && this.colorDepth != null) {
            int bpp = ((OptionsDialog.ColorDepthSelectItem) this.colorDepth.getSelectedItem()).bpp;
            this.setEnabled(bpp > 8 || bpp == 0, this.useJpegQuality);
            this.setEnabled(this.useJpegQuality.isSelected() && (bpp > 8 || bpp == 0), this.jpegQuality, this.jpegQualityPoorLabel, this.jpegQualityBestLabel);
        }

    }

    private void addCompressionLevelPane(JPanel encodingsPanel) {
        this.useCompressionLevel = new JCheckBox("Custom compression level:");
        this.useCompressionLevel.setAlignmentX(0.0F);
        encodingsPanel.add(this.useCompressionLevel);
        JPanel compressionLevelPane = new JPanel();
        compressionLevelPane.setAlignmentX(0.0F);
        this.compressionLevelFastLabel = new JLabel("fast");
        compressionLevelPane.add(this.compressionLevelFastLabel);
        this.compressionLevel = new JSlider(1, 9, 1);
        compressionLevelPane.add(this.compressionLevel);
        this.compressionLevel.setPaintTicks(true);
        this.compressionLevel.setMinorTickSpacing(1);
        this.compressionLevel.setMajorTickSpacing(1);
        this.compressionLevel.setPaintLabels(true);
        this.compressionLevel.setSnapToTicks(true);
        this.compressionLevel.setFont(this.compressionLevel.getFont().deriveFont(8.0F));
        this.compressionLevelBestLabel = new JLabel("best");
        compressionLevelPane.add(this.compressionLevelBestLabel);
        encodingsPanel.add(compressionLevelPane);
        this.compressionLevelFastLabel.setFont(this.compressionLevelFastLabel.getFont().deriveFont(10.0F));
        this.compressionLevelBestLabel.setFont(this.compressionLevelBestLabel.getFont().deriveFont(10.0F));
        this.useCompressionLevel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OptionsDialog.this.setEnabled(OptionsDialog.this.useCompressionLevel.isSelected(), OptionsDialog.this.compressionLevel, OptionsDialog.this.compressionLevelFastLabel, OptionsDialog.this.compressionLevelBestLabel);
            }
        });
        this.setCompressionLevelPaneEnable();
    }

    protected void setCompressionLevelPaneEnable() {
        this.setEnabled(this.useCompressionLevel.isSelected(), this.compressionLevel, this.compressionLevelFastLabel, this.compressionLevelBestLabel);
    }

    private void setEnabled(boolean isEnabled, JComponent... comp) {
        JComponent[] arr$ = comp;
        int len$ = comp.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            JComponent c = arr$[i$];
            c.setEnabled(isEnabled);
        }

    }

    private JPanel createLocalShapePanel() {
        JPanel localCursorShapePanel = new JPanel(new FlowLayout(0, 0, 0));
        localCursorShapePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Local cursor shape"));
        Box localCursorShapeBox = Box.createVerticalBox();
        localCursorShapePanel.add(localCursorShapeBox);
        JRadioButton dotCursorRadio = new JRadioButton("Dot cursor");
        JRadioButton smallDotCursorRadio = new JRadioButton("Small dot cursor");
        JRadioButton arrowCursorRadio = new JRadioButton("Default cursor");
        JRadioButton noCursorRadio = new JRadioButton("No local cursor");
        localCursorShapeBox.add(dotCursorRadio);
        localCursorShapeBox.add(smallDotCursorRadio);
        localCursorShapeBox.add(arrowCursorRadio);
        localCursorShapeBox.add(noCursorRadio);
        ButtonGroup localCursorButtonGroup = new ButtonGroup();
        localCursorButtonGroup.add(dotCursorRadio);
        localCursorButtonGroup.add(smallDotCursorRadio);
        localCursorButtonGroup.add(arrowCursorRadio);
        localCursorButtonGroup.add(noCursorRadio);
        return localCursorShapePanel;
    }

    private JPanel createMouseCursorPanel() {
        JPanel mouseCursorPanel = new JPanel(new FlowLayout(0, 0, 0));
        mouseCursorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mouse Cursor"));
        Box mouseCursorBox = Box.createVerticalBox();
        mouseCursorPanel.add(mouseCursorBox);
        ButtonGroup mouseCursorTrackGroup = new ButtonGroup();
        this.mouseCursorTrackSelected = new OptionsDialog.RadioButtonSelectedState();
        this.mouseCursorTrackMap = new HashMap();
        this.addRadioButton("Track remote cursor locally", LocalPointer.ON, this.mouseCursorTrackSelected, this.mouseCursorTrackMap, mouseCursorBox, mouseCursorTrackGroup);
        this.addRadioButton("Let remote server deal with mouse cursor", LocalPointer.OFF, this.mouseCursorTrackSelected, this.mouseCursorTrackMap, mouseCursorBox, mouseCursorTrackGroup);
        this.addRadioButton("Don't show remote cursor", LocalPointer.HIDE, this.mouseCursorTrackSelected, this.mouseCursorTrackMap, mouseCursorBox, mouseCursorTrackGroup);
        return mouseCursorPanel;
    }

    private JRadioButton addRadioButton(String text, final Object state, final OptionsDialog.RadioButtonSelectedState selected, Map state2buttonMap, JComponent component, ButtonGroup group) {
        JRadioButton radio = new JRadioButton(text);
        radio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selected.setSelected(state);
            }
        });
        component.add(radio);
        group.add(radio);
        state2buttonMap.put(state, radio);
        return radio;
    }

    private void addButtons(final WindowListener onClose) {
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Ok");
        buttonPanel.add(loginButton);
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OptionsDialog.this.setSettingsFromControls();
                OptionsDialog.this.setVisible(false);
            }
        });
        JButton closeButton = new JButton("Cancel");
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose.windowClosing((WindowEvent) null);
            }
        });
        this.add(buttonPanel, "South");
    }

    private static class RadioButtonSelectedState {
        private Object state;

        private RadioButtonSelectedState() {
        }

        public void setSelected(Object state) {
            this.state = state;
        }

        public Object getSelected() {
            return this.state;
        }
    }

    private static class ColorDepthSelectItem {
        final int bpp;
        final String title;

        public ColorDepthSelectItem(int bpp, String title) {
            this.bpp = bpp;
            this.title = title;
        }

        public String toString() {
            return this.title;
        }
    }

    private static class EncodingSelectItem {
        final EncodingType type;

        public EncodingSelectItem(EncodingType type) {
            this.type = type;
        }

        public String toString() {
            return this.type.getName();
        }
    }
}
