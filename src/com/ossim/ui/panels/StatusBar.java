package com.ossim.ui.panels;

import com.ossim.model.ThreadStatus;
import com.ossim.scheduler.SimEngine;
import com.ossim.ui.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class StatusBar extends JPanel {

    private final SimEngine engine;
    private JLabel clockLabel, procsLabel, threadsLabel, runLabel, waitLabel, doneLabel, modeLabel;
    private JPanel tickDot;
    private Timer  tickTimer;

    public StatusBar(SimEngine engine) {
        this.engine = engine;
        setBackground(Theme.BG2);
        setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));
        setPreferredSize(new Dimension(0, 34));
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        left.setOpaque(false);

        tickDot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, 9, 9);
                g2.dispose();
            }
        };
        tickDot.setPreferredSize(new Dimension(9, 9));
        tickDot.setBackground(Theme.BORDER2);
        tickDot.setOpaque(false);
        left.add(tickDot);

        left.add(statItem("Clock",      clockLabel   = val("0")));
        left.add(statItem("Procs",      procsLabel   = val("0")));
        left.add(statItem("Threads",    threadsLabel = val("0")));
        left.add(statItem("Running",    runLabel     = val("0")));
        left.add(statItem("Waiting",    waitLabel    = val("0")));
        left.add(statItem("Finished",   doneLabel    = val("0")));

        modeLabel = new JLabel("One-One | Round Robin");
        modeLabel.setFont(Theme.MONO_SM);
        modeLabel.setForeground(Theme.ACCENT);
        modeLabel.setBorder(new EmptyBorder(0, 0, 0, 6));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        right.setOpaque(false);
        right.add(modeLabel);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        tickTimer = new Timer(180, e -> {
            tickDot.setBackground(Theme.BORDER2);
            tickDot.repaint();
        });
        tickTimer.setRepeats(false);
    }

    public void refresh() {
        var all = engine.allThreads();
        clockLabel.setText(String.valueOf(engine.getClock()));
        procsLabel.setText(String.valueOf(engine.getProcesses().size()));
        threadsLabel.setText(String.valueOf(all.size()));
        runLabel.setText(String.valueOf(all.stream().filter(t -> t.getStatus() == ThreadStatus.RUNNING).count()));
        waitLabel.setText(String.valueOf(all.stream().filter(t -> t.getStatus() == ThreadStatus.WAITING).count()));
        long finished = all.stream().filter(t -> t.getStatus() == ThreadStatus.DONE
            || t.getStatus() == ThreadStatus.TERMINATED).count();
        doneLabel.setText(String.valueOf(finished));
        modeLabel.setText(engine.getThreadModel().getLabel() + " | " + engine.getAlgo().getLabel());
        tickDot.setBackground(Theme.GREEN);
        tickDot.repaint();
        tickTimer.restart();
    }

    private JPanel statItem(String labelText, JLabel val) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText + ":");
        l.setFont(Theme.LABEL_SM);
        l.setForeground(Theme.TEXT2);
        p.add(l);
        p.add(val);
        return p;
    }

    private JLabel val(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.MONO_BOLD);
        l.setForeground(Theme.TEXT);
        return l;
    }
}
