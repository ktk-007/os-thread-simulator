package com.tharun.threadsim.model;

public class ThreadModel {

    private String threadId;
    private String processId;

    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    private int priority;

    private int startTime = -1;
    private int completionTime = -1;

    private ThreadState state;

    public ThreadModel(String threadId, String processId,
                       int arrivalTime, int burstTime, int priority) {

        this.threadId = threadId;
        this.processId = processId;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.state = ThreadState.READY;
    }

    public void execute(int currentTime) {
        if (startTime == -1) startTime = currentTime;

        remainingTime--;

        if (remainingTime <= 0) {
            remainingTime = 0;
            completionTime = currentTime + 1;
            state = ThreadState.TERMINATED;
        } else {
            state = ThreadState.RUNNING;
        }
    }

    public int getWaitingTime() {
        return completionTime == -1 ? -1 : (completionTime - arrivalTime - burstTime);
    }

    public int getTurnaroundTime() {
        return completionTime == -1 ? -1 : (completionTime - arrivalTime);
    }

    // Getters
    public String getThreadId() { return threadId; }
    public String getProcessId() { return processId; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getPriority() { return priority; }
    public ThreadState getState() { return state; }

    public void setState(ThreadState state) { this.state = state; }
}