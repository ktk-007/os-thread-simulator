package com.tharun.threadsim.ui;

import com.tharun.threadsim.engine.SimulationEngine;
import com.tharun.threadsim.model.CpuCore;
import com.tharun.threadsim.model.ThreadModel;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class SimulationView extends VBox {

    private SimulationEngine engine;

    private HBox readyQueueBox;
    private VBox cpuCoresBox;

    public SimulationView(SimulationEngine engine) {
        this.engine = engine;

        setSpacing(20);
        setStyle("-fx-padding: 20;");

        Label readyLabel = new Label("Ready Queue:");
        readyQueueBox = new HBox(10);

        Label coresLabel = new Label("CPU Cores:");
        cpuCoresBox = new VBox(10);

        getChildren().addAll(readyLabel, readyQueueBox, coresLabel, cpuCoresBox);

        refresh();
    }

    public void refresh() {
        refreshReadyQueue();
        refreshCores();
    }

    private void refreshReadyQueue() {
        readyQueueBox.getChildren().clear();

        for (ThreadModel thread : engine.getReadyQueue()) {
            Label threadLabel = new Label(thread.getThreadId());
            threadLabel.setStyle("-fx-border-color: black; -fx-padding: 5;");
            readyQueueBox.getChildren().add(threadLabel);
        }
    }

    private void refreshCores() {
        cpuCoresBox.getChildren().clear();

        for (CpuCore core : engine.getCpuCores()) {
            String text = core.getCoreId() + ": ";

            if (core.getCurrentThread() != null) {
                text += core.getCurrentThread().getThreadId();
            } else {
                text += "Idle";
            }

            Label coreLabel = new Label(text);
            coreLabel.setStyle("-fx-border-color: blue; -fx-padding: 8;");

            cpuCoresBox.getChildren().add(coreLabel);
        }
    }
}
