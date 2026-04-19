package com.ossim.ui.panels;

import com.ossim.model.*;
import com.ossim.scheduler.SimEngine;
import com.ossim.ui.Theme;
import com.ossim.ui.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class RightTablesPanel extends JPanel {

    private final SimEngine engine;

    private DefaultTableModel detailModel;
    private JTable            detailTable;
    private DefaultTableModel timingModel;
    private JTable            timingTable;
    private DefaultTableModel statusModel;
    private JTable            statusTable;
    private DefaultTableModel kthreadModel;
    private JTable            kthreadTable;

    // Gantt
    private GanttPanel ganttPanel;

    public RightTablesPanel(SimEngine engine) {
        this.engine = engine;
        setBackground(Theme.BG2);
        setBorder(new MatteBorder(0, 1, 0, 0, Theme.BORDER));
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        // ---- Gantt chart (top, larger + time ruler) ----
        ganttPanel = new GanttPanel();
        JPanel ganttWrap = wrapPanel("\u25C7 Gantt Chart", ganttPanel);
        ganttWrap.setPreferredSize(new Dimension(0, 182));
        ganttWrap.setMinimumSize(new Dimension(0, 148));

        // ---- Tabbed thread tables ----
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(Theme.BG2);
        tabs.setForeground(Theme.TEXT2);
        tabs.setFont(Theme.LABEL_SM);

        // Tab 1: Thread Details (compact like reference UI)
        String[] detailCols = {"PID", "TID", "Algo", "Burst", "TQ", "Pri", "Rem", "Sync", "Mem"};
        detailModel = makeModel(detailCols);
        detailTable = makeTable(detailModel, true);
        setColumnWidths(detailTable, 48, 52, 42, 44, 36, 36, 44, 72, 52);
        tabs.addTab("Thread Details", UIUtils.darkScroll(detailTable));

        // Tab 2: Timing (wait + turnaround + start/finish times)
        String[] timingCols = {"PID", "TID", "Status", "Wait", "Turnaround", "Start", "Finish"};
        timingModel = makeModel(timingCols);
        timingTable = makeTable(timingModel, false);
        setColumnWidths(timingTable, 48, 52, 72, 44, 72, 52, 52);
        tabs.addTab("Timing", UIUtils.darkScroll(timingTable));

        // Tab 3: Thread status (core + kernel + status)
        String[] statusCols = {"PID", "TID", "Core", "Kernel", "Status"};
        statusModel = makeModel(statusCols);
        statusTable = makeTable(statusModel, false);
        setColumnWidths(statusTable, 48, 52, 56, 88, 72);
        tabs.addTab("Thread Status", UIUtils.darkScroll(statusTable));

        // Tab 4: Kernel thread map
        String[] kCols = {"Kernel", "User threads", "Model"};
        kthreadModel = makeModel(kCols);
        kthreadTable = makeTable(kthreadModel, false);
        setColumnWidths(kthreadTable, 72, 220, 72);
        tabs.addTab("Kernel Map", UIUtils.darkScroll(kthreadTable));

        JPanel tabsWrap = new JPanel(new BorderLayout());
        tabsWrap.setBackground(Theme.BG2);
        tabsWrap.add(tabs, BorderLayout.CENTER);

        // Main split: gantt on top, tables below
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ganttWrap, tabsWrap);
        split.setDividerSize(4);
        split.setDividerLocation(188);
        split.setResizeWeight(0.0);
        split.setBackground(Theme.BG);
        add(split, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------ Gantt
    private static class GanttEntry {
        String label;
        Color  color;
        int    startTick;
        int    endTick; // exclusive
        GanttEntry(String l, Color c, int s, int e) { label=l; color=c; startTick=s; endTick=e; }
    }

    public class GanttPanel extends JPanel {
        private final List<GanttEntry> entries = new ArrayList<>();
        private int maxTick = 1;

        public GanttPanel() {
            setBackground(Theme.BG3);
            setPreferredSize(new Dimension(0, 148));
        }

        public void addEntry(String label, Color color, int startTick, int endTick) {
            entries.add(new GanttEntry(label, color, startTick, endTick));
            maxTick = Math.max(maxTick, endTick + 1);
        }

        public void clear() { entries.clear(); maxTick = 1; }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int paddingLeft = 76;
            int rulerH = 24;
            int paddingTop = rulerH + 10;
            int paddingBot = 18;
            int paddingRight = 8;
            int chartW = W - paddingLeft - paddingRight;
            int chartH = H - paddingTop - paddingBot;

            // Background
            g2.setColor(Theme.BG3);
            g2.fillRect(0, 0, W, H);

            if (entries.isEmpty()) {
                g2.setFont(Theme.MONO_SM);
                g2.setColor(Theme.BORDER2);
                g2.drawString("Run Step or Start to populate the timeline", paddingLeft + 6, H / 2);
                g2.dispose();
                return;
            }

            int tickSpan = Math.max(maxTick, 1);
            float tickW = (float) chartW / tickSpan;

            // -------- Time ruler (reference-style top row) --------
            g2.setColor(Theme.withAlpha(Theme.BG2, 220));
            g2.fillRect(paddingLeft, 0, chartW, rulerH - 2);
            g2.setColor(Theme.BORDER);
            g2.drawLine(paddingLeft, rulerH - 1, paddingLeft + chartW, rulerH - 1);

            g2.setFont(Theme.MONO_XS);
            g2.setColor(Theme.TEXT2);
            int step = Math.max(1, tickSpan / 14);
            for (int t = 0; t <= tickSpan; t += step) {
                int x = paddingLeft + (int) (t * tickW);
                g2.setColor(Theme.withAlpha(Theme.BORDER2, 200));
                g2.drawLine(x, rulerH - 9, x, rulerH - 3);
                g2.setColor(Theme.TEXT2);
                g2.drawString(String.valueOf(t), x + 3, 14);
            }
            g2.setColor(Theme.TEXT2);
            g2.drawString("t", 6, 14);

            // Collect unique pid-tid labels (stable order)
            List<String> rows = new ArrayList<>();
            for (GanttEntry e : entries) if (!rows.contains(e.label)) rows.add(e.label);
            int nRows = rows.size();
            float rowH = (float) chartH / Math.max(nRows, 1);

            // Vertical grid (lighter)
            g2.setStroke(new BasicStroke(1f));
            for (int t = 0; t <= tickSpan; t += step) {
                int x = paddingLeft + (int) (t * tickW);
                g2.setColor(Theme.withAlpha(Theme.BORDER, 60));
                g2.drawLine(x, paddingTop, x, paddingTop + chartH);
            }

            // Row labels + separators
            g2.setFont(Theme.MONO_XS);
            for (int i = 0; i < rows.size(); i++) {
                int y = paddingTop + (int) (i * rowH);
                g2.setColor(Theme.TEXT2);
                String lbl = rows.get(i);
                if (g2.getFontMetrics().stringWidth(lbl) > paddingLeft - 6)
                    lbl = lbl.length() > 10 ? lbl.substring(0, 9) + "\u2026" : lbl;
                g2.drawString(lbl, 4, y + (int) (rowH / 2) + 4);
                g2.setColor(Theme.withAlpha(Theme.BORDER, 140));
                g2.drawLine(paddingLeft, y, paddingLeft + chartW, y);
            }

            // Draw blocks
            for (GanttEntry e : entries) {
                int row = rows.indexOf(e.label);
                if (row < 0) continue;
                int x = paddingLeft + (int) (e.startTick * tickW);
                int w = Math.max(3, (int) ((e.endTick - e.startTick) * tickW));
                int y = paddingTop + (int) (row * rowH) + 3;
                int rh = (int) rowH - 6;

                GradientPaint gp = new GradientPaint(x, y, Theme.withAlpha(e.color, 210), x, y + rh, Theme.withAlpha(e.color, 120));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, w, rh, 5, 5);
                g2.setPaint(null);
                g2.setColor(e.color);
                g2.setStroke(new BasicStroke(1.1f));
                g2.drawRoundRect(x, y, w, rh, 5, 5);

                if (w > 34) {
                    g2.setFont(new Font("Consolas", Font.BOLD, 10));
                    g2.setColor(new Color(0xF5F5F5));
                    String bl = e.label.contains("\u00B7") ? e.label.split("\u00B7")[1] : e.label;
                    FontMetrics fm = g2.getFontMetrics();
                    if (fm.stringWidth(bl) < w - 6)
                        g2.drawString(bl, x + 4, y + rh / 2 + 4);
                }
            }

            // Axis frame
            g2.setColor(Theme.BORDER2);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(paddingLeft, paddingTop + chartH, paddingLeft + chartW, paddingTop + chartH);
            g2.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartH);

            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ Table helpers
    private DefaultTableModel makeModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable makeTable(TableModel model, boolean hScroll) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? Theme.BG2 : Theme.BG3);
                c.setForeground(Theme.TEXT);
                if (isRowSelected(row)) c.setBackground(Theme.withAlpha(Theme.ACCENT, 35));
                return c;
            }
        };
        t.setBackground(Theme.BG2);
        t.setForeground(Theme.TEXT);
        t.setFont(Theme.MONO_SM);
        t.setGridColor(Theme.BORDER);
        t.setShowGrid(true);
        t.setRowHeight(24);
        t.setSelectionBackground(Theme.withAlpha(Theme.ACCENT, 40));
        t.setSelectionForeground(Theme.TEXT);
        t.getTableHeader().setBackground(Theme.BG3);
        t.getTableHeader().setForeground(Theme.TEXT2);
        t.getTableHeader().setFont(Theme.HEAD_XS);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        t.setAutoResizeMode(hScroll ? JTable.AUTO_RESIZE_OFF : JTable.AUTO_RESIZE_ALL_COLUMNS);
        return t;
    }

    private void setColumnWidths(JTable t, int... widths) {
        for (int i = 0; i < widths.length && i < t.getColumnCount(); i++) {
            TableColumn col = t.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
            col.setMinWidth(widths[i] - 10);
        }
    }

    private static String shortAlgoLabel(SchedulingAlgo a) {
        switch (a) {
            case ROUND_ROBIN: return "RR";
            case PRIORITY:    return "Pri";
            default:          return a.getLabel();
        }
    }

    private static String statusDisplay(SimThread t) {
        if (t.getStatus() == ThreadStatus.TERMINATED) return "Terminated";
        return t.getStatus().getLabel();
    }

    // ------------------------------------------------------------------ Refresh
    public void refresh() {
        List<SimThread> all = engine.allThreads();
        SchedulingAlgo algo = engine.getAlgo();
        String algoShort = shortAlgoLabel(algo);
        int tq  = engine.getTimeQuantum();
        int clk = engine.getClock();

        // PID color renderer
        DefaultTableCellRenderer pidRend = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setFont(Theme.MONO_BOLD);
                setBackground(row % 2 == 0 ? Theme.BG2 : Theme.BG3);
                if (sel) setBackground(Theme.withAlpha(Theme.ACCENT, 35));
                if (row < all.size()) setForeground(all.get(row).getColor());
                return this;
            }
        };
        pidRend.setOpaque(true);

        DefaultTableCellRenderer statusRend = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setFont(Theme.MONO_BOLD);
                String val = v != null ? v.toString() : "";
                setForeground(Theme.statusColor(val));
                setBackground(row % 2 == 0 ? Theme.BG2 : Theme.BG3);
                if (sel) setBackground(Theme.withAlpha(Theme.ACCENT, 35));
                return this;
            }
        };
        statusRend.setOpaque(true);

        // ---- Detail table ----
        detailModel.setRowCount(0);
        for (SimThread t : all) {
            String sync = "-";
            if (t.getStatus() == ThreadStatus.WAITING)
                sync = t.getSyncStatus() != null && !"-".equals(t.getSyncStatus()) ? t.getSyncStatus() : "Waiting";
            detailModel.addRow(new Object[]{
                t.getPid(), t.getTid(), algoShort,
                t.getBurstTime(), tq, t.getPriority(),
                t.getRemainingTime(), sync,
                t.getMemoryMB() + "MB"
            });
        }
        applyPidStatus(detailTable, all, pidRend, statusRend, -1);

        // ---- Timing table ----
        timingModel.setRowCount(0);
        for (SimThread t : all) {
            timingModel.addRow(new Object[]{
                t.getPid(), t.getTid(),
                statusDisplay(t),
                t.getWaitTime(),
                t.getTurnaroundTime(),
                t.getStartTick() < 0 ? "\u2014" : String.valueOf(t.getStartTick()),
                t.getFinishTick() < 0 ? "\u2014" : String.valueOf(t.getFinishTick())
            });
        }
        applyPidStatus(timingTable, all, pidRend, statusRend, 2);

        // ---- Status table ----
        statusModel.setRowCount(0);
        for (SimThread t : all) {
            String core = t.getAssignedCore() > 0 ? ("C" + t.getAssignedCore()) : "\u2014";
            statusModel.addRow(new Object[]{
                t.getPid(), t.getTid(),
                core,
                t.getKthreadId().isEmpty() ? "\u2014" : t.getKthreadId(),
                statusDisplay(t)
            });
        }
        applyPidStatus(statusTable, all, pidRend, statusRend, 4);

        // ---- Kernel thread map ----
        kthreadModel.setRowCount(0);
        for (KernelThread kt : engine.getKthreads()) {
            kthreadModel.addRow(new Object[]{
                kt.getId(),
                String.join("  →  ", kt.getUserThreadIds()),
                engine.getThreadModel().getLabel()
            });
        }

        // ---- Gantt update ----
        ganttPanel.clear();
        for (SimThread t : all) {
            if (t.getStartTick() >= 0) {
                int end = t.getFinishTick() >= 0 ? t.getFinishTick() : clk;
                String label = t.getPid() + "·" + t.getTid();
                ganttPanel.addEntry(label, t.getColor(), t.getStartTick(), end);
            }
        }
        ganttPanel.repaint();
    }

    private void applyPidStatus(JTable table, List<SimThread> all,
                                 DefaultTableCellRenderer pidRend,
                                 DefaultTableCellRenderer statusRend,
                                 int statusCol) {
        if (table.getColumnCount() > 0) table.getColumnModel().getColumn(0).setCellRenderer(pidRend);
        if (statusCol >= 0 && statusCol < table.getColumnCount())
            table.getColumnModel().getColumn(statusCol).setCellRenderer(statusRend);
    }

    private JPanel wrapPanel(String title, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG2);
        JLabel t = UIUtils.sectionTitle(title);
        t.setBorder(new EmptyBorder(4, 10, 3, 8));
        p.add(t, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }
}
