package com.tharun.threadsim.scheduler;

import com.tharun.threadsim.model.*;

import java.util.*;

public class RoundRobinScheduler implements Scheduler {

    private Queue<ThreadModel> queue = new LinkedList<>();
    private int quantum = 2;
    private Map<ThreadModel, Integer> executionTime = new HashMap<>();

    public void setQuantum(int q) {
        this.quantum = q;
    }

    @Override
    public ThreadModel getNext(List<ThreadModel> threads, int currentTime) {

        for (ThreadModel t : threads) {
            if (t.getState() == ThreadState.READY && !queue.contains(t)) {
                queue.add(t);
                executionTime.put(t, 0);
            }
        }

        return queue.poll();
    }

    public void onExecute(ThreadModel t) {

        executionTime.put(t, executionTime.getOrDefault(t, 0) + 1);

        if (executionTime.get(t) >= quantum && t.getState() != ThreadState.TERMINATED) {
            t.setState(ThreadState.READY);
            queue.add(t);
            executionTime.put(t, 0);
        }
    }
}