package com.ossim.model;

import java.util.ArrayList;
import java.util.List;

public class KernelThread {
    private final String id;
    private final List<String> userThreadIds;
    private final String ownerPid;

    public KernelThread(String id, String ownerPid) {
        this.id = id;
        this.ownerPid = ownerPid;
        this.userThreadIds = new ArrayList<>();
    }

    public void addThread(String tid) { userThreadIds.add(tid); }

    public String getId() { return id; }
    public String getOwnerPid() { return ownerPid; }
    public List<String> getUserThreadIds() { return userThreadIds; }
    public int getThreadCount() { return userThreadIds.size(); }
}
