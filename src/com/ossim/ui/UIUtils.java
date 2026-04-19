package com.ossim.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class UIUtils {

    public static void applyDarkLook() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        UIManager.put("Panel.background",                Theme.BG);
        UIManager.put("ScrollPane.background",           Theme.BG);
        UIManager.put("Viewport.background",             Theme.BG2);
        UIManager.put("Table.background",                Theme.BG2);
        UIManager.put("Table.foreground",                Theme.TEXT);
        UIManager.put("Table.gridColor",                 Theme.BORDER);
        UIManager.put("Table.font",                      Theme.MONO_SM);
        UIManager.put("Table.selectionBackground",       Theme.withAlpha(Theme.ACCENT, 40));
        UIManager.put("Table.selectionForeground",       Theme.TEXT);
        UIManager.put("TableHeader.background",          Theme.BG3);
        UIManager.put("TableHeader.foreground",          Theme.TEXT2);
        UIManager.put("TableHeader.font",                Theme.HEAD_XS);
        UIManager.put("ScrollBar.background",            Theme.BG2);
        UIManager.put("ScrollBar.thumb",                 Theme.BORDER2);
        UIManager.put("ScrollBar.track",                 Theme.BG2);
        UIManager.put("ComboBox.background",             Theme.BG2);
        UIManager.put("ComboBox.foreground",             Theme.TEXT);
        UIManager.put("ComboBox.font",                   Theme.LABEL);
        UIManager.put("ComboBox.selectionBackground",    Theme.BORDER2);
        UIManager.put("TextField.background",            Theme.BG2);
        UIManager.put("TextField.foreground",            Theme.TEXT);
        UIManager.put("TextField.font",                  Theme.MONO_SM);
        UIManager.put("TextField.caretForeground",       Theme.ACCENT);
        UIManager.put("Spinner.background",              Theme.BG2);
        UIManager.put("RadioButton.background",          Theme.BG2);
        UIManager.put("RadioButton.foreground",          Theme.TEXT2);
        UIManager.put("RadioButton.font",                Theme.LABEL);
        UIManager.put("Label.foreground",                Theme.TEXT);
        UIManager.put("Label.font",                      Theme.LABEL);
        UIManager.put("TabbedPane.background",           Theme.BG2);
        UIManager.put("TabbedPane.foreground",           Theme.TEXT2);
        UIManager.put("TabbedPane.font",                 Theme.LABEL_SM);
        UIManager.put("TabbedPane.selected",             Theme.BG3);
        UIManager.put("SplitPane.background",            Theme.BG);
        UIManager.put("SplitPane.dividerSize",           4);
        UIManager.put("OptionPane.background",           Theme.BG2);
        UIManager.put("OptionPane.messageForeground",    Theme.TEXT);
        UIManager.put("OptionPane.font",                 Theme.LABEL);
        UIManager.put("Button.background",               Theme.BG2);
        UIManager.put("Button.foreground",               Theme.TEXT);
        UIManager.put("Button.font",                     Theme.LABEL);
    }

    public static class OutlinedButton extends JButton {
        private Color accent;

        public OutlinedButton(String text, Color borderColor, Color textColor) {
            super(text);
            this.accent = borderColor;
            setForeground(textColor);
            setFont(Theme.HEAD_SM);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void setAccent(Color borderColor, Color textColor) {
            this.accent = borderColor;
            setForeground(textColor);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed())       g2.setColor(Theme.withAlpha(accent, 80));
            else if (getModel().isRollover()) g2.setColor(Theme.withAlpha(accent, 40));
            else                              g2.setColor(Theme.BG2);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static OutlinedButton makeButton(String text, Color borderColor, Color textColor) {
        return new OutlinedButton(text, borderColor, textColor);
    }

    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(Theme.HEAD_SM);
        l.setForeground(Theme.ACCENT);
        l.setBorder(new EmptyBorder(5, 10, 5, 8));
        return l;
    }

    public static Border lineBorder() {
        return BorderFactory.createLineBorder(Theme.BORDER);
    }

    public static JScrollPane darkScroll(JComponent comp) {
        JScrollPane sp = new JScrollPane(comp);
        sp.setBackground(Theme.BG2);
        sp.getViewport().setBackground(Theme.BG2);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getVerticalScrollBar().setBackground(Theme.BG2);
        sp.getHorizontalScrollBar().setBackground(Theme.BG2);
        return sp;
    }

    public static JComboBox<String> darkCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(Theme.BG2);
        cb.setForeground(Theme.TEXT);
        cb.setFont(Theme.LABEL);
        cb.setBorder(BorderFactory.createLineBorder(Theme.BORDER2));
        return cb;
    }

    public static JSpinner darkSpinner(int val, int min, int max) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(val, min, max, 1));
        sp.setBackground(Theme.BG2);
        sp.setFont(Theme.LABEL);
        JSpinner.DefaultEditor ed = (JSpinner.DefaultEditor) sp.getEditor();
        ed.getTextField().setBackground(Theme.BG2);
        ed.getTextField().setForeground(Theme.TEXT);
        ed.getTextField().setFont(Theme.MONO_SM);
        ed.getTextField().setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER2));
        return sp;
    }
}
