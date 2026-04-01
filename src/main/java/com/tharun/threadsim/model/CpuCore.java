package com.tharun.threadsim.model;

public class CpuCore {

    private String coreId;
    private ThreadModel currentThread;

    public CpuCore(String coreId) {
        this.coreId = coreId;
    }

    public String getCoreId() {
        return coreId;
    }

    public ThreadModel getCurrentThread() {
        return currentThread;
    }

    public void assignThread(ThreadModel thread) {
        this.currentThread = thread;

        if (thread != null) {
            thread.setState(ThreadState.RUNNING);
        }
    }

    public void releaseThread() {
        if (currentThread != null) {
            currentThread.setState(ThreadState.READY);
        }
        this.currentThread = null;
    }

    public boolean isIdle() {
        return currentThread == null;
    }
}