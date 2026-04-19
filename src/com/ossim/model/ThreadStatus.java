package com.ossim.model;

public enum ThreadStatus {
    READY, RUNNING, WAITING, DONE, TERMINATED;

    public String getLabel() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
