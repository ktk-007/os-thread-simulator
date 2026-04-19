package com.ossim.model;

public enum SchedulingAlgo {
    FCFS("FCFS"),
    ROUND_ROBIN("Round Robin"),
    SJF("SJF"),
    PRIORITY("Priority");

    private final String label;
    SchedulingAlgo(String label) { this.label = label; }
    public String getLabel() { return label; }

    @Override public String toString() { return label; }
}
