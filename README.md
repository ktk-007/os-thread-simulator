# OS Thread Simulator

A Java Swing desktop application that simulates OS thread scheduling, synchronization, and kernel thread mapping.

## Requirements
- **Java JDK 11 or higher**
- Download from: https://adoptium.net (free, open-source)

## How to Build & Run

### Windows
1. Double-click `build.bat` — this compiles and packages `OSThreadSim.jar`
2. Double-click `run.bat` — or run `java -jar OSThreadSim.jar`

### Linux / macOS
```bash
chmod +x build.sh run.sh
./build.sh
./run.sh
```

## Features

### Control Panel (Top Bar)
| Control | Options |
|---|---|
| Thread Model | One-One, Many-One, Many-Many |
| Scheduling | FCFS, Round Robin, SJF, Priority |
| Config | Auto (random burst), Manual (fixed burst) |
| Sync | Semaphores, Monitors |
| CPU Cores | 1–16 |
| Time Quantum | 1–20 ticks (Round Robin only) |
| Burst Time | 1–30 ticks (Manual mode only) |

### Simulation Controls
- **▶ Start / ⏸ Pause** — auto-advance at 1 tick/second
- **⏭ Step** — advance one tick manually
- **↺ Reset** — clear everything, start fresh

### Left Panel — Processes
- Start with **no processes** — click **"+ Add Process"** to create one
- Each process starts with 2 threads
- **+Thread** — add a thread to a specific process
- **✕ Kill** — terminate all threads of a process (shows "Terminated" in tables, not "Done")

### Center Panel
- **Thread Movement Bar** — shows live transitions (Ready → Core, Preempted → Ready, Done ✓, etc.)
- **Synchronization Visualizer:**
  - *Semaphore mode*: lock icon, value, holder, wait queue per semaphore
  - *Monitor mode*: animated entry queue, mutex lock state, condition variable pulse
- **CPU Cores Grid** — shows which thread runs on each core with a progress bar

### Right Panel
- **Gantt Chart** — visual timeline of all threads with color coding
- **Thread Details tab** — PID, TID, Algorithm, Burst, TimeQ, Priority, Remaining, Sync status, Memory
- **Timing tab** — Wait time, Turnaround time, Start/Finish ticks
- **Status tab** — Kernel thread assignment, core, live status badge
- **Kernel Map tab** — Shows user→kernel thread mapping (changes with Thread Model)

## Thread Models Explained
- **One-One**: Every user thread → own kernel thread
- **Many-One**: All threads of a process → single kernel thread
- **Many-Many**: User threads grouped into roughly half as many kernel threads

## Status Colors
| Color | Meaning |
|---|---|
| 🔵 Cyan | Ready |
| 🟢 Green | Running |
| 🟡 Yellow | Waiting (blocked on semaphore) |
| ⚫ Grey | Done (natural completion) |
| 🔴 Red | Terminated (killed by user) |
