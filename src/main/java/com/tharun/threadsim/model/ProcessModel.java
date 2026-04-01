package com.tharun.threadsim.model;

import java.util.ArrayList;
import java.util.List;

public class ProcessModel {

    private String processId;
    private List<ThreadModel> threads;

    public ProcessModel(String processId) {
        this.processId = processId;
        this.threads = new ArrayList<>();
    }

    public String getProcessId() {
        return processId;
    }

    public List<ThreadModel> getThreads() {
        return threads;
    }

    public void addThread(ThreadModel thread) {
        threads.add(thread);
    }
}
