package com.tharun.threadsim.app;
import com.tharun.threadsim.engine.SimulationEngine;
import com.tharun.threadsim.model.ProcessModel;
import com.tharun.threadsim.model.ThreadModel;
import com.tharun.threadsim.ui.SimulationView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

public class MainApp extends Application {
    private SimulationEngine engine;
    private Timeline timeline;
    private boolean autoMode = false;
    @Override
    public void start(Stage stage) {
        int[] processCounter = {1};
        engine = new SimulationEngine();
        engine.initialize();
        SimulationView simulationView = new SimulationView(engine);
        Button addProcessButton = new Button("Add Process");

        ListView<String> processListView = new ListView<>();
        processListView.setPrefHeight(180);
        processListView.setMinHeight(150);
        VBox processBox = new VBox(10, addProcessButton, processListView);
        VBox.setVgrow(processListView, Priority.ALWAYS);
        addProcessButton.setOnAction(e -> {

            if (engine.getProcessCount() >= 8) {
                System.out.println("Maximum 8 processes allowed.");
                return;
            }

            String processId = "P" + processCounter[0]++;

            ProcessModel process = new ProcessModel(processId);
            engine.addProcess(process);

            processListView.getItems().add(processId);
        });
        // ---- Thread Input ----
        TextField threadIdField = new TextField();
        threadIdField.setPromptText("Thread ID");

        TextField burstField = new TextField();
        burstField.setPromptText("Burst Time");

        TextField priorityField = new TextField();
        priorityField.setPromptText("Priority");

        TextField memoryField = new TextField();
        memoryField.setPromptText("Memory");

        Button addThreadButton = new Button("Add Thread");

        HBox threadInputBox = new HBox(10,
                threadIdField,
                burstField,
                priorityField,
                memoryField,
                addThreadButton
        );
        addThreadButton.setOnAction(e -> {

            String selectedProcessId = processListView.getSelectionModel().getSelectedItem();

            if (selectedProcessId == null) {
                System.out.println("Select a process first.");
                return;
            }

            ProcessModel selectedProcess = null;

            for (ProcessModel p : engine.getProcesses()) {
                if (p.getProcessId().equals(selectedProcessId)) {
                    selectedProcess = p;
                    break;
                }
            }

            if (selectedProcess == null) return;

            if (selectedProcess.getThreads().size() >= 8) {
                System.out.println("Maximum 8 threads per process allowed.");
                return;
            }

            // ---- AUTO GENERATION ----
            int threadNumber = selectedProcess.getThreads().size() + 1;
            String threadId = selectedProcessId + "-T" + threadNumber;

            int burst = 2 + (int)(Math.random() * 9);       // 2–10
            int priority = 1 + (int)(Math.random() * 5);    // 1–5
            int memory = 10 + (int)(Math.random() * 91);    // 10–100

            ThreadModel thread = new ThreadModel(threadId, burst, priority, memory);

            selectedProcess.addThread(thread);
            engine.addThread(thread);

            simulationView.refresh();
        });
        // ---- Mode Selection ----
        RadioButton stepModeButton = new RadioButton("Step Mode");
        RadioButton autoModeButton = new RadioButton("Auto Mode");

        ToggleGroup modeGroup = new ToggleGroup();
        stepModeButton.setToggleGroup(modeGroup);
        autoModeButton.setToggleGroup(modeGroup);

        stepModeButton.setSelected(true);  // default

        stepModeButton.setOnAction(e -> autoMode = false);
        autoModeButton.setOnAction(e -> autoMode = true);

        // ---- Control Buttons ----
        Button startButton = new Button("Start");
        Button stepButton = new Button("Step");
        Button resetButton = new Button("Reset");

        // ---- Timeline (Auto Mode) ----
        timeline = new Timeline(
                new KeyFrame(Duration.millis(1050), e -> {

                    if (!engine.isSimulationRunning()) {
                        return;
                    }

                    engine.runOneStep();
                    simulationView.refresh();

                    if (engine.isSimulationFinished()) {
                        timeline.stop();
                        engine.stopSimulation();
                        System.out.println("Simulation Finished.");
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);

        // ---- Button Actions ----

        startButton.setOnAction(e -> {
            engine.startSimulation();

            if (autoMode) {
                timeline.play();
            }
        });

        stepButton.setOnAction(e -> {

            if (!engine.isSimulationRunning()) {
                engine.startSimulation();
            }

            engine.runOneStep();
            simulationView.refresh();
        });

        resetButton.setOnAction(e -> {
            timeline.stop();
            engine.resetSimulation();
            simulationView.refresh();
        });

        // ---- Layout ----
        HBox modeBox = new HBox(15, stepModeButton, autoModeButton);
        HBox controlBox = new HBox(15, startButton, stepButton, resetButton);

        VBox root = new VBox(20,
                processBox,
                threadInputBox,
                modeBox,
                controlBox,
                simulationView
        );
        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 700, 400);

        stage.setTitle("Multithreading & Synchronization Simulator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}