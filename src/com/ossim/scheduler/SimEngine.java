package com.ossim.scheduler;

import com.ossim.model.*;
import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

public class SimEngine {

    public interface SimListener {
        void onTick(int clock, List<MoveEvent> events);
        void onFinished();
    }

    // --- Config ---
    private ThreadModel    threadModel  = ThreadModel.ONE_ONE;
    private SchedulingAlgo algo         = SchedulingAlgo.ROUND_ROBIN;
    private boolean        autoConfig   = true;
    private boolean        useSemaphores = true;
    private int            coreCount    = 4;
    private int            timeQuantum  = 3;
    private int            defaultBurst = 5;

    // --- State ---
    private final List<SimProcess>    processes   = new ArrayList<>();
    private final List<KernelThread>  kthreads    = new ArrayList<>();
    private final List<Semaphore>     semaphores  = new ArrayList<>();
    private final List<MoveEvent>     moveHistory = new ArrayList<>();
    private int   clock     = 0;
    private int   ktCounter = 1;

    private SimListener listener;

    public void setListener(SimListener l) { this.listener = l; }

    // ---- Init (empty — no default processes) ----
    public void init() {
        SimProcess.resetCounter();
        SimThread.resetCounter();
        processes.clear();
        kthreads.clear();
        semaphores.clear();
        moveHistory.clear();
        clock = 0;
        ktCounter = 1;
    }

    // ---- Process / Thread management ----
    public SimProcess addProcess() {
        SimProcess p = new SimProcess(algo, defaultBurst, autoConfig);
        processes.add(p);
        rebuildKthreads();
        rebuildSemaphores();
        return p;
    }

    public SimThread addThread(String pid) {
        SimProcess p = processes.stream().filter(x -> x.getPid().equals(pid)).findFirst().orElse(null);
        if (p == null) return null;
        SimThread t = p.addThread(algo, defaultBurst, autoConfig);
        rebuildKthreads();
        return t;
    }

    /** Mark all threads of a process as TERMINATED (not DONE — user-initiated kill) */
    public void terminateProcess(String pid) {
        processes.stream().filter(p -> p.getPid().equals(pid)).findFirst()
            .ifPresent(p -> p.getThreads().forEach(t -> {
                if (t.getStatus() != ThreadStatus.DONE && t.getStatus() != ThreadStatus.TERMINATED) {
                    t.setStatus(ThreadStatus.TERMINATED);
                    t.setAssignedCore(0);
                }
            }));
        rebuildKthreads();
        rebuildSemaphores();
    }

    // ---- Kernel thread mapping ----
    public void rebuildKthreads() {
        kthreads.clear();
        ktCounter = 1;
        List<SimThread> active = allThreads().stream()
            .filter(t -> t.getStatus() != ThreadStatus.DONE && t.getStatus() != ThreadStatus.TERMINATED)
            .collect(Collectors.toList());

        switch (threadModel) {
            case ONE_ONE:
                for (SimThread t : active) {
                    KernelThread kt = new KernelThread("Kthread" + ktCounter++, t.getPid());
                    kt.addThread(t.getTid());
                    t.setKthreadId(kt.getId());
                    kthreads.add(kt);
                }
                break;
            case MANY_ONE:
                for (SimProcess proc : processes) {
                    List<SimThread> pts = proc.getThreads().stream()
                        .filter(t -> t.getStatus() != ThreadStatus.DONE && t.getStatus() != ThreadStatus.TERMINATED)
                        .collect(Collectors.toList());
                    if (pts.isEmpty()) continue;
                    KernelThread kt = new KernelThread("Kthread" + ktCounter++, proc.getPid());
                    for (SimThread t : pts) { kt.addThread(t.getTid()); t.setKthreadId(kt.getId()); }
                    kthreads.add(kt);
                }
                break;
            case MANY_MANY:
                for (int i = 0; i < active.size(); i += 2) {
                    KernelThread kt = new KernelThread("Kthread" + ktCounter++, active.get(i).getPid());
                    kt.addThread(active.get(i).getTid());
                    active.get(i).setKthreadId(kt.getId());
                    if (i + 1 < active.size()) {
                        kt.addThread(active.get(i + 1).getTid());
                        active.get(i + 1).setKthreadId(kt.getId());
                    }
                    kthreads.add(kt);
                }
                break;
        }
    }

    public void rebuildSemaphores() {
        // Keep existing semaphores, just ensure count matches active threads
        int n = Math.min(3, Math.max(0, allThreads().size() / 2));
        while (semaphores.size() < n) {
            semaphores.add(new Semaphore("Sem_" + (char)('A' + semaphores.size()), 1));
        }
        while (semaphores.size() > n) {
            semaphores.remove(semaphores.size() - 1);
        }
    }

