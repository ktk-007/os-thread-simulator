package com.tharun.threadsim.engine;

import com.tharun.threadsim.model.CpuCore;
import com.tharun.threadsim.model.ProcessModel;
import com.tharun.threadsim.model.ThreadModel;
import com.tharun.threadsim.model.ThreadState;
import com.tharun.threadsim.scheduler.FCFSScheduler;
import com.tharun.threadsim.scheduler.SchedulingAlgorithm;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {

    private List<ProcessModel> processes = new ArrayList<>();
    private List<ThreadModel> readyQueue;
    private List<CpuCore> cpuCores;
    private SchedulingAlgorithm scheduler;
    private boolean isRunning = false;

    public SimulationEngine() {
        this.readyQueue = new ArrayList<>();
        this.cpuCores = new ArrayList<>();

        // Default scheduler
        this.scheduler = new FCFSScheduler();
    }

    public void initialize() {
        System.out.println("Simulation Engine Initialized");

    }
    public void addProcess(ProcessModel process) {
        processes.add(process);
    }

    public List<ProcessModel> getProcesses() {
        return processes;
    }

    public int getProcessCount() {
        return processes.size();
    }
    // Add thread to ready queue
    public void addThread(ThreadModel thread) {
        thread.setState(ThreadState.READY);
        readyQueue.add(thread);
    }

    // Create CPU cores dynamically
    public void createCores(int numberOfCores) {
        cpuCores.clear();
        for (int i = 1; i <= numberOfCores; i++) {
            cpuCores.add(new CpuCore("Core-" + i));
        }
    }

    // Perform one scheduling step
    public void schedule() {

        for (CpuCore core : cpuCores) {

            if (core.isIdle() && !readyQueue.isEmpty()) {

                ThreadModel nextThread = scheduler.selectNextThread(readyQueue);

                if (nextThread != null) {
                    readyQueue.remove(nextThread);
                    core.assignThread(nextThread);
                }
            }
        }
    }

    public void setScheduler(SchedulingAlgorithm scheduler) {
        this.scheduler = scheduler;
    }

    public List<ThreadModel> getReadyQueue() {
        return readyQueue;
    }

    public List<CpuCore> getCpuCores() {
        return cpuCores;
    }

    public void startSimulation() {
        isRunning = true;
    }

    public void stopSimulation() {
        isRunning = false;
    }

    public void resetSimulation() {

        stopSimulation();

        readyQueue.clear();
        cpuCores.clear();
        processes.clear();
    }

    public boolean isSimulationRunning() {
        return isRunning;
    }

    public boolean isSimulationFinished() {

        if (!readyQueue.isEmpty()) {
            return false;
        }

        for (CpuCore core : cpuCores) {
            if (!core.isIdle()) {
                return false;
            }
        }

        return true;
    }
    public void runOneStep() {

        if (!isRunning) {
            return;
        }


        for (CpuCore core : cpuCores) {

            ThreadModel runningThread = core.getCurrentThread();

            if (runningThread != null) {

                runningThread.decreaseRemainingTime(1);

                System.out.println(
                        runningThread.getThreadId() +
                                " running on " +
                                core.getCoreId() +
                                " | Remaining: " +
                                runningThread.getRemainingTime()
                );

                // If finished
                if (runningThread.getRemainingTime() == 0) {
                    runningThread.setState(ThreadState.TERMINATED);
                    core.assignThread(null); // free the core
                    System.out.println(runningThread.getThreadId() + " terminated.");
                }
            }
        }


        schedule();
    }


}
