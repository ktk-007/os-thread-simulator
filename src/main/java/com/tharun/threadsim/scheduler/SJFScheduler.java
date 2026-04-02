package com.tharun.threadsim.scheduler;

import com.tharun.threadsim.model.*;

import java.util.Comparator;
import java.util.List;

public class SJFScheduler implements Scheduler {

    @Override
    public ThreadModel getNext(List<ThreadModel> threads, int currentTime) {

        return threads.stream()
                .filter(t -> t.getState() == ThreadState.READY)
                .min(Comparator.comparingInt(ThreadModel::getBurstTime))
                .orElse(null);
    }
}