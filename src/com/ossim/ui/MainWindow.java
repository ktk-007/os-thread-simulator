package com.ossim.ui;

import com.ossim.model.MoveEvent;
import com.ossim.model.SimThread;
import com.ossim.scheduler.SimEngine;
import com.ossim.ui.panels.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import javax.swing.*;




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

    private javax.swing.Timer autoTimer;

    
    private static final int AUTO_DELAY = 1000;

    public MainWindow() {
        super("RTOS ThreadVision (Thread Simulator)");
        UIUtils.applyDarkLook();
        setUndecorated(true);          
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1460, 900);
        setMinimumSize(new Dimension(1240, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        build();
        engine.init();   // empty — no default processes
        refresh();
    }

    private void build() {
        setLayout(new BorderLayout());

       
        CustomTitleBar titleBar = new CustomTitleBar(this);

     
        topBar = new TopBarPanel(engine, new TopBarPanel.TopBarListener() {
            @Override public void onStart()         { handleStartPause(); }
            @Override public void onStep()          { doStep(); }
            @Override public void onConfigChanged() { refresh(); }
        });

       
        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.setBackground(Theme.BG);
        northWrapper.add(titleBar, BorderLayout.NORTH);
        northWrapper.add(topBar,   BorderLayout.CENTER);
        add(northWrapper, BorderLayout.NORTH);

       
        statusBar = new StatusBar(engine);
        add(statusBar, BorderLayout.SOUTH);

       
        processPanel = new ProcessPanel(engine);
        processPanel.setListener(new ProcessPanel.ProcessPanelListener() {
            @Override public void onAddProcess()          { engine.addProcess(); refresh(); }
            @Override public void onAddThread(String pid) { engine.addThread(pid); refresh(); }
            @Override public void onTerminate(String pid) { engine.terminateProcess(pid); refresh(); }
            @Override public void onReset()               { doReset(); }
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

        
        rightTables = new RightTablesPanel(engine);
        rightTables.setMinimumSize(new Dimension(300, 0));
        rightTables.setPreferredSize(new Dimension(500, 0));

        
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
                int maxRight = Math.max(480, w - 400);
                int rightW = Math.min(wantRight, maxRight);
                int loc = w - rightW - mainSplit.getDividerSize();
                if (loc > 200 && loc < w - 40) mainSplit.setDividerLocation(loc);
            }
        });

        
        autoTimer = new javax.swing.Timer(AUTO_DELAY, e -> doStep());
        autoTimer.setRepeats(true);

        
        engine.setListener(new SimEngine.SimListener() {
            @Override public void onTick(int clock, java.util.List<MoveEvent> events) {
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


                    
                    java.util.List<MoveEvent> moveHistory = new ArrayList<>(engine.getMoveHistory());
                    Map<String, String> finalCoreIds = extractFinalCoreIds(engine);
                    topBar.setExportSnapshot(moveHistory, finalCoreIds, engine);
                    topBar.getExportButton().setEnabled(true);
                    JOptionPane.showMessageDialog(MainWindow.this,
                        "<html><b>Simulation Complete</b><br>All threads have finished execution.<br>Clock: "
                            + engine.getClock() + " ticks</html>",
                        "Simulation Complete", JOptionPane.INFORMATION_MESSAGE);
                });
            }


            
            private Map<String, String> extractFinalCoreIds(SimEngine engine) {
                Map<String, String> finalCores = new HashMap<>();
                
                for (SimThread t : engine.allThreads()) {
                    String core = String.valueOf(t.getAssignedCore());
                    finalCores.put(t.getTid(), core != null ? core : "");
                }


                
                
                java.util.List<MoveEvent> history = engine.getMoveHistory();
                if (history != null && !history.isEmpty()) {
                    java.util.Set<String> knownStates = new java.util.HashSet<>(java.util.Arrays.asList(
                        "Ready", "Running", "Waiting", "Blocked", "Done", "Created", "Terminated"
                    ));
                    
                    for (MoveEvent event : history) {
                        String tid = event.tid;
                        if ((finalCores.get(tid) == null || finalCores.get(tid).isEmpty())) {
                            
                            if (!knownStates.contains(event.to) && event.to != null && !event.to.isEmpty()) {
                                finalCores.put(tid, event.to);
                            }
                           
                            else if (!knownStates.contains(event.from) && event.from != null && !event.from.isEmpty()) {
                                finalCores.put(tid, event.from);
                            }
                        }
                    }
                }
                
                return finalCores;
            }
        });
    }

    private void handleStartPause() {
        if (topBar.isStarted()) {
            topBar.getExportButton().setEnabled(false);
            autoTimer.start();
        } else {
            autoTimer.stop();
        }
    }

    private void doStep() {
        engine.tick();
    }

    private void doReset() {
        autoTimer.stop();
        movementBar.clear();
        topBar.getExportButton().setEnabled(false);
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
