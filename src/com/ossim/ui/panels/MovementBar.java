package com.ossim.ui.panels;

import com.ossim.model.MoveEvent;
import com.ossim.ui.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MovementBar extends JPanel {

    private static final int MAX_VISIBLE = 12;
    private final List<MoveEvent> recentEvents = new ArrayList<>();
    private JPanel eventContainer;

    public MovementBar() {
        setBackground(Theme.BG2);
        setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 44));

        JLabel label = new JLabel("  \u25B8 Thread movement:");
        label.setFont(Theme.HEAD_SM);
        label.setForeground(Theme.ACCENT);
        label.setBorder(new EmptyBorder(0, 4, 0, 8));
        add(label, BorderLayout.WEST);

        eventContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        eventContainer.setBackground(Theme.BG2);
        add(eventContainer, BorderLayout.CENTER);
    }

    public void addEvents(List<MoveEvent> events) {
        recentEvents.addAll(events);
        if (recentEvents.size() > MAX_VISIBLE)
            recentEvents.subList(0, recentEvents.size() - MAX_VISIBLE).clear();
        refresh();
    }

    public void clear() { recentEvents.clear(); refresh(); }

    private void refresh() {
        eventContainer.removeAll();
        for (MoveEvent e : recentEvents) {
            JLabel chip = new JLabel(e.pid + "·" + e.tid + " → " + e.to);
            chip.setFont(Theme.MONO_SM);
            chip.setForeground(e.color);
            chip.setOpaque(true);
            chip.setBackground(new Color(e.color.getRed(), e.color.getGreen(), e.color.getBlue(), 22));
            chip.setBorder(new EmptyBorder(2, 7, 2, 7));
            eventContainer.add(chip);
        }
        eventContainer.revalidate();
        eventContainer.repaint();
    }
}
