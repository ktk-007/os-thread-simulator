package com.tharun.threadsim.scheduler;

import com.tharun.threadsim.model.ThreadModel;
import java.util.List;

public interface Scheduler {
    ThreadModel getNext(List<ThreadModel> threads, int currentTime);
}