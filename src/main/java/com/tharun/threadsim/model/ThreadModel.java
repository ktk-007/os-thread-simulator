package com.tharun.threadsim.model;

public class ThreadModel {

    private String threadId;
    private int burstTime;
    private int remainingTime;
    private int priority;
    private int memorySize;
    private ThreadState state;

    public ThreadModel(String threadId,
                       int burstTime,
                       int priority,
                       int memorySize) {

        this.threadId = threadId;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.memorySize = memorySize;
        this.state = ThreadState.NEW;
    }

    // Getters

    public String getThreadId() {
        return threadId;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public ThreadState getState() {
        return state;
    }

    // Setters

    public void setState(ThreadState state) {
        this.state = state;
    }

    public void decreaseRemainingTime(int time) {
        this.remainingTime -= time;
        if (this.remainingTime < 0) {
            this.remainingTime = 0;
        }
    }
}