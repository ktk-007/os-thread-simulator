package com.tharun.threadsim.engine;

import com.tharun.threadsim.model.*;
import com.tharun.threadsim.scheduler.*;

import java.util.*;

public class SimulationEngine {

    private List<ProcessModel> processes = new ArrayList<>();
    private List<ThreadModel> threads = new ArrayList<>();
    private List<CpuCore> cores = new ArrayList<>();
    private ThreadCreationMode creationMode = ThreadCreationMode.AUTO;
    private Scheduler scheduler = new FCFSScheduler();
    private RoundRobinScheduler rrScheduler = new RoundRobinScheduler();

    private ThreadMapper mapper;
    private ThreadMappingType mappingType = ThreadMappingType.ONE_TO_ONE;

    private int time = 0;
    public void setCreationMode(ThreadCreationMode mode) {
        this.creationMode = mode;
    }

    public void addAutoThread() {

        if (selectedProcess == null) return;

        int n = selectedProcess.getThreads().size() + 1;

        ThreadModel t = new ThreadModel(
                selectedProcess.getProcessId() + "-T" + n,
                selectedProcess.getProcessId(),
                time,
                2 + (int)(Math.random()*8),
                1 + (int)(Math.random()*5)
        );

        selectedProcess.addThread(t);
        threads.add(t);
    }

    public void addManualThread(int burst, int priority) {

        if (selectedProcess == null) return;

        int n = selectedProcess.getThreads().size() + 1;

        ThreadModel t = new ThreadModel(
                selectedProcess.getProcessId() + "-T" + n,
                selectedProcess.getProcessId(),
                time,
                burst,
                priority
        );

        selectedProcess.addThread(t);
        threads.add(t);
    }
    public void setCoreCount(int count) {

        cores.clear();

        for (int i = 1; i <= count; i++) {
            cores.add(new CpuCore(i));
        }

        mapper = new ThreadMapper(count);
    }
    public void addThread(int burst, int priority) {

        if (creationMode == ThreadCreationMode.AUTO) {
            addAutoThread();
        } else {
            addManualThread(burst, priority);
        }
    }
    //
    private ProcessModel selectedProcess = null;

    // control
    private boolean isRunning = false;
    private Thread simulationThread;

    public SimulationEngine(int coreCount) {
        for (int i = 1; i <= coreCount; i++) {
            cores.add(new CpuCore(i));
        }
        mapper = new ThreadMapper(coreCount);
    }

    // ---------------- PROCESS ----------------
    public void addProcess() {
        ProcessModel p = new ProcessModel("P" + (processes.size() + 1));
        processes.add(p);


    }

    public void selectProcess(int index) {
        if (index >= 0 && index < processes.size()) {
            selectedProcess = processes.get(index);
        }
    }

    public ProcessModel getSelectedProcess() {
        return selectedProcess;
    }

    // ---------------- THREAD ----------------
    public void addThreadToSelectedProcess() {

        if (selectedProcess == null) return;

        int n = selectedProcess.getThreads().size() + 1;

        ThreadModel t = new ThreadModel(
                selectedProcess.getProcessId() + "-T" + n,
                selectedProcess.getProcessId(),
                time,
                2 + (int)(Math.random()*8),
                1 + (int)(Math.random()*5)
        );

        selectedProcess.addThread(t);
        threads.add(t);
    }
    public boolean hasSelectedProcess() {
        return selectedProcess != null;
    }
    // ---------------- CONFIG ----------------
    public void setAlgorithm(SchedulingAlgorithm algo) {
        switch (algo) {
            case FCFS -> scheduler = new FCFSScheduler();
            case SJF -> scheduler = new SJFScheduler();
            case PRIORITY -> scheduler = new PriorityScheduler();
            case ROUND_ROBIN -> scheduler = rrScheduler;
        }
    }

    public void setQuantum(int q) {
        rrScheduler.setQuantum(q);
    }

    public void setMappingType(ThreadMappingType type) {
        this.mappingType = type;
        mapper.setMappingType(type);
    }

    // ---------------- STEP ----------------
    public void step() {

        mapper.map(threads);

        for (CpuCore core : cores) {

            if (core.getCurrentThread() == null) {

                ThreadModel next = scheduler.getNext(threads, time);

                if (next != null) {
                    core.assign(next);
                }
            }

            ThreadModel running = core.getCurrentThread();

            if (running != null) {

                running.execute(time);

                if (scheduler instanceof RoundRobinScheduler rr) {
                    rr.onExecute(running);
                }

                if (running.getState() == ThreadState.TERMINATED) {
                    core.assign(null);
                }
            }
        }

        time++;
    }

    // ---------------- START ----------------
    public void start() {
        if (isRunning) return;

        isRunning = true;

        simulationThread = new Thread(() -> {
            while (isRunning) {
                step();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        simulationThread.start();
    }

    public void stop() {
        isRunning = false;
    }

    // ---------------- RESET ----------------
    public void reset() {
        stop();

        processes.clear();
        threads.clear();
        selectedProcess = null;

        for (CpuCore core : cores) {
            core.assign(null);
        }

        time = 0;
    }
    public void terminateSelectedProcess() {

        if (selectedProcess == null) return;

        for (ThreadModel t : selectedProcess.getThreads()) {
            t.setState(ThreadState.TERMINATED);
            threads.remove(t);
        }

        processes.remove(selectedProcess);
        selectedProcess = null;
    }

    // ---------------- GETTERS ----------------
    public List<ProcessModel> getProcesses() { return processes; }
    public List<ThreadModel> getThreads() { return threads; }
    public List<CpuCore> getCores() { return cores; }
    public int getTime() { return time; }
    public boolean isRunning() { return isRunning; }
}