package com.ossim.ui;

import com.ossim.model.MoveEvent;
import com.ossim.scheduler.SimEngine;
import com.ossim.ui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public class MainWindow extends JFrame {

    private final SimEngine engine = new SimEngine();

    private TopBarPanel     topBar;
    private ProcessPanel    processPanel;
    private MovementBar     movementBar;
    private SyncPanel       syncPanel;
    private CoresPanel      coresPanel;
    private RightTablesPanel rightTables;
    private StatusBar       statusBar;

    private JSplitPane      mainSplit;
    private JSplitPane      leftSplit;

    private Timer autoTimer;

    // Slower tick: 1000ms auto, 800ms step animation
    private static final int AUTO_DELAY = 1000;

    public MainWindow() {
        super("OS Thread Simulator");
        UIUtils.applyDarkLook();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1460, 880);
        setMinimumSize(new Dimension(1240, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        build();
        engine.init();   // empty — no default processes
        refresh();
    }

    private void build() {
        setLayout(new BorderLayout());

        // ---- TOP BAR ----
        topBar = new TopBarPanel(engine, new TopBarPanel.TopBarListener() {
            @Override public void onStart()         { handleStartPause(); }
            @Override public void onStep()          { doStep(); }
            @Override public void onReset()         { doReset(); }
            @Override public void onConfigChanged() { refresh(); }
        });
        add(topBar, BorderLayout.NORTH);

        // ---- STATUS BAR ----
        statusBar = new StatusBar(engine);
        add(statusBar, BorderLayout.SOUTH);

        // ---- LEFT PANEL ----
        processPanel = new ProcessPanel(engine);
        processPanel.setListener(new ProcessPanel.ProcessPanelListener() {
            @Override public void onAddProcess()          { engine.addProcess(); refresh(); }
            @Override public void onAddThread(String pid) { engine.addThread(pid); refresh(); }
            @Override public void onTerminate(String pid) { engine.terminateProcess(pid); refresh(); }
        });

        // ---- CENTER ----
        movementBar = new MovementBar();
        syncPanel   = new SyncPanel(engine);
        coresPanel  = new CoresPanel(engine);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Theme.BG);
        centerPanel.add(movementBar, BorderLayout.NORTH);

        JPanel syncAndCores = new JPanel(new BorderLayout());
        syncAndCores.setBackground(Theme.BG);
        syncAndCores.add(syncPanel,  BorderLayout.NORTH);
        syncAndCores.add(coresPanel, BorderLayout.CENTER);
        centerPanel.add(syncAndCores, BorderLayout.CENTER);

        // ---- RIGHT PANEL (reference width ~320–360px) ----
        rightTables = new RightTablesPanel(engine);
        rightTables.setMinimumSize(new Dimension(300, 0));
        rightTables.setPreferredSize(new Dimension(340, 0));

        // ---- SPLITS ----
        leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, processPanel, centerPanel);
        leftSplit.setDividerSize(4);
        leftSplit.setDividerLocation(250);
        leftSplit.setBackground(Theme.BG);

        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, rightTables);
        mainSplit.setDividerSize(4);
        mainSplit.setResizeWeight(1.0);
        mainSplit.setBackground(Theme.BG);

        add(mainSplit, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int w = mainSplit.getWidth();
                if (w <= 0) return;
                int wantRight = rightTables.getPreferredSize().width;
                int maxRight = Math.max(280, w - 400);
                int rightW = Math.min(wantRight, maxRight);
                int loc = w - rightW - mainSplit.getDividerSize();
                if (loc > 200 && loc < w - 40) mainSplit.setDividerLocation(loc);
            }
        });

        // ---- AUTO TIMER (slower) ----
        autoTimer = new Timer(AUTO_DELAY, e -> doStep());
        autoTimer.setRepeats(true);

        // ---- ENGINE LISTENER ----
        engine.setListener(new SimEngine.SimListener() {
            @Override public void onTick(int clock, List<MoveEvent> events) {
                SwingUtilities.invokeLater(() -> {
                    movementBar.addEvents(events);
                    refresh();
                });
            }
            @Override public void onFinished() {
                SwingUtilities.invokeLater(() -> {
                    if (autoTimer.isRunning()) {
                        autoTimer.stop();
                        topBar.resetStartState();
                    }
                    JOptionPane.showMessageDialog(MainWindow.this,
                        "<html><b>Simulation Complete</b><br>All threads have finished execution.<br>Clock: "
                            + engine.getClock() + " ticks</html>",
                        "Simulation Complete", JOptionPane.INFORMATION_MESSAGE);
                });
            }
        });
    }

    private void handleStartPause() {
        if (topBar.isStarted()) {
            autoTimer.start();
        } else {
            autoTimer.stop();
        }
    }

    private void doStep() {
        engine.tick();
        // Engine listener calls refresh via onTick
    }

    private void doReset() {
        autoTimer.stop();
        movementBar.clear();
        engine.init();
        refresh();
    }

    private void refresh() {
        if (processPanel != null) processPanel.refresh();
        if (syncPanel != null) syncPanel.refresh();
        if (coresPanel != null) coresPanel.refresh();
        if (rightTables != null) rightTables.refresh();
        if (statusBar != null) statusBar.refresh();
    }
}
