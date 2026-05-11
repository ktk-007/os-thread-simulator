# Contributing to OS Thread Simulator

Thank you for your interest in contributing to OS Thread Simulator (RTOS).  
This project is an educational and professional visualization environment for CPU scheduling, threading models, and synchronization concepts in Operating Systems.

We welcome contributions from beginners and experienced developers alike.

---

# Table of Contents

- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Development Setup](#development-setup)
- [Coding Guidelines](#coding-guidelines)
- [Commit Message Convention](#commit-message-convention)
- [Pull Request Process](#pull-request-process)
- [Suggested Contribution Areas](#suggested-contribution-areas)
- [Reporting Bugs](#reporting-bugs)
- [Feature Requests](#feature-requests)

---

# Getting Started

## Fork the Repository

Click the **Fork** button on GitHub and clone your fork locally.

```bash
git clone https://github.com/YOUR_USERNAME/os-thread-simulator.git
cd os-thread-simulator
```

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

## Running the Project

### Linux / macOS

```bash
bash run.sh
```

### Windows

Run the packaged executable if available:

```text
RTOS_ThreadVision_Installer.exe
```

Or run manually using the JAR:

```bash
java -jar OSThreadSim.jar
```

---

# Project Structure

```text
src/com/ossim/
│
├── model/          # Core simulation models
├── scheduler/      # Scheduling engine and algorithms
├── ui/             # Main UI components
├── ui/panels/      # Specialized visualization panels
```

---

# Coding Guidelines

Please follow the existing coding style and architecture.

## General Rules

- Keep code modular and readable
- Avoid unnecessary dependencies
- Maintain the zero-dependency philosophy
- Use meaningful variable and method names
- Prefer enums over magic constants
- Separate simulation logic from UI logic

---

## UI Guidelines

- Reuse components from `UIUtils.java`
- Follow the existing dark theme system in `Theme.java`
- Keep UI updates efficient and responsive

---

## Scheduler Logic

When adding scheduling or synchronization features:

- Ensure fairness and correctness
- Update thread states consistently
- Keep timing calculations accurate
- Avoid blocking the Swing Event Dispatch Thread (EDT)

---

# Commit Message Convention

Use clear and descriptive commit messages.

## Examples

```text
docs: add contributing guidelines
fix: resolve round robin queue issue
feat: add starvation detection system
refactor: split scheduler logic into strategy classes
```

---

# Pull Request Process

1. Fork the repository
2. Create a new branch

```bash
git checkout -b feature/my-feature
```

3. Make your changes
4. Commit your work

```bash
git commit -m "feat: add new scheduling visualization"
```

5. Push to your fork

```bash
git push origin feature/my-feature
```

6. Open a Pull Request

---

# Suggested Contribution Areas

We welcome contributions in the following areas:

## Scheduling Algorithms

- FCFS improvements
- Round Robin optimizations
- Priority scheduling enhancements
- Starvation prevention

## Visualization

- Better Gantt chart rendering
- CPU utilization graphs
- Thread animations
- Timeline enhancements

## Synchronization

- Semaphore improvements
- Monitor visualization
- Deadlock detection
- Resource allocation graphs

## UI/UX

- Accessibility improvements
- Responsive layouts
- Theme enhancements
- Better controls and onboarding

## Documentation

- README improvements
- Architecture documentation
- Tutorials and examples

---

# Reporting Bugs

When reporting bugs, please include:

- Operating system
- Java version
- Steps to reproduce
- Expected behavior
- Actual behavior
- Screenshots (if applicable)

---

# Feature Requests

Feature suggestions are welcome.

Please provide:

- Clear problem description
- Proposed solution
- Expected behavior
- Optional mockups or diagrams

---

# Code of Conduct

Be respectful and constructive when interacting with contributors and maintainers.

---

# Acknowledgements

Developed and maintained by:

**Tharun (ktk-007)**  
GitHub: https://github.com/ktk-007

Thank you for contributing to OS Thread Simulator.
