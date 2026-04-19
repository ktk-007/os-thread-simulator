package com.ossim.ui.panels;

import com.ossim.model.*;
import com.ossim.scheduler.SimEngine;
import com.ossim.ui.Theme;
import com.ossim.ui.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class ProcessPanel extends JPanel {

    public interface ProcessPanelListener {
        void onAddProcess();
        void onAddThread(String pid);
        void onTerminate(String pid);
    }

    private final SimEngine engine;
    private ProcessPanelListener listener;
    private JPanel listPanel;

    public ProcessPanel(SimEngine engine) {
        this.engine = engine;
        setBackground(Theme.BG2);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(248, 0));
        build();
    }

    public void setListener(ProcessPanelListener l) { this.listener = l; }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG2);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.add(UIUtils.sectionTitle("\u25C7 Processes"), BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.BG2);
        listPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        JScrollPane scroll = UIUtils.darkScroll(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        JButton addBtn = UIUtils.makeButton("+  Add Process", Theme.ACCENT, Theme.ACCENT);
        addBtn.setPreferredSize(new Dimension(220, 34));
        addBtn.setFont(Theme.HEAD_SM);
        addBtn.addActionListener(e -> { if (listener != null) listener.onAddProcess(); });
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(Theme.BG2);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));
        btnPanel.add(addBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void refresh() {
        listPanel.removeAll();
        for (SimProcess proc : engine.getProcesses()) {
            listPanel.add(buildProcessRow(proc));
            listPanel.add(Box.createVerticalStrut(5));
        }

        if (engine.getProcesses().isEmpty()) {
            JLabel hint = new JLabel("<html><center>No processes.<br>Click 'Add Process'<br>to begin.</center></html>");
            hint.setFont(Theme.LABEL_SM);
            hint.setForeground(Theme.TEXT2);
            hint.setHorizontalAlignment(SwingConstants.CENTER);
            hint.setAlignmentX(CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(30));
            listPanel.add(hint);
        }

        listPanel.add(Box.createVerticalGlue());
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildProcessRow(SimProcess proc) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(Theme.BG2);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 1, 1, 1, Theme.withAlpha(proc.getColor(), 100)),
            new EmptyBorder(0, 0, 4, 0)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        header.setBackground(Theme.withAlpha(proc.getColor(), 40));
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Consolas", Font.PLAIN, 14));
        dot.setForeground(proc.getColor());
        header.add(dot);
        JLabel pid = new JLabel(proc.getPid());
        pid.setFont(Theme.HEAD_SM);
        pid.setForeground(proc.getColor());
        header.add(pid);

        String agg = processAggregateStatus(proc);
        Color aggColor = Theme.statusColor(agg);
        JLabel statusBadge = new JLabel(agg);
        statusBadge.setFont(Theme.MONO_XS);
        statusBadge.setForeground(aggColor);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(Theme.withAlpha(aggColor, 35));
        statusBadge.setBorder(new EmptyBorder(1, 6, 1, 6));
        header.add(statusBadge);

        long alive = proc.getThreads().stream()
            .filter(t -> t.getStatus() != ThreadStatus.DONE && t.getStatus() != ThreadStatus.TERMINATED).count();
        JLabel cnt = new JLabel(alive + "/" + proc.getThreads().size() + " t");
        cnt.setFont(Theme.LABEL_SM);
        cnt.setForeground(Theme.TEXT2);
        header.add(cnt);
        panel.add(header, BorderLayout.NORTH);

        // Thread pills
        JPanel threadArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        threadArea.setBackground(Theme.BG2);
        for (SimThread t : proc.getThreads()) {
            String st = threadStatusAbbrev(t.getStatus());
            Color fg = Theme.statusColor(t.getStatus().getLabel());
            JLabel pill = new JLabel(t.getTid() + " " + st);
            pill.setFont(Theme.MONO_XS);
            pill.setForeground(fg);
            pill.setOpaque(true);
            pill.setBackground(Theme.withAlpha(fg, 30));
            pill.setBorder(new EmptyBorder(2, 5, 2, 5));
            threadArea.add(pill);
        }
        panel.add(threadArea, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        actions.setBackground(Theme.BG2);
        actions.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));

        JButton addT = UIUtils.makeButton("+ Thread", Theme.BORDER2, Theme.TEXT);
        addT.setFont(Theme.LABEL_SM);
        addT.setPreferredSize(new Dimension(78, 24));
        addT.addActionListener(e -> { if (listener != null) listener.onAddThread(proc.getPid()); });
        actions.add(addT);

        JButton term = UIUtils.makeButton("Kill", Theme.RED, Theme.RED);
        term.setFont(Theme.LABEL_SM);
        term.setPreferredSize(new Dimension(52, 24));
        term.addActionListener(e -> { if (listener != null) listener.onTerminate(proc.getPid()); });
        actions.add(term);

        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    /** Short label for thread rows (matches reference-style abbreviations). */
    private static String threadStatusAbbrev(ThreadStatus s) {
        switch (s) {
            case RUNNING:    return "run";
            case READY:      return "rdy";
            case WAITING:    return "wait";
            case DONE:       return "done";
            case TERMINATED: return "term";
            default:         return s.getLabel();
        }
    }

    /**
     * Rolled-up status for the process card: surfaces waiting prominently (any thread blocked).
     */
    private static String processAggregateStatus(SimProcess proc) {
        var threads = proc.getThreads();
        boolean anyWait = threads.stream().anyMatch(t -> t.getStatus() == ThreadStatus.WAITING);
        boolean anyRun  = threads.stream().anyMatch(t -> t.getStatus() == ThreadStatus.RUNNING);
        boolean anyAlive = threads.stream().anyMatch(t ->
            t.getStatus() != ThreadStatus.DONE && t.getStatus() != ThreadStatus.TERMINATED);
        if (!anyAlive) {
            if (threads.stream().allMatch(t -> t.getStatus() == ThreadStatus.DONE)) return "Done";
            if (threads.stream().allMatch(t -> t.getStatus() == ThreadStatus.TERMINATED)) return "Terminated";
            return "Done";
        }
        if (anyWait) return "Waiting";
        if (anyRun)  return "Running";
        return "Ready";
    }
}
