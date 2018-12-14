package com.glavsoft.viewer;

import com.glavsoft.viewer.swing.Surface;
import com.glavsoft.viewer.swing.Utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class ContainerManager {
    public static final int FS_SCROLLING_ACTIVE_BORDER = 20;
    private final Viewer viewer;
    private JToggleButton zoomFitButton;
    private JToggleButton zoomFullScreenButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton zoomAsIsButton;
    private JPanel outerPanel;
    private JScrollPane scroller;
    private Container container;
    private boolean forceResizable = true;
    private ContainerManager.ButtonsBar buttonsBar;
    private Surface surface;
    private boolean isSeparateFrame;
    private Rectangle oldContainerBounds;
    private volatile boolean isFullScreen;
    private Border oldScrollerBorder;
    private JLayeredPane lpane;
    private ContainerManager.EmptyButtonsBarMouseAdapter buttonsBarMouseAdapter;

    public ContainerManager(Viewer viewer) {
        this.viewer = viewer;
    }

    public Container createContainer(final Surface surface, boolean isSeparateFrame, boolean isApplet) {
        this.surface = surface;
        this.isSeparateFrame = isSeparateFrame;
        this.outerPanel = new JPanel(new FlowLayout(1, 0, 0)) {
            public Dimension getSize() {
                return surface.getPreferredSize();
            }

            public Dimension getPreferredSize() {
                return surface.getPreferredSize();
            }
        };
        this.lpane = new JLayeredPane() {
            public Dimension getSize() {
                return surface.getPreferredSize();
            }

            public Dimension getPreferredSize() {
                return surface.getPreferredSize();
            }
        };
        this.lpane.setPreferredSize(surface.getPreferredSize());
        this.lpane.add(surface, JLayeredPane.DEFAULT_LAYER, 0);
        this.outerPanel.add(this.lpane);
        this.scroller = new JScrollPane(this.outerPanel);
        if (isSeparateFrame) {
            JFrame frame = new JFrame();
            if (!isApplet) {
                frame.setDefaultCloseOperation(3);
            }

            frame.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
            Utils.setApplicationIconsForWindow(frame);
            this.container = frame;
        } else {
            this.container = this.viewer;
        }

        this.container.setLayout(new BorderLayout(0, 0));
        this.container.add(this.scroller, "Center");
        if (isSeparateFrame) {
            this.outerPanel.setSize(surface.getPreferredSize());
            this.internalPack((Dimension) null);
            this.container.setVisible(true);
        }

        this.container.validate();
        return this.container;
    }

    public void pack() {
        Dimension outerPanelOldSize = this.outerPanel.getSize();
        this.outerPanel.setSize(this.viewer.getSurface().getPreferredSize());
        if (this.container != this.viewer && !this.viewer.isZoomToFitSelected()) {
            this.internalPack(outerPanelOldSize);
        }

        if (this.buttonsBar != null) {
            this.updateZoomButtonsState();
        }

        this.viewer.updateFrameTitle();
    }

    private void internalPack(Dimension outerPanelOldSize) {
        Rectangle workareaRectangle = this.getWorkareaRectangle();
        if (workareaRectangle.equals(this.container.getBounds())) {
            this.forceResizable = true;
        }

        boolean isHScrollBar = this.scroller.getHorizontalScrollBar().isShowing() && !this.forceResizable;
        boolean isVScrollBar = this.scroller.getVerticalScrollBar().isShowing() && !this.forceResizable;
        boolean isWidthChangeable = true;
        boolean isHeightChangeable = true;
        if (outerPanelOldSize != null && this.viewer.getSurface().oldSize != null) {
            isWidthChangeable = this.forceResizable || outerPanelOldSize.width == this.viewer.getSurface().oldSize.width && !isHScrollBar;
            isHeightChangeable = this.forceResizable || outerPanelOldSize.height == this.viewer.getSurface().oldSize.height && !isVScrollBar;
        }

        this.forceResizable = false;
        this.container.validate();
        Insets containerInsets = this.container.getInsets();
        Dimension preferredSize = this.container.getPreferredSize();
        Rectangle preferredRectangle = new Rectangle(this.container.getLocation(), preferredSize);
        if (null == outerPanelOldSize && workareaRectangle.contains(preferredRectangle)) {
            ((JFrame) this.container).pack();
        } else {
            Dimension minDimension = new Dimension(containerInsets.left + containerInsets.right, containerInsets.top + containerInsets.bottom);
            if (this.buttonsBar != null && this.buttonsBar.isVisible) {
                minDimension.width += this.buttonsBar.getWidth();
                minDimension.height += this.buttonsBar.getHeight();
            }

            Dimension dim = new Dimension(preferredSize);
            Point location = this.container.getLocation();
            int dy;
            int h;
            int dh;
            if (!isWidthChangeable) {
                dim.width = this.container.getWidth();
            } else {
                if (isVScrollBar) {
                    dim.width += this.scroller.getVerticalScrollBar().getWidth();
                }

                if (dim.width < minDimension.width) {
                    dim.width = minDimension.width;
                }

                dy = location.x - workareaRectangle.x;
                if (dy < 0) {
                    dy = 0;
                    location.x = workareaRectangle.x;
                }

                h = workareaRectangle.width - dy;
                if (h < dim.width) {
                    dh = dim.width - h;
                    if (dh < dy) {
                        location.x -= dh;
                    } else {
                        dim.width = workareaRectangle.width;
                        location.x = workareaRectangle.x;
                    }
                }
            }

            if (!isHeightChangeable) {
                dim.height = this.container.getHeight();
            } else {
                if (isHScrollBar) {
                    dim.height += this.scroller.getHorizontalScrollBar().getHeight();
                }

                if (dim.height < minDimension.height) {
                    dim.height = minDimension.height;
                }

                dy = location.y - workareaRectangle.y;
                if (dy < 0) {
                    dy = 0;
                    location.y = workareaRectangle.y;
                }

                h = workareaRectangle.height - dy;
                if (h < dim.height) {
                    dh = dim.height - h;
                    if (dh < dy) {
                        location.y -= dh;
                    } else {
                        dim.height = workareaRectangle.height;
                        location.y = workareaRectangle.y;
                    }
                }
            }

            if (!location.equals(this.container.getLocation())) {
                this.container.setLocation(location);
            }

            if (!this.isFullScreen) {
                this.container.setSize(dim);
            }
        }

        this.scroller.revalidate();
    }

    private Rectangle getWorkareaRectangle() {
        GraphicsConfiguration graphicsConfiguration = this.container.getGraphicsConfiguration();
        Rectangle screenBounds = graphicsConfiguration.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);
        screenBounds.x += screenInsets.left;
        screenBounds.y += screenInsets.top;
        screenBounds.width -= screenInsets.left + screenInsets.right;
        screenBounds.height -= screenInsets.top + screenInsets.bottom;
        return screenBounds;
    }

    void addZoomButtons() {
        this.buttonsBar.createStrut();
        this.zoomOutButton = this.buttonsBar.createButton("zoom-out", "Zoom Out", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ContainerManager.this.zoomFitButton.setSelected(false);
                ContainerManager.this.viewer.getUiSettings().zoomOut();
            }
        });
        this.zoomInButton = this.buttonsBar.createButton("zoom-in", "Zoom In", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ContainerManager.this.zoomFitButton.setSelected(false);
                ContainerManager.this.viewer.getUiSettings().zoomIn();
            }
        });
        this.zoomAsIsButton = this.buttonsBar.createButton("zoom-100", "Zoom 100%", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ContainerManager.this.zoomFitButton.setSelected(false);
                ContainerManager.this.forceResizable = false;
                ContainerManager.this.viewer.getUiSettings().zoomAsIs();
            }
        });
        this.zoomFitButton = this.buttonsBar.createToggleButton("zoom-fit", "Zoom to Fit Window", new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    ContainerManager.this.viewer.setZoomToFitSelected(true);
                    ContainerManager.this.forceResizable = true;
                    ContainerManager.this.zoomToFit();
                    ContainerManager.this.updateZoomButtonsState();
                    ((JComponent) e.getSource()).setBackground(Color.RED);
                } else {
                    ContainerManager.this.viewer.setZoomToFitSelected(false);
                    ((JComponent) e.getSource()).setBackground((Color) null);
                }

                ContainerManager.this.viewer.setSurfaceToHandleKbdFocus();
            }
        });
        if (!this.isSeparateFrame) {
        }

    }

    private void svitchOnFullscreenMode() {
        this.zoomFullScreenButton.setSelected(true);
        this.oldContainerBounds = this.container.getBounds();
        this.setButtonsBarVisible(false);
        this.forceResizable = true;
        JFrame frame = (JFrame) this.container;
        frame.dispose();
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setVisible(true);

        try {
            frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(frame);
            this.isFullScreen = true;
            this.scroller.setVerticalScrollBarPolicy(21);
            this.scroller.setHorizontalScrollBarPolicy(31);
            this.oldScrollerBorder = this.scroller.getBorder();
            this.scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
            (new ContainerManager.FullscreenBorderDetectionThread(frame)).start();
        } catch (Exception var3) {
        }

    }

    private void switchOffFullscreenMode() {
        if (this.isFullScreen) {
            this.zoomFullScreenButton.setSelected(false);
            this.isFullScreen = false;
            this.setButtonsBarVisible(true);
            JFrame frame = (JFrame) this.container;

            try {
                frame.dispose();
                frame.setUndecorated(false);
                frame.setResizable(true);
                frame.getGraphicsConfiguration().getDevice().setFullScreenWindow((Window) null);
            } catch (Exception var3) {
            }

            this.scroller.setVerticalScrollBarPolicy(20);
            this.scroller.setHorizontalScrollBarPolicy(30);
            this.scroller.setBorder(this.oldScrollerBorder);
            this.container.setBounds(this.oldContainerBounds);
            frame.setVisible(true);
            this.pack();
        }

    }

    private void zoomToFit() {
        Dimension scrollerSize = this.scroller.getSize();
        Insets scrollerInsets = this.scroller.getInsets();
        this.viewer.getUiSettings().zoomToFit(scrollerSize.width - scrollerInsets.left - scrollerInsets.right, scrollerSize.height - scrollerInsets.top - scrollerInsets.bottom + (this.isFullScreen ? this.buttonsBar.getHeight() : 0), this.viewer.getWorkingProtocol().getFbWidth(), this.viewer.getWorkingProtocol().getFbHeight());
    }

    void registerResizeListener(Container container) {
        container.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (ContainerManager.this.viewer.isZoomToFitSelected()) {
                    ContainerManager.this.zoomToFit();
                    ContainerManager.this.updateZoomButtonsState();
                    ContainerManager.this.viewer.updateFrameTitle();
                    ContainerManager.this.viewer.setSurfaceToHandleKbdFocus();
                }

            }
        });
    }

    void updateZoomButtonsState() {
        this.zoomOutButton.setEnabled(this.viewer.getUiSettings().getScalePercent() > 10.0D);
        this.zoomInButton.setEnabled(this.viewer.getUiSettings().getScalePercent() < 500.0D);
        this.zoomAsIsButton.setEnabled(this.viewer.getUiSettings().getScalePercent() != 100.0D);
    }

    public ContainerManager.ButtonsBar createButtonsBar() {
        this.buttonsBar = new ContainerManager.ButtonsBar();
        return this.buttonsBar;
    }

    public void setButtonsBarVisible(boolean isVisible) {
        this.buttonsBar.setVisible(isVisible);
        if (isVisible) {
            this.buttonsBar.borderOff();
            this.container.add(this.buttonsBar.bar, "South");
        } else {
            this.container.remove(this.buttonsBar.bar);
            this.buttonsBar.borderOn();
        }

    }

    public void setButtonsBarVisibleFS(boolean isVisible) {
        if (isVisible) {
            if (!this.buttonsBar.isVisible) {
                this.lpane.add(this.buttonsBar.bar, JLayeredPane.POPUP_LAYER, 0);
                int bbWidth = this.buttonsBar.bar.getPreferredSize().width;
                this.buttonsBar.bar.setBounds(this.scroller.getViewport().getViewPosition().x + (this.scroller.getWidth() - bbWidth) / 2, 0, bbWidth, this.buttonsBar.bar.getPreferredSize().height);
                if (null == this.buttonsBarMouseAdapter) {
                    this.buttonsBarMouseAdapter = new ContainerManager.EmptyButtonsBarMouseAdapter();
                }

                this.buttonsBar.bar.addMouseListener(this.buttonsBarMouseAdapter);
            }
        } else {
            this.buttonsBar.bar.removeMouseListener(this.buttonsBarMouseAdapter);
            this.lpane.remove(this.buttonsBar.bar);
            this.lpane.repaint(this.buttonsBar.bar.getBounds());
        }

        this.buttonsBar.setVisible(isVisible);
    }

    private class FullscreenBorderDetectionThread extends Thread {
        public static final int SHOW_HIDE_BUTTONS_BAR_DELAY_IN_MILLS = 700;
        private final JFrame frame;
        private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private ScheduledFuture futureForShow;
        private ScheduledFuture futureForHide;
        private Point mousePoint;
        private Point oldMousePoint;
        private Point viewPosition;

        public FullscreenBorderDetectionThread(JFrame frame) {
            super("FS border detector");
            this.frame = frame;
        }

        public void run() {
            this.setPriority(1);

            while (ContainerManager.this.isFullScreen) {
                this.mousePoint = MouseInfo.getPointerInfo().getLocation();
                if (null == this.oldMousePoint) {
                    this.oldMousePoint = this.mousePoint;
                }

                SwingUtilities.convertPointFromScreen(this.mousePoint, this.frame);
                this.viewPosition = ContainerManager.this.scroller.getViewport().getViewPosition();
                this.processButtonsBarVisibility();
                boolean needScrolling = this.processVScroll() || this.processHScroll();
                this.oldMousePoint = this.mousePoint;
                if (needScrolling) {
                    this.cancelShowExecutor();
                    ContainerManager.this.setButtonsBarVisibleFS(false);
                    this.makeScrolling(this.viewPosition);
                }

                try {
                    Thread.sleep(100L);
                } catch (Exception var3) {
                }
            }

        }

        private boolean processHScroll() {
            Point var10000;
            if (this.mousePoint.x < 20) {
                if (this.viewPosition.x > 0) {
                    int delta = 20 - this.mousePoint.x;
                    if (this.mousePoint.y != this.oldMousePoint.y) {
                        delta *= 2;
                    }

                    var10000 = this.viewPosition;
                    var10000.x -= delta;
                    if (this.viewPosition.x < 0) {
                        this.viewPosition.x = 0;
                    }

                    return true;
                }
            } else if (this.mousePoint.x > this.frame.getWidth() - 20) {
                Rectangle viewRect = ContainerManager.this.scroller.getViewport().getViewRect();
                int right = viewRect.width + viewRect.x;
                if (right < ContainerManager.this.outerPanel.getSize().width) {
                    int deltax = 20 - (this.frame.getWidth() - this.mousePoint.x);
                    if (this.mousePoint.y != this.oldMousePoint.y) {
                        deltax *= 2;
                    }

                    var10000 = this.viewPosition;
                    var10000.x += deltax;
                    if (this.viewPosition.x + viewRect.width > ContainerManager.this.outerPanel.getSize().width) {
                        this.viewPosition.x = ContainerManager.this.outerPanel.getSize().width - viewRect.width;
                    }

                    return true;
                }
            }

            return false;
        }

        private boolean processVScroll() {
            Point var10000;
            if (this.mousePoint.y < 20) {
                if (this.viewPosition.y > 0) {
                    int delta = 20 - this.mousePoint.y;
                    if (this.mousePoint.x != this.oldMousePoint.x) {
                        delta *= 2;
                    }

                    var10000 = this.viewPosition;
                    var10000.y -= delta;
                    if (this.viewPosition.y < 0) {
                        this.viewPosition.y = 0;
                    }

                    return true;
                }
            } else if (this.mousePoint.y > this.frame.getHeight() - 20) {
                Rectangle viewRect = ContainerManager.this.scroller.getViewport().getViewRect();
                int bottom = viewRect.height + viewRect.y;
                if (bottom < ContainerManager.this.outerPanel.getSize().height) {
                    int deltax = 20 - (this.frame.getHeight() - this.mousePoint.y);
                    if (this.mousePoint.x != this.oldMousePoint.x) {
                        deltax *= 2;
                    }

                    var10000 = this.viewPosition;
                    var10000.y += deltax;
                    if (this.viewPosition.y + viewRect.height > ContainerManager.this.outerPanel.getSize().height) {
                        this.viewPosition.y = ContainerManager.this.outerPanel.getSize().height - viewRect.height;
                    }

                    return true;
                }
            }

            return false;
        }

        private void processButtonsBarVisibility() {
            if (this.mousePoint.y < 1) {
                this.cancelHideExecutor();
                if (!ContainerManager.this.buttonsBar.isVisible && (null == this.futureForShow || this.futureForShow.isDone())) {
                    this.futureForShow = this.scheduler.schedule(new Runnable() {
                        public void run() {
                            FullscreenBorderDetectionThread.this.showButtonsBar();
                        }
                    }, 700L, TimeUnit.MILLISECONDS);
                }
            } else {
                this.cancelShowExecutor();
            }

            if (ContainerManager.this.buttonsBar.isVisible && this.mousePoint.y <= ContainerManager.this.buttonsBar.getHeight()) {
                this.cancelHideExecutor();
            }

            if (ContainerManager.this.buttonsBar.isVisible && this.mousePoint.y > ContainerManager.this.buttonsBar.getHeight() && (null == this.futureForHide || this.futureForHide.isDone())) {
                this.futureForHide = this.scheduler.schedule(new Runnable() {
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ContainerManager.this.setButtonsBarVisibleFS(false);
                                ContainerManager.this.container.validate();
                            }
                        });
                    }
                }, 700L, TimeUnit.MILLISECONDS);
            }

        }

        private void cancelHideExecutor() {
            this.cancelExecutor(this.futureForHide);
        }

        private void cancelShowExecutor() {
            this.cancelExecutor(this.futureForShow);
        }

        private void cancelExecutor(ScheduledFuture future) {
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }

        }

        private void makeScrolling(final Point viewPosition) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ContainerManager.this.scroller.getViewport().setViewPosition(viewPosition);
                    Point mousePosition = ContainerManager.this.surface.getMousePosition();
                    if (mousePosition != null) {
                        MouseEvent mouseEvent = new MouseEvent(FullscreenBorderDetectionThread.this.frame, 0, 0L, 0, mousePosition.x, mousePosition.y, 0, false);
                        MouseMotionListener[] arr$ = ContainerManager.this.surface.getMouseMotionListeners();
                        int len$ = arr$.length;

                        for (int i$ = 0; i$ < len$; ++i$) {
                            MouseMotionListener mml = arr$[i$];
                            mml.mouseMoved(mouseEvent);
                        }
                    }

                }
            });
        }

        private void showButtonsBar() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ContainerManager.this.setButtonsBarVisibleFS(true);
                }
            });
        }
    }

    private static class EmptyButtonsBarMouseAdapter extends MouseAdapter {
        private EmptyButtonsBarMouseAdapter() {
        }
    }

    public static class ButtonsBar {
        private static final Insets BUTTONS_MARGIN = new Insets(2, 2, 2, 2);
        private JPanel bar = new JPanel(new FlowLayout(1, 4, 1));
        private boolean isVisible;

        public ButtonsBar() {
        }

        public JButton createButton(String iconId, String tooltipText, ActionListener actionListener) {
            JButton button = new JButton(Utils.getButtonIcon(iconId));
            button.setToolTipText(tooltipText);
            button.setMargin(BUTTONS_MARGIN);
            this.bar.add(button);
            button.addActionListener(actionListener);
            return button;
        }

        public void createStrut() {
            this.bar.add(Box.createHorizontalStrut(10));
        }

        public JToggleButton createToggleButton(String iconId, String tooltipText, ItemListener itemListener) {
            JToggleButton button = new JToggleButton(Utils.getButtonIcon(iconId));
            button.setToolTipText(tooltipText);
            button.setMargin(BUTTONS_MARGIN);
            this.bar.add(button);
            button.addItemListener(itemListener);
            return button;
        }

        public void setVisible(boolean isVisible) {
            this.isVisible = isVisible;
        }

        public int getWidth() {
            return this.bar.getMinimumSize().width;
        }

        public int getHeight() {
            return this.bar.getMinimumSize().height;
        }

        public void borderOn() {
            this.bar.setBorder(BorderFactory.createBevelBorder(0));
        }

        public void borderOff() {
            this.bar.setBorder(BorderFactory.createEmptyBorder());
        }
    }
}
