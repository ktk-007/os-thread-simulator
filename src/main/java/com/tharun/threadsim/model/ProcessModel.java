package com.tharun.threadsim.model;

import java.util.*;

public class ProcessModel {

    private String processId;
    private List<ThreadModel> threads = new ArrayList<>();

    public ProcessModel(String processId) {
        this.processId = processId;
    }

    public void addThread(ThreadModel t) {
        threads.add(t);
    }

    public String getProcessId() { return processId; }
    public List<ThreadModel> getThreads() { return threads; }
}