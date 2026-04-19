package com.ossim.model;

import java.awt.Color;

public class MoveEvent {
    public final String pid;
    public final String tid;
    public final String from;
    public final String to;
    public final Color color;
    public final int tick;

    public MoveEvent(String pid, String tid, String from, String to, Color color, int tick) {
        this.pid = pid; this.tid = tid; this.from = from;
        this.to = to; this.color = color; this.tick = tick;
    }

    @Override
    public String toString() {
        return pid + "·" + tid + "  →  " + to + "  [t=" + tick + "]";
    }
}
