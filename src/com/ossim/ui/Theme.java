package com.ossim.ui;

import java.awt.*;

public class Theme {
    public static final Color BG       = new Color(0x0A0A0F);
    public static final Color BG2      = new Color(0x111118);
    public static final Color BG3      = new Color(0x16161F);
    public static final Color BORDER   = new Color(0x2A2A3A);
    public static final Color BORDER2  = new Color(0x3A3A50);
    public static final Color TEXT     = new Color(0xE0E0F0);
    public static final Color TEXT2    = new Color(0x9090BB);
    public static final Color ACCENT   = new Color(0x00D4FF);
    public static final Color ACCENT2  = new Color(0x7C3AED);
    public static final Color GREEN    = new Color(0x00E676);
    public static final Color YELLOW   = new Color(0xFFD600);
    public static final Color RED      = new Color(0xFF1744);
    public static final Color ORANGE   = new Color(0xFF6D00);
    public static final Color PURPLE   = new Color(0xAA77FF);

    public static final Font MONO_XS   = new Font("Consolas", Font.PLAIN,  12);
    public static final Font MONO_SM   = new Font("Consolas", Font.PLAIN,  13);
    public static final Font MONO_BOLD = new Font("Consolas", Font.BOLD,   13);
    public static final Font MONO_LG   = new Font("Consolas", Font.BOLD,   15);
    public static final Font HEAD      = new Font("Tahoma",   Font.BOLD,   14);
    public static final Font HEAD_SM   = new Font("Tahoma",   Font.BOLD,   13);
    public static final Font HEAD_XS   = new Font("Tahoma",   Font.BOLD,   12);
    public static final Font HEAD_LG   = new Font("Tahoma",   Font.BOLD,   17);
    public static final Font LABEL     = new Font("Tahoma",   Font.PLAIN,  13);
    public static final Font LABEL_SM  = new Font("Tahoma",   Font.PLAIN,  12);

    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    public static Color statusColor(String status) {
        if (status == null) return TEXT2;
        switch (status.toUpperCase()) {
            case "RUNNING":    return GREEN;
            case "READY":      return ACCENT;
            case "WAITING":    return YELLOW;
            case "DONE":       return new Color(0x666688);
            case "TERMINATED": return RED;
            default:           return TEXT2;
        }
    }
}
