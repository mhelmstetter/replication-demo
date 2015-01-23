package com.mongodb.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;


public class DockedInternalFrame extends JInternalFrame {

    /* (non-Javadoc)
     * @see javax.swing.JInternalFrame#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
    private boolean moveEnabled = true;

    protected Action filterAction;
    
    private HeaderPanel headerPanel;
    private JLabel titleLabel;
    
    public String toString() {
        return titleLabel.getText();
    }

    public DockedInternalFrame(String title, JToolBar toolBar) {
        super(title);
        ComponentUI frameUI = this.getUI();
        if (frameUI instanceof BasicInternalFrameUI) {
            BasicInternalFrameUI bui = (BasicInternalFrameUI) frameUI;
            // Have to do this or we'll get an exception
            setRootPaneCheckingEnabled(false);
            titleLabel = new JLabel(title);
            headerPanel = new HeaderPanel(titleLabel, toolBar);
            headerPanel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        try {
                            DockedInternalFrame.this.setMaximum(true);
                        } catch (PropertyVetoException e1) {
                            e1.printStackTrace();
                        }
                    }
                }    
            });
            bui.setNorthPane(headerPanel);
        } else {
            throw new IllegalArgumentException("Expected BasicInternalFrameUI but found " + frameUI.getClass().getName());
        }

    }
    
    public void setToolBar(JToolBar toolBar) {
        headerPanel.setToolBar(toolBar);
    }
    

    protected void processMouseMotionEvent(MouseEvent e) {
        if (moveEnabled) {
            super.processMouseMotionEvent(e);
        }
    }

    public void internalSetBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
    public void setBounds(int x, int y, int width, int height) {
        if (moveEnabled) {
            super.setBounds(x, y, width, height);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Component#setBounds(java.awt.Rectangle)
     */
    public void setBounds(Rectangle r) {
        if (moveEnabled) {
            super.setBounds(r);
        }
    }

    /**
     * @param moveEnabled
     *            The moveEnabled to set.
     */
    public void setMoveEnabled(boolean moveEnabled) {
        this.moveEnabled = moveEnabled;
    }
    
    private class HeaderPanel extends JPanel {
        private GradientPanel gradientPanel;
        public HeaderPanel(JLabel label, JToolBar bar) {
            super();
            this.setLayout(new BorderLayout());
            Color selectedTitleGradientColor = UIManager
                    .getColor("InternalFrame.activeTitleBackground");           

            gradientPanel = new GradientPanel(new BorderLayout(),
                    selectedTitleGradientColor);
            label.setOpaque(false);
            label.setBorder(new EmptyBorder(0, 5, 0, 0));

            gradientPanel.add(label, BorderLayout.WEST);
            gradientPanel.setBorder(new LineBorder(Color.GRAY, 1));
            add(gradientPanel, BorderLayout.CENTER);

            if (bar != null) {
                setToolBar(bar);
            }

            setOpaque(false);
        }
        
        public void setToolBar(JToolBar bar) {
            bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            gradientPanel.add(bar, BorderLayout.EAST);
        }

    }

    // A panel with a horizontal gradient background.
    private static class GradientPanel extends JPanel {

        private GradientPanel(LayoutManager lm, Color background) {
            super(lm);
            setBackground(background);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isOpaque()) {
                return;
            }
            Color control = UIManager.getColor("control");
            int width = getWidth();
            int height = getHeight();

            Graphics2D g2 = (Graphics2D) g;
            Paint storedPaint = g2.getPaint();
            g2.setPaint(new GradientPaint(0, 0, getBackground(), width, 0,
                    control));
            g2.fillRect(0, 0, width, height);
            g2.setPaint(storedPaint);
        }
    }    
}
