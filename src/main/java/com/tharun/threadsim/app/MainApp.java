package com.tharun.threadsim.app;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApp extends Application {

    private VBox processArea = new VBox(10);
    private HBox movementArea = new HBox(20);
    private GridPane coreGrid = new GridPane();

    private VBox panelContainer = new VBox();
    private int activePanel = -1;

    private int coreCount = 16;

    @Override
    public void start(Stage stage) {

        String bg = "#0b0b15";
        String panel = "#1a1a2e";
        String border = "#6c63ff";
        String text = "#e6f1ff";

        // ================= TOP =================
        ComboBox<String> model = new ComboBox<>();
        model.getItems().addAll("ONE_TO_ONE","MANY_TO_ONE","MANY_TO_MANY");
        model.setValue("ONE_TO_ONE");

        ComboBox<String> algo = new ComboBox<>();
        algo.getItems().addAll("FCFS","SJF","PRIORITY","ROUND_ROBIN");
        algo.setValue("FCFS");

        ComboBox<String> mode = new ComboBox<>();
        mode.getItems().addAll("AUTO","MANUAL");
        mode.setValue("AUTO");

        ComboBox<Integer> cores = new ComboBox<>();
        for(int i=1;i<=16;i++) cores.getItems().add(i);
        cores.setValue(16);

        VBox syncBox = new VBox();
        syncBox.setPrefSize(500,260);
        syncBox.setStyle("-fx-background-color:"+panel+";-fx-border-color:"+border);
        // UPDATED: dynamic core + sync resize
        cores.setOnAction(e -> {
            coreCount = cores.getValue();
            buildCoreGrid(panel,border,text);

            double width = 600 - (coreCount * 10);
            if(width < 250) width = 250;
            syncBox.setPrefWidth(width);
        });

        TextField quantum = new TextField();
        quantum.setPromptText("Quantum");

        HBox top = new HBox(10,
                label("Thread:",text), model,
                label("Algo:",text), algo,
                label("Mode:",text), mode,
                label("Cores:",text), cores,
                quantum,
                new Button("Start"),
                new Button("Step"),
                new Button("Reset")
        );
        top.setPadding(new Insets(10));
        top.setStyle("-fx-background-color:"+panel);

        // ================= LEFT =================
        VBox left = new VBox(15,
                new Button("Add Process"),
                new Button("Add Thread"),
                new Button("Terminate")
        );
        left.setPadding(new Insets(10));
        left.setStyle("-fx-background-color:"+panel);

        // ================= PROCESS PANEL =================
        processArea.setPadding(new Insets(10));
        processArea.setStyle("-fx-background-color:"+panel);

        ScrollPane processScroll = new ScrollPane(processArea);
        processScroll.setPrefHeight(180);

        // ================= THREAD MOVEMENT =================
        movementArea.setPadding(new Insets(0));
        movementArea.setPrefHeight(80);

        // FIXED: real connector (no box)
        movementArea.setStyle("-fx-background-color: transparent;");
        movementArea.setAlignment(Pos.CENTER);

        // ================= SYNC (WIDE) =================


        // ================= CORE CONTAINER =================
        VBox coreContainer = new VBox();
        coreContainer.setPadding(new Insets(10));
        coreContainer.setStyle("-fx-border-color:"+border);

        buildCoreGrid(panel,border,text);
        coreContainer.getChildren().add(coreGrid);

        HBox lower = new HBox(20, syncBox, coreContainer);
        lower.setPadding(new Insets(10));

        // ================= PROCESS (TOP) =================
        VBox processWrapper = new VBox(processScroll);
        processWrapper.setAlignment(Pos.CENTER);
        processWrapper.setPadding(new Insets(10));
        processWrapper.setPrefHeight(200);

        // ================= MOVEMENT CONNECTOR =================
        HBox movementWrapper = new HBox(movementArea);
        movementWrapper.setAlignment(Pos.CENTER);
        movementWrapper.setPadding(new Insets(5));

        // FIXED: no border / no dashed / clean connector
        movementArea.setPrefWidth(800);

        // ================= CORES (BOTTOM) =================
        VBox coreWrapper = new VBox(lower);
        coreWrapper.setAlignment(Pos.CENTER);
        coreWrapper.setPadding(new Insets(10));

        // ================= CENTER STACK =================
        VBox center = new VBox(10,
                processWrapper,
                movementWrapper,
                coreWrapper
        );

        center.setAlignment(Pos.TOP_CENTER);

        // ================= COLLAPSERS =================
        Button c1 = arrow();
        Button c2 = arrow();
        Button c3 = arrow();

        c1.setOnAction(e -> togglePanel(1));
        c2.setOnAction(e -> togglePanel(2));
        c3.setOnAction(e -> togglePanel(3));

        VBox arrows = new VBox(40, c1, c2, c3);
        arrows.setPadding(new Insets(10));

        panelContainer.setPrefWidth(0);

        HBox right = new HBox(arrows, panelContainer);

        // ================= ROOT =================
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setLeft(left);
        root.setCenter(center);
        root.setRight(right);
        root.setStyle("-fx-background-color:"+bg);

        Scene scene = new Scene(root,1400,800);
        stage.setScene(scene);
        stage.setTitle("OS Thread Simulator");
        stage.show();
    }

    // ================= COLLAPSER =================
    private void togglePanel(int id){

        if(activePanel == id){
            panelContainer.setPrefWidth(0);
            panelContainer.getChildren().clear();
            activePanel = -1;
            return;
        }

        activePanel = id;

        panelContainer.setPrefWidth(600);
        panelContainer.getChildren().clear();

        VBox content = new VBox();
        content.setPadding(new Insets(20));

        if(id == 1) content.getChildren().add(new Label("THREAD TABLE"));
        else if(id == 2) content.getChildren().add(new Label("KERNEL THREAD TABLE"));
        else content.getChildren().add(new Label("WAIT / TURNAROUND"));

        panelContainer.getChildren().add(content);
    }

    // ================= CORE GRID =================
    private void buildCoreGrid(String panel,String border,String text){

        coreGrid.getChildren().clear();
        coreGrid.setHgap(20);
        coreGrid.setVgap(20);

        //
        int cols = (int)Math.ceil(Math.sqrt(coreCount));

        for(int i=0;i<coreCount;i++){

            VBox cell = new VBox(5);
            cell.setAlignment(Pos.CENTER);

            Label title = label("Core " + (i+1), text);

            Pane box = new Pane();
            box.setPrefSize(120,60);
            box.setStyle("-fx-background-color:"+panel+";-fx-border-color:"+border);

            cell.getChildren().addAll(title, box);

            coreGrid.add(cell, i % cols, i / cols);
        }
    }

    private Button arrow(){
        Button b = new Button("▶");
        b.setStyle("-fx-font-size:18; -fx-background-color:transparent; -fx-text-fill:white;");
        return b;
    }

    private Label label(String t,String c){
        Label l = new Label(t);
        l.setTextFill(Color.web(c));
        return l;
    }

    public static void main(String[] args){
        launch();
    }
}