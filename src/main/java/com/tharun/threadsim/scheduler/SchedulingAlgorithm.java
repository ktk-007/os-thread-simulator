package com.tharun.threadsim.scheduler;

import com.tharun.threadsim.model.ThreadModel;

import java.util.List;

public interface SchedulingAlgorithm {

    ThreadModel selectNextThread(List<ThreadModel> readyQueue);

    String getName();
}
