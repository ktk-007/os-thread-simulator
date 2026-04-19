package com.ossim.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SimProcess {
    private static int pidCounter = 1;
    private static int colorIdx   = 0;

    private static final Color[] PALETTE = {
        new Color(0x7C3AED), new Color(0x059669), new Color(0xD97706),
        new Color(0xDC2626), new Color(0x2563EB), new Color(0xDB2777),
        new Color(0x0891B2), new Color(0x65A30D), new Color(0xEA580C),
        new Color(0x9333EA), new Color(0x0D9488), new Color(0xCA8A04)
    };

    private final String pid;
    private final Color  color;
    private final List<SimThread> threads;

    public SimProcess(SchedulingAlgo algo, int defaultBurst, boolean autoConfig) {
        this.pid     = "P" + (pidCounter++);
        this.color   = PALETTE[colorIdx % PALETTE.length];
        colorIdx++;
        this.threads = new ArrayList<>();
        // Start with 2 threads by default when process is added
        addThread(algo, defaultBurst, autoConfig);
        addThread(algo, defaultBurst, autoConfig);
    }

    public SimThread addThread(SchedulingAlgo algo, int defaultBurst, boolean autoConfig) {
        int burst = autoConfig ? (3 + (int)(Math.random() * 10)) : defaultBurst;
        SimThread t = new SimThread(pid, color, burst, algo);
        threads.add(t);
        return t;
    }

    public static void resetCounter() { pidCounter = 1; colorIdx = 0; }

    public String getPid()               { return pid; }
    public Color  getColor()             { return color; }
    public List<SimThread> getThreads()  { return threads; }

    public int activeThreadCount() {
        return (int) threads.stream()
            .filter(t -> t.getStatus() != ThreadStatus.DONE && t.getStatus() != ThreadStatus.TERMINATED)
            .count();
    }
}
