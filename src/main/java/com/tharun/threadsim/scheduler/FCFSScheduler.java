package com.tharun.threadsim.scheduler;

import com.tharun.threadsim.model.ThreadModel;

import java.util.List;

public class FCFSScheduler implements SchedulingAlgorithm {

    @Override
    public ThreadModel selectNextThread(List<ThreadModel> readyQueue) {

        if (readyQueue == null || readyQueue.isEmpty()) {
            return null;
        }

        // FCFS → return first thread in queue
        return readyQueue.get(0);
    }

    @Override
    public String getName() {
        return "First Come First Serve (FCFS)";
    }
}
