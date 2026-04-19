package com.ossim.ui.panels;

import com.ossim.model.*;
import com.ossim.scheduler.SimEngine;
import com.ossim.ui.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class CoresPanel extends JPanel {

    private final SimEngine engine;
    private JPanel grid;

    public CoresPanel(SimEngine engine) {
        this.engine = engine;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("  \u25C7 CPU cores");
        title.setFont(Theme.HEAD_SM);
        title.setForeground(Theme.TEXT2);
        title.setBorder(new EmptyBorder(5, 4, 3, 0));
        add(title, BorderLayout.NORTH);

        grid = new JPanel();
        grid.setBackground(Theme.BG);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Theme.BG);
        scroll.getViewport().setBackground(Theme.BG);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        int n    = engine.getCoreCount();
        int cols = Math.min(n, 4);
        grid.setLayout(new GridLayout(0, cols, 8, 8));
        grid.setBorder(new EmptyBorder(6, 8, 6, 8));
        grid.removeAll();

        List<SimThread> all = engine.allThreads();
        for (int c = 1; c <= n; c++) {
            final int core = c;
            List<SimThread> threads = all.stream()
                .filter(t -> t.getStatus() == ThreadStatus.RUNNING && t.getAssignedCore() == core)
                .collect(Collectors.toList());
            grid.add(buildCoreBox(core, threads));
        }

        grid.revalidate();
        grid.repaint();
    }

    private JPanel buildCoreBox(int coreNum, List<SimThread> threads) {
        boolean active = !threads.isEmpty();

        JPanel box = new JPanel(new BorderLayout(0, 3)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                Color borderCol = active ? Theme.ACCENT : Theme.BORDER;
                if (active) {
                    g2.setColor(Theme.withAlpha(Theme.ACCENT, 15));
                    g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 7, 7);
                }
                g2.setColor(borderCol);
                g2.setStroke(new BasicStroke(active ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(7, 8, 4, 8));
        box.setPreferredSize(new Dimension(150, 90));

        JLabel coreLabel = new JLabel("Core " + coreNum);
        coreLabel.setFont(Theme.HEAD_XS);
        coreLabel.setForeground(active ? Theme.ACCENT : Theme.TEXT2);
        box.add(coreLabel, BorderLayout.NORTH);

        JPanel threadArea = new JPanel();
        threadArea.setLayout(new BoxLayout(threadArea, BoxLayout.Y_AXIS));
        threadArea.setOpaque(false);

        if (threads.isEmpty()) {
            JLabel idle = new JLabel("idle");
            idle.setFont(Theme.MONO_SM);
            idle.setForeground(Theme.BORDER2);
            idle.setAlignmentX(LEFT_ALIGNMENT);
            threadArea.add(idle);
        } else {
            for (SimThread t : threads) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

                JLabel tl = new JLabel(t.getPid() + "·" + t.getTid());
                tl.setFont(Theme.MONO_BOLD);
                tl.setForeground(t.getColor());
                row.add(tl);

                JLabel rem = new JLabel(t.getRemainingTime() + "t");
                rem.setFont(Theme.MONO_SM);
                rem.setForeground(Theme.TEXT2);
                row.add(rem);
                threadArea.add(row);
            }
        }
        box.add(threadArea, BorderLayout.CENTER);

        // Progress bar
        if (!threads.isEmpty()) {
            SimThread t = threads.get(0);
            double pct = t.getCompletionPercent() / 100.0;
            JPanel barBg = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(Theme.BORDER);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(t.getColor());
                    g.fillRect(0, 0, (int)(getWidth() * pct), getHeight());
                }
            };
            barBg.setPreferredSize(new Dimension(0, 4));
            barBg.setOpaque(false);
            box.add(barBg, BorderLayout.SOUTH);
        }

        return box;
    }
}
