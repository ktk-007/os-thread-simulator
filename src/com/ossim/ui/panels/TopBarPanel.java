package com.ossim.ui.panels;

import com.ossim.model.*;
import com.ossim.scheduler.SimEngine;
import com.ossim.ui.Theme;
import com.ossim.ui.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TopBarPanel extends JPanel {

    public interface TopBarListener {
        void onStart();
        void onStep();
        void onConfigChanged();
    }

    private final SimEngine engine;
    private final TopBarListener listener;

    private JComboBox<String> threadModelCombo;
    private JComboBox<String> algoCombo;
    private JRadioButton autoBtn, manualBtn;
    private JRadioButton semBtn, monBtn;
    private JSpinner coreSpinner, tqSpinner, burstSpinner;
    private JLabel tqLabel, tqSpin, burstLabel, burstSpin;
    public  UIUtils.OutlinedButton startBtn;
    public  UIUtils.OutlinedButton stepBtn;
    private boolean started = false;

    public TopBarPanel(SimEngine engine, TopBarListener listener) {
        this.engine   = engine;
        this.listener = listener;
        setBackground(Theme.BG2);
        setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.BORDER));
        // Fixed height removed to prevent cutoff
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // Add some padding
        build();
    }

    private void build() {
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 15));
        left.setOpaque(false);

        // Thread model
        left.add(lbl("Thread model:"));
        threadModelCombo = UIUtils.darkCombo(new String[]{"One-One", "Many-One", "Many-Many"});
        threadModelCombo.setPreferredSize(new Dimension(100, 28));
        threadModelCombo.addActionListener(e -> applyConfig());
        left.add(threadModelCombo);

        sep(left);

        // Scheduling
        left.add(lbl("Scheduling:"));
        algoCombo = UIUtils.darkCombo(new String[]{"FCFS", "Round Robin", "SJF", "Priority"});
        algoCombo.setSelectedIndex(1);
        algoCombo.setPreferredSize(new Dimension(105, 28));
        algoCombo.addActionListener(e -> applyConfig());
        left.add(algoCombo);

        sep(left);

        // Config mode
        left.add(lbl("Config:"));
        autoBtn   = radio("Auto",   true);
        manualBtn = radio("Manual", false);
        ButtonGroup cg = new ButtonGroup(); cg.add(autoBtn); cg.add(manualBtn);
        autoBtn.addActionListener(e -> applyConfig());
        manualBtn.addActionListener(e -> applyConfig());
        JPanel configPanel= new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        configPanel.setOpaque(false);
        configPanel.add(autoBtn);
        configPanel.add(manualBtn);

        left.add(configPanel);


        sep(left);

        // Sync model
        left.add(lbl("Sync:"));
        semBtn = radio("Semaphores", true);
        monBtn = radio("Monitors",   false);
        ButtonGroup sg = new ButtonGroup(); sg.add(semBtn); sg.add(monBtn);
        semBtn.addActionListener(e -> applyConfig());
        monBtn.addActionListener(e -> applyConfig());
        JPanel syncPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        syncPanel.setOpaque(false);
        syncPanel.add(semBtn);
        syncPanel.add(monBtn);

        left.add(syncPanel);

        sep(left);

        // Cores
        left.add(lbl("CPU Cores:"));
        coreSpinner = UIUtils.darkSpinner(4, 1, 16);
        coreSpinner.setPreferredSize(new Dimension(45, 28));
        coreSpinner.addChangeListener(e -> applyConfig());
        left.add(coreSpinner);

        sep(left);

        // Time quantum
        tqLabel = lbl("Time Quantum:");
        left.add(tqLabel);
        tqSpinner = UIUtils.darkSpinner(3, 1, 20);
        tqSpinner.setPreferredSize(new Dimension(45, 28));
        tqSpinner.addChangeListener(e -> applyConfig());
        left.add(tqSpinner);

        // Burst (manual mode only)
        burstLabel = lbl("Burst Time:");
        left.add(burstLabel);
        burstSpinner = UIUtils.darkSpinner(5, 1, 30);
        burstSpinner.setPreferredSize(new Dimension(45, 28));
        burstSpinner.addChangeListener(e -> applyConfig());
        left.add(burstSpinner);

        // Action buttons (right-aligned)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 10));
        right.setOpaque(false);

        startBtn = UIUtils.makeButton("Start", Theme.ACCENT, Theme.ACCENT);
        startBtn.setPreferredSize(new Dimension(80, 34));
        startBtn.addActionListener(e -> { toggleStart(); listener.onStart(); });
        right.add(startBtn);

        stepBtn = UIUtils.makeButton("Step", Theme.BORDER2, Theme.TEXT);
        stepBtn.setPreferredSize(new Dimension(80, 34));
        stepBtn.addActionListener(e -> listener.onStep());
        right.add(stepBtn);

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        applyConfig();
        updateVisibility();
    }



    private void toggleStart() {
        started = !started;
        if (started) {
            startBtn.setText("Pause");
            startBtn.setAccent(Theme.GREEN, new Color(0x0A0A0F));
        } else {
            startBtn.setText("Start");
            startBtn.setAccent(Theme.ACCENT, Theme.ACCENT);
        }
        repaint();
    }

    public void resetStartState() {
        started = false;
        startBtn.setText("Start");
        startBtn.setAccent(Theme.ACCENT, Theme.ACCENT);
        repaint();
    }

    public boolean isStarted() { return started; }

    private void applyConfig() {
        switch (threadModelCombo.getSelectedIndex()) {
            case 0: engine.setThreadModel(ThreadModel.ONE_ONE);   break;
            case 1: engine.setThreadModel(ThreadModel.MANY_ONE);  break;
            case 2: engine.setThreadModel(ThreadModel.MANY_MANY); break;
        }
        switch (algoCombo.getSelectedIndex()) {
            case 0: engine.setAlgo(SchedulingAlgo.FCFS);         break;
            case 1: engine.setAlgo(SchedulingAlgo.ROUND_ROBIN);  break;
            case 2: engine.setAlgo(SchedulingAlgo.SJF);          break;
            case 3: engine.setAlgo(SchedulingAlgo.PRIORITY);     break;
        }
        engine.setAutoConfig(autoBtn.isSelected());
        engine.setUseSemaphores(semBtn.isSelected());
        engine.setCoreCount((Integer) coreSpinner.getValue());
        engine.setTimeQuantum((Integer) tqSpinner.getValue());
        engine.setDefaultBurst((Integer) burstSpinner.getValue());
        updateVisibility();
        listener.onConfigChanged();
    }

    private void updateVisibility() {
        boolean isRR       = algoCombo.getSelectedIndex() == 1;
        boolean isPriority = algoCombo.getSelectedIndex() == 3;
        boolean isManual   = manualBtn.isSelected();
        tqLabel.setVisible(isRR);    tqSpinner.setVisible(isRR);
        // Burst time not relevant for Priority scheduling
        boolean showBurst  = isManual && !isPriority;
        burstLabel.setVisible(showBurst); burstSpinner.setVisible(showBurst);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.LABEL);
        l.setForeground(Theme.TEXT2);
        return l;
    }

    private JRadioButton radio(String t, boolean sel) {
        JRadioButton r = new JRadioButton(t, sel);
        r.setFont(Theme.LABEL);
        r.setForeground(Theme.TEXT2);
        r.setBackground(Theme.BG2);
        r.setFocusPainted(false);
        return r;
    }

    private void sep(JPanel into) {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 24));
        s.setForeground(Theme.BORDER2);
        into.add(s);
    }
}
