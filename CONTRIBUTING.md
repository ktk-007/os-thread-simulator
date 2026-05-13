# Contributing to OS Thread Simulator

Thank you for your interest in contributing to OS Thread Simulator (RTOS).

OS Thread Simulator is a professional educational visualization environment for CPU scheduling, synchronization, thread mapping, and operating system execution models.

This project is designed as a "glass-box" Operating System simulator where contributors and learners can visually observe how scheduling algorithms, synchronization primitives, kernel-thread mapping, and execution states behave internally.

We welcome contributions from beginners, students, open-source contributors, and experienced systems developers alike.

---

# Table of Contents

- [Getting Started](#getting-started)
- [Project Philosophy](#project-philosophy)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Architecture Workflow](#architecture-workflow)
- [Coding Guidelines](#coding-guidelines)
- [Branch Naming Convention](#branch-naming-convention)
- [Commit Message Convention](#commit-message-convention)
- [Pull Request Process](#pull-request-process)
- [UI Contribution Guidelines](#ui-contribution-guidelines)
- [Suggested Contribution Areas](#suggested-contribution-areas)
- [Reporting Bugs](#reporting-bugs)
- [Feature Requests](#feature-requests)
- [Code of Conduct](#code-of-conduct)

---

# Getting Started

## Fork the Repository

Click the **Fork** button on GitHub and clone your fork locally.

```bash
git clone https://github.com/YOUR_USERNAME/os-thread-simulator.git
cd os-thread-simulator
```

---

# Project Philosophy

## Zero-Dependency Architecture

This project follows a strict **Zero-Dependency Philosophy**.

Unlike many modern Java projects, this simulator intentionally avoids:

- Maven
- Gradle
- External runtime frameworks
- Heavy dependency chains

The goal is to maintain:

- Full transparency of the build process
- Lightweight execution
- High portability
- Easier learning for students
- Complete control over compilation and runtime behavior

---

## Important Rule About Dependencies

Please avoid introducing:

- External libraries
- Build systems
- Runtime frameworks
- Third-party UI toolkits

unless the change has first been discussed with the maintainers.

This helps preserve the educational and lightweight architecture of the simulator.

---

# Development Setup

## Prerequisites

Make sure the following are installed:

- Java JDK 17 or later
- Git

Verify installation:

```bash
java -version
git --version
```

---

## Build the Project

### Linux / macOS

```bash
bash build.sh
```

This script:

- Compiles all Java source files
- Generates the `OSThreadSim.jar`
- Uses a custom manifest
- Maintains the zero-dependency workflow

---

## Run the Project

### Linux / macOS

```bash
bash run.sh
```

### Windows

Run the packaged executable if available:

```text
RTOS ThreadVision-1.0.exe
```

Or run manually:

```bash
java -jar OSThreadSim.jar
```

---

# Project Structure

```text
.
├── build.sh
├── run.sh
├── README.md
├── LICENSE.txt
├── src
│   ├── com
│   │   └── ossim
│   │       ├── Main.java
│   │       ├── model
│   │       ├── scheduler
│   │       └── ui
│   │           └── panels
│   └── resources
└── Webapp_version
```

---

## Core Structure Overview

### `model/`

Contains the operating system simulation models:

- Simulated Threads
- Processes
- Semaphores
- Kernel Thread mappings
- Scheduling enums
- Thread states

---

### `scheduler/`

Contains the simulation engine responsible for:

- Clock tick progression
- Scheduling decisions
- Core assignment
- Thread execution lifecycle
- Synchronization handling

---

### `ui/`

Contains:

- Main application window
- Theme management
- Shared UI utilities

---

### `ui/panels/`

Contains all visualization panels:

- CPU Core visualizer
- Gantt chart
- Movement/event feed
- Synchronization dashboard
- Thread tables
- Process controls

---

# Architecture Workflow

This section explains how the simulator internally operates so contributors can align changes with the current architecture.

---

# Scheduler Workflow

The core simulation engine is:

```text
src/com/ossim/scheduler/SimEngine.java
```

The simulation advances through discrete clock ticks.

Each tick performs the following stages:

---

## 1. Running Thread Execution

Currently running threads:

- Reduce remaining burst time
- Increment turnaround time
- Increment time quantum usage

Possible outcomes:

- Thread completes execution → `DONE`
- Thread enters I/O wait → `WAITING`
- Thread gets preempted (Round Robin) → `READY`

---

## 2. Waiting Thread Recovery

Threads in the `WAITING` state may:

- Continue waiting
- Re-enter the ready queue

This simulates asynchronous I/O wakeups and synchronization recovery.

---

## 3. Ready Queue Scheduling

Ready threads are sorted according to the selected algorithm:

### Supported Algorithms

- FCFS
- Round Robin
- SJF
- Priority Scheduling

Sorting logic is controlled by:

```text
getComparator()
```

inside `SimEngine.java`.

---

## 4. Core Assignment

Free CPU cores are populated using the scheduler output.

Each thread receives:

- Assigned CPU core
- Execution state updates
- Quantum reset
- Visualization updates

Core occupancy is visualized in:

```text
CoresPanel.java
```

---

## 5. Statistics Update

All timing metrics are updated:

- Wait time
- Turnaround time
- Finish tick
- Quantum usage

The UI tables and Gantt chart refresh after every tick.

---

# Synchronization Workflow

Synchronization currently uses semaphores.

Core synchronization logic exists in:

```text
src/com/ossim/model/Semaphore.java
```

---

## Semaphore Flow

Threads may attempt semaphore acquisition during scheduling.

### Successful Acquire

- Thread continues execution
- Semaphore becomes locked
- Ownership recorded

### Failed Acquire

- Thread enters `WAITING`
- Thread becomes visually blocked
- Added to semaphore wait queue

---

## Semaphore Release

When a thread finishes:

- Owned semaphores are released
- Waiting threads may wake up
- New ownership gets assigned

This workflow is handled inside:

```text
releaseSemaphores()
```

in `SimEngine.java`.

---

# Thread Mapping Workflow

The simulator supports:

- One-to-One
- Many-to-One
- Many-to-Many

mapping strategies.

Kernel thread rebuilding occurs dynamically inside:

```text
rebuildKthreads()
```

Each thread model changes:

- Kernel thread allocation
- Thread grouping
- Visualization mappings

The relationships are displayed in:

```text
Kernel Map
```

inside `RightTablesPanel.java`.

---

# Visualization Workflow

The simulator UI is designed as a real-time operating system dashboard.

---

## Gantt Chart Pipeline

The Gantt chart records:

- Execution slices
- Start tick
- Finish tick
- Thread activity duration

Rendering is handled inside:

```text
RightTablesPanel.java
```

using the internal:

```text
GanttPanel
```

class.

---

## CPU Core Visualization

`CoresPanel.java` visualizes:

- Running threads
- Core occupancy
- Remaining execution time
- Thread progress bars

Each thread receives a unique color for visual tracking.

---

## Thread Movement Feed

`MovementBar.java` displays real-time events such as:

```text
Ready → Running
Running → Waiting
Waiting → Ready
Running → Done
```

This acts as a live execution log for the simulation.

---

## UI Refresh Cycle

The UI updates through:

```java
SwingUtilities.invokeLater()
```

to maintain Swing Event Dispatch Thread safety.

Contributors should avoid blocking the EDT during:

- Scheduling
- Rendering
- Synchronization operations

---

# Coding Guidelines

Please follow the existing architecture and coding style.

---

# General Rules

- Keep code modular and readable
- Maintain separation between UI and simulation logic
- Prefer enums over magic constants
- Avoid deeply coupled code
- Keep scheduling logic deterministic where possible
- Preserve real-time visualization responsiveness

---

# Dependency Rules

Do not introduce:

- Maven
- Gradle
- External UI frameworks
- Heavy runtime dependencies

without prior maintainer discussion.

---

# UI Guidelines

When modifying UI components:

- Reuse utilities from `UIUtils.java`
- Follow the existing theme system in `Theme.java`
- Preserve dark-theme consistency
- Keep animations lightweight
- Avoid excessive repaint loops

---

# Scheduler Guidelines

When modifying scheduling logic:

- Ensure thread states remain valid
- Keep timing metrics accurate
- Preserve fairness guarantees
- Avoid scheduler starvation unless intentionally simulated

---

# Synchronization Guidelines

When working on semaphores or monitors:

- Avoid inconsistent lock ownership
- Ensure waiting queues remain valid
- Prevent synchronization deadlocks
- Keep visualization states aligned with internal states

---

# Branch Naming Convention

Please follow this branch naming style:

## Examples

```bash
feat/add-priority-aging
fix/round-robin-quantum-bug
docs/update-contributing-guide
ui/improve-gantt-visualization
refactor/scheduler-cleanup
```

---

# Commit Message Convention

Use clear and descriptive commit messages.

## Examples

```text
feat: add starvation prevention system
fix: resolve semaphore queue issue
docs: improve contributing documentation
ui: enhance core visualization rendering
refactor: simplify scheduling workflow
```

---

# Pull Request Process

## 1. Fork the Repository

Create your own fork of the project.

---

## 2. Create a New Branch

```bash
git checkout -b feat/my-feature
```

---

## 3. Make Your Changes

Ensure:

- Code compiles correctly
- Existing functionality remains stable
- UI changes follow the current design system

---

## 4. Commit Your Changes

```bash
git commit -m "feat: add scheduler enhancement"
```

---

## 5. Push Your Branch

```bash
git push origin feat/my-feature
```

---

## 6. Open a Pull Request

Use descriptive PR titles.

### Example PR Titles

```text
feat: add starvation prevention visualization
fix: resolve semaphore synchronization issue
docs: improve architecture documentation
ui: enhance gantt chart rendering
```

---

# UI Contribution Guidelines

For UI-related pull requests, contributors are strongly encouraged to include:

- Screenshots
- Short GIFs
- Screen recordings/videos

This helps maintainers review:

- Visualization behavior
- Animation correctness
- UI responsiveness
- Layout consistency

You may attach media directly in:

- Pull Request descriptions
- Pull Request comments

---

# Suggested Contribution Areas

We welcome contributions in the following areas.

---

# Scheduling Algorithms

- FCFS improvements
- Round Robin optimizations
- Priority scheduling enhancements
- Starvation prevention
- Aging systems
- Multi-level queue scheduling

---

# Visualization

- Better Gantt chart rendering
- Timeline scaling
- Execution heatmaps
- CPU utilization graphs
- Thread execution animations

---

# Synchronization

- Monitor implementation
- Deadlock detection
- Resource allocation graphs
- Lock contention visualization

---

# Performance

- Rendering optimizations
- Simulation scaling
- Large-thread-count performance improvements

---

# UI / UX

- Accessibility improvements
- Better onboarding
- Improved controls
- Responsive layouts
- Theme improvements

---

# Documentation

- Architecture documentation
- Tutorials
- OS theory explanations
- Example simulations

---

# Reporting Bugs

When reporting bugs, please include:

- Operating system
- Java version
- Steps to reproduce
- Expected behavior
- Actual behavior
- Screenshots/videos if applicable

---

# Feature Requests

Feature suggestions are welcome.

Please provide:

- Problem description
- Proposed solution
- Expected behavior
- Optional diagrams/mockups

---

# Code of Conduct

Please remain respectful and constructive in all discussions and reviews.

Healthy collaboration and learning are core goals of this project.

---

# Acknowledgements

Developed and maintained by:

**Tharun (ktk-007)**

GitHub:
https://github.com/ktk-007

---

Thank you for contributing to OS Thread Simulator.
Your contributions help make operating system concepts more visual, interactive, and accessible for learners everywhere.