    // ---- Simulation tick ----
    public void tick() {
        if (processes.isEmpty()) return;
        List<MoveEvent> events = new ArrayList<>();
        List<SimThread> all    = allThreads();

        // 1. Advance running threads
        List<SimThread> running = all.stream()
            .filter(t -> t.getStatus() == ThreadStatus.RUNNING)
            .collect(Collectors.toList());

        for (SimThread t : running) {
            t.setRemainingTime(Math.max(0, t.getRemainingTime() - 1));
            t.incrementTurnaround();
            t.incrementQuantum();

            if (t.getRemainingTime() <= 0) {
                t.setStatus(ThreadStatus.DONE);
                t.setFinishTick(clock);
                releaseSemaphores(t, events);
                events.add(new MoveEvent(t.getPid(), t.getTid(), "Core " + t.getAssignedCore(), "Done ✓", t.getColor(), clock));
                t.setAssignedCore(0);
                t.resetQuantum();
            } else if (algo == SchedulingAlgo.ROUND_ROBIN && t.getTimeQuantumUsed() >= timeQuantum) {
                t.setStatus(ThreadStatus.READY);
                events.add(new MoveEvent(t.getPid(), t.getTid(), "Core " + t.getAssignedCore(), "Preempted → Ready", t.getColor(), clock));
                t.setAssignedCore(0);
                t.resetQuantum();
            }
        }

        // 2. Waiting threads may unblock
        all.stream().filter(t -> t.getStatus() == ThreadStatus.WAITING).forEach(t -> {
            t.incrementWaitTime();
            t.incrementTurnaround();
            if (Math.random() < 0.30) {
                t.setStatus(ThreadStatus.READY);
                t.setSyncStatus("-");
                events.add(new MoveEvent(t.getPid(), t.getTid(), "Waiting", "Ready", t.getColor(), clock));
            }
        });

        // 3. Sort ready queue
        List<SimThread> readyQ = all.stream()
            .filter(t -> t.getStatus() == ThreadStatus.READY)
            .sorted(getComparator())
            .collect(Collectors.toList());

        // 4. Assign to free cores
        Set<Integer> busyCores = all.stream()
            .filter(t -> t.getStatus() == ThreadStatus.RUNNING)
            .map(SimThread::getAssignedCore)
            .collect(Collectors.toSet());

        for (int core = 1; core <= coreCount && !readyQ.isEmpty(); core++) {
            if (!busyCores.contains(core)) {
                SimThread t = readyQ.remove(0);
                // Semaphore gate (12% chance)
                if (useSemaphores && !semaphores.isEmpty() && Math.random() < 0.12) {
                    Semaphore sem = semaphores.get((int)(Math.random() * semaphores.size()));
                    if (!sem.tryAcquire(t.getTid())) {
                        t.setStatus(ThreadStatus.WAITING);
                        t.setSyncStatus("Blocked:" + sem.getName());
                        events.add(new MoveEvent(t.getPid(), t.getTid(), "Ready", "Waiting (" + sem.getName() + ")", t.getColor(), clock));
                        continue;
                    }
                }
                t.setStatus(ThreadStatus.RUNNING);
                t.setStartTick(clock);
                t.setAssignedCore(core);
                t.resetQuantum();
                busyCores.add(core);
                events.add(new MoveEvent(t.getPid(), t.getTid(), "Ready", "Core " + core, t.getColor(), clock));
            }
        }

        // 5. Remaining ready threads wait
        all.stream().filter(t -> t.getStatus() == ThreadStatus.READY).forEach(SimThread::incrementWaitTime);

        clock++;
        moveHistory.addAll(events);
        if (moveHistory.size() > 300) moveHistory.subList(0, moveHistory.size() - 300).clear();
        rebuildKthreads();

        if (listener != null) {
            listener.onTick(clock, events);
            boolean allDone = all.stream()
                .allMatch(t -> t.getStatus() == ThreadStatus.DONE || t.getStatus() == ThreadStatus.TERMINATED);
            if (!all.isEmpty() && allDone) listener.onFinished();
        }
    }

    private void releaseSemaphores(SimThread t, List<MoveEvent> events) {
        for (Semaphore s : semaphores) {
            if (t.getTid().equals(s.getHeldBy())) {
                String next = s.release();
                if (next != null) {
                    SimThread nt = getThread(next);
                    if (nt != null) { nt.setStatus(ThreadStatus.READY); nt.setSyncStatus("-"); }
                }
            }
        }
    }

    private Comparator<SimThread> getComparator() {
        switch (algo) {
            case SJF:      return Comparator.comparingInt(SimThread::getRemainingTime);
            case PRIORITY: return Comparator.comparingInt(SimThread::getPriority).reversed();
            default:       return Comparator.comparingInt(t -> 0);
        }
    }

    public SimThread getThread(String tid) {
        return allThreads().stream().filter(t -> t.getTid().equals(tid)).findFirst().orElse(null);
    }

    public List<SimThread> allThreads() {
        return processes.stream().flatMap(p -> p.getThreads().stream()).collect(Collectors.toList());
    }

    // ---- Getters / Setters ----
    public List<SimProcess>   getProcesses()   { return processes; }
    public List<KernelThread> getKthreads()    { return kthreads; }
    public List<Semaphore>    getSemaphores()  { return semaphores; }
    public List<MoveEvent>    getMoveHistory() { return moveHistory; }
    public int                getClock()       { return clock; }

    public void setThreadModel(ThreadModel m)  { threadModel = m;   rebuildKthreads(); }
    public void setAlgo(SchedulingAlgo a)       { algo = a; }
    public void setAutoConfig(boolean b)        { autoConfig = b; }
    public void setUseSemaphores(boolean b)     { useSemaphores = b; rebuildSemaphores(); }
    public void setCoreCount(int n)             { coreCount = Math.max(1, Math.min(16, n)); }
    public void setTimeQuantum(int q)           { timeQuantum = Math.max(1, q); }
    public void setDefaultBurst(int b)          { defaultBurst = Math.max(1, b); }

    public ThreadModel    getThreadModel()  { return threadModel; }
    public SchedulingAlgo getAlgo()         { return algo; }
    public int            getCoreCount()    { return coreCount; }
    public int            getTimeQuantum()  { return timeQuantum; }
    public boolean        isAutoConfig()    { return autoConfig; }
    public boolean        isUseSemaphores() { return useSemaphores; }
}
