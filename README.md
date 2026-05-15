# OS THREAD SIMULATOR (RTOS)

A professional simulation environment for Operating System CPU scheduling, synchronization, and threading models. This project serves as a "glass-box" visualizer for complex kernel-level operations.

---

## ARCHITECTURAL PHILOSOPHY: THE "ZERO-DEPENDENCY" APPROACH

Unlike many modern Java projects, this simulator was built with a **Strict Zero-Dependency** philosophy.

- **No Maven / No Gradle**: We consciously avoided these tools to ensure the project stays lightweight and portable. Using Maven would introduce a large overhead of hidden scripts and a "wait-to-download" cycle.
- **Raw Control**: By writing our own build scripts, we maintain 100% control over the compilation process and classpaths.
- **Portability**: The project can be compiled and run on any machine with a standard JDK installed, without needing to resolve external dependencies or configure complex environments.
- **Open Source**: We have fully **Open Sourced** this project to encourage learning and collaboration. You can find the source, modify it, and study the kernel logic without restriction.

---

## CORE FEATURES

### Multicore Hardware Simulation

- **Adjustable Cores**: Simulate between **1 and 16 CPU cores**.
- **Real-Time Execution**: Each core features a dynamic progress bar showing the remaining burst time for the currently assigned thread.
- **Per-Thread Colors**: Threads are assigned unique colors, making it easy to track them across the cores and the Gantt chart.

### Comprehensive Data Tables

The simulator provides four distinct views into the system state:

- **Thread Details**: Shows the raw data: PID, TID, active Algorithm, Burst Time, Time Quantum, Priority, and Memory footprint (MB).
- **Timing Statistics**: Tracks real-time performance metrics: Arrival Time, Wait Time, Total Turnaround Time, and the exact clock tick when a thread finishes.
- **Thread Status**: A live view of where each thread is (Ready, Running, Waiting, or Done), highlighting its current Core and Kernel Thread mapping.
- **Kernel Thread Map**: Visualizes the relationship between User Threads and Kernel Threads based on the selected Thread Model (1:1, M:1, or M:M).

### Synchronization & Control

- **Dual Sync Models**: Toggle between **Semaphores** and **Monitors**.
- **Visual Blocking**: Threads are visibly blocked (turning yellow) when trying to acquire a held lock.
- **Lock Dashboard**: Shows which thread holds the lock and the queue of threads waiting to enter.

### Gantt Chart & Movement Bar

- **Historical Timeline**: A persistent Gantt chart records every execution slice, allowing for post-simulation analysis.
- **Event Feed**: The "Thread Movement" bar provides a human-readable stream of events (e.g., "P1-T1: Ready -> Core 2").

---

## PROJECT INVENTORY (JAVA FILE MAP)

Below is a short guide to every file in the repository and its role in the system:

| File Path                               | Description                                                            |
| --------------------------------------- | ---------------------------------------------------------------------- |
| `src/com/ossim/Main.java`               | The application entry point; initializes the UI and core engine.       |
| `src/com/ossim/model/KernelThread.java` | Represents a kernel-level thread object used in thread mapping models. |

| `src/com/ossim/model/MoveEvent.java` | A data structure for tracking thread state transitions (used for UI animations). |

| `src/com/ossim/model/SchedulingAlgo.java` | Enum defining the supported algorithms (FCFS, RR, SJF, Priority). |

| `src/com/ossim/model/Semaphore.java` | Implements the mutual exclusion and signaling logic for synchronization. |

| `src/com/ossim/model/SimProcess.java` | A logical container for a group of user threads. |

| `src/com/ossim/model/SimThread.java` | The core thread model tracking state, burst time, priority, and wait times. |

| `src/com/ossim/model/ThreadModel.java` | Enum for thread mapping strategies: 1:1, Many-to-One, and Many-to-Many. |

| `src/com/ossim/model/ThreadStatus.java` | Defines lifecycle states: Ready, Running, Waiting, Done, Terminated. |

| `src/com/ossim/scheduler/SimEngine.java` | The "Kernel Engine": Handles the clock cycles and algorithm dispatch logic. |

| `src/com/ossim/ui/MainWindow.java` | The primary application frame that orchestrates the overall layout and updates. |

| `src/com/ossim/ui/Theme.java` | Central repository for styling tokens, including the custom dark mode palette. |

| `src/com/ossim/ui/UIUtils.java` | Reusable UI components like dark-themed buttons, spinners, and combo boxes. |

| `src/com/ossim/ui/panels/CoresPanel.java` | A specialized visualizer for CPU core occupancy and execution progress. |

| `src/com/ossim/ui/panels/CustomTitleBar.java` | A custom-drawn draggable window frame replacing the standard OS chrome. |

| `src/com/ossim/ui/panels/MovementBar.java` | A live event feed showing thread movements (Ready → Running, etc.). |

| `src/com/ossim/ui/panels/ProcessPanel.java` | Side panel sidebar for managing processes and manually adding threads. |

| `src/com/ossim/ui/panels/RightTablesPanel.java` | A tabbed interface hosting the Gantt chart and all technical data tables. |

| `src/com/ossim/ui/panels/StatusBar.java` | Displays global statistics like the current clock tick and total thread count. |

| `src/com/ossim/ui/panels/SyncPanel.java` | Visualizes the state and queues of Semaphores and Monitors (Locks/Signals). |

| `src/com/ossim/ui/panels/TopBarPanel.java` | The control center for configuring algorithms, cores, and time quanta. |

---

## USER MANUAL & USAGE

### 1. Build and Run

- **Windows**: Use the professional **RTOS_ThreadVision_Installer.exe** (created via jpackage) for a full installation.
- **Linux / macOS**:
  - Ensure Java is installed.
  - You can simply **double-click the OSThreadSim.jar** file to launch the app.
  - Alternatively, use the terminal: `bash run.sh`.

### 2. Configuring the Simulation

- **Top Bar**: Use this to swap between **FCFS**, **Round Robin**, **SJF**, and **Priority** on the fly.
- **Thread Models**: Toggle between **1:1**, **Many-to-One**, and **Many-to-Many**.
- **Controls**: Use **▶ Start/Pause** for automatic time progression or **⏭ Step** for tick-by-tick debugging.

### 3. Managing Processes

- **Add Process**: Creates a new process container with an initial thread.
- **Add Thread**: In the left sidebar, click the process name to add more threads to it.
- **Kill Process**: Instantly terminates all threads within that process.

### 4. Reading the Visuals

- **Gantt Chart**: Located in the first tab on the right; provides a timeline of execution.
- **Sync Panel**: Watch for the lock icon to see which thread currently holds a semaphore.
- **Cores**: Observe the green bars to see real-time execution progress on the selected cores.

---

## ROADMAP: FUTURE VERSIONS

- **Stand-alone Windows EXE**: We are currently developing a full-fledged Windows application version.
- **Native Installer**: Future releases will include a professional `.exe` installer (using `jpackage`), which wil bundle its own Java Runtime—no manual setup required.
- **Cross-Platform Bundles**: Standalone packages for Linux and macOS.

---

**Ownership**: Tharun (ktk-007) | **GitHub**: https://github.com/ktk-007/os-thread-simulator
