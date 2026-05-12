# OS Thread Simulator (RTOS)

A professional Operating System simulation environment for visualizing:

- CPU Scheduling Algorithms
- Multicore Execution
- Thread Mapping Models
- Synchronization Mechanisms
- Kernel-Level Thread Operations

This project acts as a **glass-box visualizer** for understanding complex operating system concepts through real-time simulation and interactive visual feedback.

---

# Table of Contents

- [Features](#features)
- [Architectural Philosophy](#architectural-philosophy)
- [Project Structure](#project-structure)
- [Java File Inventory](#java-file-inventory)
- [Installation & Setup](#installation--setup)
- [Running the Project](#running-the-project)
- [User Guide](#user-guide)
- [Screenshots](#screenshots)
- [Contributions](#contributions)
- [Author](#author)
- [License](#license)

---

# Features

## Multicore Hardware Simulation

- Simulate between **1 and 16 CPU cores**
- Real-time execution progress tracking
- Per-thread color visualization
- Dynamic burst-time progress bars

---

## CPU Scheduling Algorithms

Supports multiple scheduling strategies:

- FCFS (First Come First Serve)
- Round Robin (RR)
- Shortest Job First (SJF)
- Priority Scheduling

Algorithms can be switched dynamically during simulation.

---

## Threading Models

Visualize different user-to-kernel thread mappings:

- One-to-One (1:1)
- Many-to-One (M:1)
- Many-to-Many (M:M)

---

## Synchronization System

Supports dual synchronization models:

- Semaphores
- Monitors

Features include:

- Visual thread blocking
- Lock ownership tracking
- Waiting queue visualization
- Synchronization event monitoring

---

## Real-Time Visualization

### Gantt Chart

Persistent execution timeline for analyzing scheduling behavior.

### Thread Movement Feed

Tracks thread transitions such as:

```text
Ready → Running
Running → Waiting
Waiting → Done
```

### Core Activity Monitor

Observe live execution across multiple CPU cores.

---

## Data & Statistics Panels

### Thread Details

Displays:

- PID
- TID
- Burst Time
- Priority
- Memory Usage
- Scheduling Algorithm
- Time Quantum

### Timing Statistics

Tracks:

- Arrival Time
- Waiting Time
- Turnaround Time
- Completion Tick

### Thread Status

Live thread states:

- Ready
- Running
- Waiting
- Done
- Terminated

### Kernel Thread Map

Visual representation of thread mapping strategies.

---

# Architectural Philosophy

## Zero-Dependency Design

This project intentionally follows a strict **Zero-Dependency Philosophy**.

### Why?

- No Maven
- No Gradle
- No external frameworks

### Benefits

- Lightweight and portable
- Faster startup and compilation
- Full control over build process
- No dependency resolution issues
- Works on any machine with a standard JDK

---

# Project Structure

```text
src/com/ossim/
│
├── model/              # Core simulation models
├── scheduler/          # Scheduling engine and dispatch logic
├── ui/                 # Main UI framework
├── ui/panels/          # Specialized visualization panels
```

---

# Java File Inventory

| File | Description |
|------|-------------|
| `Main.java` | Application entry point |
| `KernelThread.java` | Kernel-level thread representation |
| `MoveEvent.java` | Tracks thread state transitions |
| `SchedulingAlgo.java` | Scheduling algorithm enum |
| `Semaphore.java` | Synchronization implementation |
| `SimProcess.java` | Process container |
| `SimThread.java` | Core thread model |
| `ThreadModel.java` | Thread mapping models |
| `ThreadStatus.java` | Thread lifecycle states |
| `SimEngine.java` | Main scheduling engine |
| `MainWindow.java` | Primary application window |
| `Theme.java` | UI styling system |
| `UIUtils.java` | Shared UI utilities |
| `CoresPanel.java` | CPU core visualizer |
| `MovementBar.java` | Thread movement event feed |
| `ProcessPanel.java` | Process management sidebar |
| `RightTablesPanel.java` | Statistics and Gantt panel |
| `StatusBar.java` | Global simulation statistics |
| `SyncPanel.java` | Synchronization visualizer |
| `TopBarPanel.java` | Simulation controls |

---

# Quick Start

## Prerequisites

- Java JDK 17+
- Git

Verify installation:

```bash
java -version
git --version
```

---

# Clone Repository

```bash
git clone https://github.com/ktk-007/os-thread-simulator.git
cd os-thread-simulator
```

---

# Run the Project

## Linux / macOS

```bash
bash run.sh
```

## Windows

Run:

```text
RTOS_ThreadVision_Installer.exe
```

Or launch manually:

```bash
java -jar OSThreadSim.jar
```

---

# User Guide

## Configure Simulation

Use the top control bar to:

- Select scheduling algorithms
- Configure core count
- Set time quantum
- Choose thread models

---

## Simulation Controls

| Control | Function |
|---------|----------|
| ▶ Start/Pause | Start or pause execution |
| ⏭ Step | Execute one simulation tick |

---

## Process Management

### Add Process

Creates a new simulated process with an initial thread.

### Add Thread

Click an existing process to create additional threads.

### Kill Process

Terminates all threads belonging to a process.

---

# Screenshots

> Simulator UI Screenshots:
>
> <img width="1516" height="969" alt="Image" src="https://github.com/user-attachments/assets/d0011857-bdd7-4730-8226-caba31a0b69d" />
> <img width="1516" height="969" alt="Image" src="https://github.com/user-attachments/assets/373bd52f-6685-4e59-93be-4e0ba51f3df3" />

---

# Contributions

Contributions are welcome.

Areas open for contribution:

- Scheduling improvements
- Visualization enhancements
- Synchronization systems
- Performance metrics
- Documentation improvements
- UI/UX refinements

Please read:

```text
CONTRIBUTING.md
```

before opening a Pull Request.

---

# Author

**Tharun (ktk-007)**

GitHub:  
https://github.com/ktk-007/os-thread-simulator

---

# License

This project is open-source and intended for educational and research purposes.
