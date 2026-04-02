package com.tharun.threadsim.sync;

import com.tharun.threadsim.model.ThreadModel;

import java.util.*;

public class Semaphore {

    private int value;
    private Queue<ThreadModel> queue = new LinkedList<>();

    public Semaphore(int value) {
        this.value = value;
    }

    public void wait(ThreadModel t) {
        if (value > 0) {
            value--;
        } else {
            queue.add(t);
            t.setState(com.tharun.threadsim.model.ThreadState.WAITING);
        }
    }

    public void signal() {
        if (!queue.isEmpty()) {
            ThreadModel t = queue.poll();
            t.setState(com.tharun.threadsim.model.ThreadState.READY);
        } else {
            value++;
        }
    }
}