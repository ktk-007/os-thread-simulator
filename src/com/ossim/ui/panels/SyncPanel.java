package com.ossim.ui.panels;

import com.ossim.model.*;
import com.ossim.scheduler.SimEngine;
import com.ossim.ui.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Synchronization Visualizer panel.
 * - Semaphore mode: shows each semaphore value, holder, waiting queue with animated lock icon
 * - Monitor mode: shows a full monitor visualization with condition variable queue
 */
public class SyncPanel extends JPanel {

    private final SimEngine engine;
    private JPanel vizArea;
    private JLabel titleLabel;

    // Monitor animation state
    private String monitorOwner   = null;
    private int    monitorPulse   = 0;
    private final java.util.Deque<String> monitorQueue = new java.util.ArrayDeque<>();
    private Timer monitorAnimTimer;

    public SyncPanel(SimEngine engine) {
        this.engine = engine;
        setBackground(Theme.BG3);
        setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 80));

        titleLabel = new JLabel("  \u25C7 Synchronization — Semaphores");
        titleLabel.setFont(Theme.HEAD_XS);
        titleLabel.setForeground(Theme.TEXT2);
        titleLabel.setBorder(new EmptyBorder(4, 4, 2, 8));
        add(titleLabel, BorderLayout.NORTH);

        vizArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        vizArea.setBackground(Theme.BG3);
        add(vizArea, BorderLayout.CENTER);

        // Monitor animation timer — fires every 900ms to animate queue progress
        monitorAnimTimer = new Timer(900, e -> tickMonitorAnim());
        monitorAnimTimer.setRepeats(true);
    }

    public void refresh() {
        boolean useSem = engine.isUseSemaphores();
        titleLabel.setText("  \u25C7 Synchronization — " + (useSem ? "Semaphores" : "Monitors"));

        vizArea.removeAll();

        if (useSem) {
            monitorAnimTimer.stop();
            buildSemaphoreViz();
        } else {
            if (!monitorAnimTimer.isRunning()) monitorAnimTimer.start();
            syncMonitorState();
            buildMonitorViz();
        }

        vizArea.revalidate();
        vizArea.repaint();
    }

    // ------------------------------------------------------------------ Semaphores
    private void buildSemaphoreViz() {
        List<com.ossim.model.Semaphore> sems = engine.getSemaphores();
        if (sems.isEmpty()) {
            addPlaceholder("Add processes to activate semaphores");
            return;
        }
        for (com.ossim.model.Semaphore s : sems) {
            vizArea.add(buildSemBox(s));
        }
    }

    private JPanel buildSemBox(com.ossim.model.Semaphore s) {
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                Color border = s.isLocked() ? Theme.RED : Theme.GREEN;
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        box.setBorder(new EmptyBorder(2, 6, 2, 6));

        JLabel lockIcon = new JLabel(s.isLocked() ? "[lock]" : "[open]");
        lockIcon.setFont(Theme.MONO_SM);
        lockIcon.setForeground(s.isLocked() ? Theme.RED : Theme.GREEN);
        box.add(lockIcon);

        JLabel name = new JLabel(s.getName());
        name.setFont(Theme.MONO_BOLD);
        name.setForeground(Theme.ACCENT);
        box.add(name);

        Color valCol = s.isLocked() ? Theme.RED : Theme.GREEN;
        JLabel val = new JLabel("val=" + s.getValue());
        val.setFont(Theme.MONO_BOLD);
        val.setForeground(valCol);
        box.add(val);

        String holdStr = s.getHeldBy() != null ? "held by " + s.getHeldBy() : "free";
        JLabel info = new JLabel(holdStr);
        info.setFont(Theme.MONO_SM);
        info.setForeground(Theme.TEXT2);
        box.add(info);

        if (s.getWaitingCount() > 0) {
            JLabel wq = new JLabel("| wait-queue: " + s.getWaitingCount());
            wq.setFont(Theme.MONO_SM);
            wq.setForeground(Theme.YELLOW);
            box.add(wq);
        }
        return box;
    }

    // ------------------------------------------------------------------ Monitor
    private void syncMonitorState() {
        // Build a simulated monitor state from running/waiting threads
        List<SimThread> all = engine.allThreads();
        SimThread runner = all.stream()
            .filter(t -> t.getStatus() == ThreadStatus.RUNNING)
            .findFirst().orElse(null);
        monitorOwner = runner != null ? runner.getPid() + "·" + runner.getTid() : null;

        monitorQueue.clear();
        all.stream()
            .filter(t -> t.getStatus() == ThreadStatus.WAITING)
            .limit(6)
            .forEach(t -> monitorQueue.add(t.getPid() + "·" + t.getTid()));
    }

    private void buildMonitorViz() {
        // Main monitor box
        JPanel monBox = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Theme.PURPLE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        monBox.setOpaque(false);
        monBox.setBorder(new EmptyBorder(5, 10, 5, 10));
        monBox.setPreferredSize(new Dimension(520, 64));

        // Left: Monitor title + owner
        JPanel leftSide = new JPanel(new GridLayout(2, 1, 0, 2));
        leftSide.setOpaque(false);

        JLabel monTitle = new JLabel("MONITOR");
        monTitle.setFont(Theme.MONO_BOLD);
        monTitle.setForeground(Theme.PURPLE);
        leftSide.add(monTitle);

        String ownerText = monitorOwner != null ? "Owner: " + monitorOwner : "Owner: (none)";
        JLabel ownerLbl = new JLabel(ownerText);
        ownerLbl.setFont(Theme.MONO_SM);
        ownerLbl.setForeground(monitorOwner != null ? Theme.GREEN : Theme.TEXT2);
        leftSide.add(ownerLbl);
        monBox.add(leftSide, BorderLayout.WEST);

        // Center: Entry queue animation
        JPanel queuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        queuePanel.setOpaque(false);

        JLabel qLbl = new JLabel("Entry Queue → ");
        qLbl.setFont(Theme.LABEL_SM);
        qLbl.setForeground(Theme.TEXT2);
        queuePanel.add(qLbl);

        if (monitorQueue.isEmpty()) {
            JLabel empty = new JLabel("(empty)");
            empty.setFont(Theme.MONO_SM);
            empty.setForeground(Theme.BORDER2);
            queuePanel.add(empty);
        } else {
            for (String tid : monitorQueue) {
                JLabel chip = new JLabel(tid);
                chip.setFont(Theme.MONO_SM);
                chip.setForeground(Theme.YELLOW);
                chip.setOpaque(true);
                chip.setBackground(Theme.withAlpha(Theme.YELLOW, 25));
                chip.setBorder(new EmptyBorder(1, 5, 1, 5));
                queuePanel.add(chip);
                JLabel arrow = new JLabel("→");
                arrow.setFont(Theme.MONO_SM);
                arrow.setForeground(Theme.BORDER2);
                queuePanel.add(arrow);
            }
        }
        monBox.add(queuePanel, BorderLayout.CENTER);

        // Right: Mutex status indicator
        JPanel rightSide = new JPanel(new GridLayout(2, 1, 0, 2));
        rightSide.setOpaque(false);
        boolean locked = monitorOwner != null;
        JLabel mutexLbl = new JLabel("Mutex");
        mutexLbl.setFont(Theme.HEAD_XS);
        mutexLbl.setForeground(Theme.TEXT2);
        mutexLbl.setHorizontalAlignment(SwingConstants.CENTER);
        rightSide.add(mutexLbl);

        JLabel mutexState = new JLabel(locked ? "LOCKED" : "UNLOCKED") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = locked ? Theme.withAlpha(Theme.RED, 60) : Theme.withAlpha(Theme.GREEN, 40);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mutexState.setFont(Theme.MONO_BOLD);
        mutexState.setForeground(locked ? Theme.RED : Theme.GREEN);
        mutexState.setHorizontalAlignment(SwingConstants.CENTER);
        mutexState.setOpaque(false);
        mutexState.setBorder(new EmptyBorder(1, 6, 1, 6));
        rightSide.add(mutexState);
        monBox.add(rightSide, BorderLayout.EAST);

        vizArea.add(monBox);

        // Condition variable box
        JPanel cvBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(Theme.BG2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Theme.BORDER2);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
            }
        };
        cvBox.setOpaque(false);
        cvBox.setBorder(new EmptyBorder(3, 8, 3, 8));
        cvBox.setPreferredSize(new Dimension(180, 64));

        JLabel cvTitle = new JLabel("Cond. Var. Wait");
        cvTitle.setFont(Theme.HEAD_XS);
        cvTitle.setForeground(Theme.ACCENT2);
        cvBox.add(cvTitle);

        // Show pulse animation dot
        for (int i = 0; i < 3; i++) {
            JLabel dot = new JLabel("●");
            dot.setFont(new Font("Consolas", Font.PLAIN, 18));
            dot.setForeground(i == (monitorPulse % 3)
                ? Theme.withAlpha(Theme.ACCENT2, 230)
                : Theme.withAlpha(Theme.ACCENT2, 60));
            cvBox.add(dot);
        }
        vizArea.add(cvBox);
    }

    private void tickMonitorAnim() {
        monitorPulse++;
        if (!engine.isUseSemaphores()) {
            syncMonitorState();
            vizArea.removeAll();
            buildMonitorViz();
            vizArea.revalidate();
            vizArea.repaint();
        }
    }

    private void addPlaceholder(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.MONO_SM);
        l.setForeground(Theme.BORDER2);
        vizArea.add(l);
    }
}
