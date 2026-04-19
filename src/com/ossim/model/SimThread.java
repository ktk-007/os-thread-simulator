package com.ossim.model;

import java.awt.Color;

public class SimThread {
    private static int tidCounter = 1;

    private final String tid;
    private final String pid;
    private final Color color;

    private int burstTime;
    private int remainingTime;
    private int priority;
    private ThreadStatus status;
    private int assignedCore;      // 0 = none
    private String kthreadId;
    private int waitTime;
    private int turnaroundTime;
    private int startTick;
    private int finishTick;
    private String syncStatus;
    private int memoryMB;
    private int timeQuantumUsed;
    private SchedulingAlgo algo;

    public SimThread(String pid, Color color, int burstTime, SchedulingAlgo algo) {
        this.tid = "T" + (tidCounter++);
        this.pid = pid;
        this.color = color;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = (int)(Math.random() * 5) + 1;
        this.status = ThreadStatus.READY;
        this.assignedCore = 0;
        this.kthreadId = "";
        this.waitTime = 0;
        this.turnaroundTime = 0;
        this.startTick = -1;
        this.finishTick = -1;
        this.syncStatus = "-";
        this.memoryMB = 64 + (int)(Math.random() * 192);
        this.timeQuantumUsed = 0;
        this.algo = algo;
    }

    public static void resetCounter() { tidCounter = 1; }

    // Getters
    public String getTid() { return tid; }
    public String getPid() { return pid; }
    public Color getColor() { return color; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getPriority() { return priority; }
    public ThreadStatus getStatus() { return status; }
    public int getAssignedCore() { return assignedCore; }
    public String getKthreadId() { return kthreadId; }
    public int getWaitTime() { return waitTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getStartTick() { return startTick; }
    public int getFinishTick() { return finishTick; }
    public String getSyncStatus() { return syncStatus; }
    public int getMemoryMB() { return memoryMB; }
    public int getTimeQuantumUsed() { return timeQuantumUsed; }
    public SchedulingAlgo getAlgo() { return algo; }

    // Setters
    public void setRemainingTime(int v) { remainingTime = v; }
    public void setStatus(ThreadStatus s) { status = s; }
    public void setAssignedCore(int c) { assignedCore = c; }
    public void setKthreadId(String k) { kthreadId = k; }
    public void incrementWaitTime() { waitTime++; }
    public void incrementTurnaround() { turnaroundTime++; }
    public void setStartTick(int t) { if (startTick < 0) startTick = t; }
    public void setFinishTick(int t) { finishTick = t; }
    public void setSyncStatus(String s) { syncStatus = s; }
    public void incrementQuantum() { timeQuantumUsed++; }
    public void resetQuantum() { timeQuantumUsed = 0; }
    public void setAlgo(SchedulingAlgo a) { algo = a; }
    public void setBurstTime(int b) { burstTime = b; remainingTime = b; }

    public double getCompletionPercent() {
        if (burstTime == 0) return 100.0;
        return 100.0 * (burstTime - remainingTime) / burstTime;
    }
}
