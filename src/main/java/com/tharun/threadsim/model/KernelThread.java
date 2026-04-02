package com.tharun.threadsim.model;

public class KernelThread {

    private String id;
    private ThreadModel assignedThread;

    public KernelThread(String id) {
        this.id = id;
    }

    public void assign(ThreadModel t) {
        this.assignedThread = t;
    }

    public ThreadModel getAssignedThread() {
        return assignedThread;
    }

    public String getId() { return id; }
}