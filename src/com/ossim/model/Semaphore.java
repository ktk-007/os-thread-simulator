package com.ossim.model;

import java.util.ArrayDeque;
import java.util.Queue;

public class Semaphore {
    private final String name;
    private int value;
    private String heldBy;
    private final Queue<String> waitQueue;

    public Semaphore(String name, int initialValue) {
        this.name = name;
        this.value = initialValue;
        this.heldBy = null;
        this.waitQueue = new ArrayDeque<>();
    }

    public boolean tryAcquire(String tid) {
        if (value > 0) {
            value--;
            heldBy = tid;
            return true;
        }
        waitQueue.add(tid);
        return false;
    }

    public String release() {
        heldBy = null;
        String next = waitQueue.poll();
        if (next == null) value++;
        else heldBy = next;
        return next; // returns waiting tid that now gets it, or null
    }

    public String getName() { return name; }
    public int getValue() { return value; }
    public String getHeldBy() { return heldBy; }
    public int getWaitingCount() { return waitQueue.size(); }
    public boolean isLocked() { return value <= 0; }
}
