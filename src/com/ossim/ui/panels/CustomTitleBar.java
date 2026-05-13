package com.ossim.ui.panels;

import com.ossim.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Custom themed title bar replacing the OS native window decoration.
 * Supports drag-to-move, minimize, maximize/restore, and close.
 */
public class CustomTitleBar extends JPanel {

    private final JFrame owner;
    private Point dragOrigin;
    private Rectangle restoreBounds;
    private boolean maximized = false;

    // Buttons
    private final TitleBarBtn closeBtn;
    private final TitleBarBtn maxBtn;
    private final TitleBarBtn minBtn;

    public CustomTitleBar(JFrame owner) {
        this.owner = owner;
        setBackground(Theme.BG2);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 50));

        // ---- Left: app icon + title ----
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        left.setOpaque(false);

        // Coloured dot acting as a simple logo
        JLabel logo = new JLabel("⬡");
        logo.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        logo.setForeground(Theme.ACCENT);
        left.add(logo);

        JLabel title = new JLabel("RTOS ThreadVision (Thread Simulator)");
        title.setFont(new Font("Tahoma", Font.BOLD, 18));
        title.setForeground(Theme.TEXT);
        left.add(title);

        add(left, BorderLayout.CENTER);

        // ---- Right: min / max / close ----
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        minBtn   = new TitleBarBtn("–", Theme.TEXT2,  Theme.BORDER2);
        maxBtn   = new TitleBarBtn("⬜", Theme.TEXT2, Theme.BORDER2);
        closeBtn = new TitleBarBtn("✕", Theme.RED,    new Color(0xC0392B));

        minBtn.addActionListener(e -> owner.setState(JFrame.ICONIFIED));
        maxBtn.addActionListener(e -> toggleMaximize());
        closeBtn.addActionListener(e -> { owner.dispatchEvent(new WindowEvent(owner, WindowEvent.WINDOW_CLOSING)); });

        right.add(minBtn);
        right.add(maxBtn);
        right.add(closeBtn);
        add(right, BorderLayout.EAST);

        // ---- Drag-to-move ----
        MouseAdapter dragger = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { dragOrigin = e.getLocationOnScreen(); }
            @Override public void mouseDragged(MouseEvent e) {
                if (maximized) return; // don't drag while maximized
                Point now = e.getLocationOnScreen();
                owner.setLocation(
                    owner.getX() + (now.x - dragOrigin.x),
                    owner.getY() + (now.y - dragOrigin.y)
                );
                dragOrigin = now;
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) toggleMaximize();
            }
        };
        addMouseListener(dragger);
        addMouseMotionListener(dragger);
        left.addMouseListener(dragger);
        left.addMouseMotionListener(dragger);
    }

    private void toggleMaximize() {
        if (!maximized) {
            restoreBounds = owner.getBounds();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            owner.setBounds(ge.getMaximumWindowBounds());
            maximized = true;
            maxBtn.setText("❐");
        } else {
            owner.setBounds(restoreBounds);
            maximized = false;
            maxBtn.setText("⬜");
        }
    }

    // ── Inner button class ──────────────────────────────────────────────────
    static class TitleBarBtn extends JButton {
        private final Color hoverBg;

        TitleBarBtn(String text, Color fg, Color hoverBg) {
            super(text);
            this.hoverBg = hoverBg;
            setForeground(fg);
            setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(60, 60));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            if (getModel().isRollover() || getModel().isPressed()) {
                g.setColor(hoverBg);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            super.paintComponent(g);
        }
    }
}
