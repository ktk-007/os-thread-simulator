package com.tharun.threadsim.model;

public class CpuCore {

    private int id;
    private ThreadModel runningThread;

    public CpuCore(int id) {
        this.id = id;
    }

    public void assign(ThreadModel t) {
        this.runningThread = t;

        if (t != null) {
            t.setState(ThreadState.RUNNING);
        }
    }

    public void execute(int time) {
        if (runningThread != null) {
            runningThread.execute(time);

            if (runningThread.getState() == ThreadState.TERMINATED) {
                runningThread = null;
            }
        }
    }

    // ✅ ADD THESE METHODS (YOUR ERROR FIX)

    public int getCoreId() {
        return id;
    }

    public ThreadModel getCurrentThread() {
        return runningThread;
    }
}