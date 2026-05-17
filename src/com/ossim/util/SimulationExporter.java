package com.ossim.util;

import com.ossim.model.*;
import com.ossim.scheduler.SimEngine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class SimulationExporter {

    // Extract final core assignments from SimThread state + MoveEvent history fallback
    private static Map<String, String> extractFinalCoreIds(java.util.List<MoveEvent> moveHistory, SimEngine engine) {
        Map<String, String> finalCores = new HashMap<>();
        
        // PRIORITY 1: Read getAssignedCore() directly from SimThread objects
        // (This is captured at onFinished time before engine clears state)
        for (SimThread t : engine.allThreads()) {
            int coreNum = t.getAssignedCore();
            String coreStr = (coreNum >= 0) ? ("Core-" + coreNum) : "";
            finalCores.put(t.getTid(), coreStr);
        }
        
        // PRIORITY 2: If any thread still has empty core ID, try MoveEvent history
        if (moveHistory != null && !moveHistory.isEmpty()) {
            // Known state strings to exclude
            java.util.Set<String> knownStates = new java.util.HashSet<>(java.util.Arrays.asList(
                "Ready", "Running", "Waiting", "Blocked", "Done", "Created", "Terminated"
            ));
            
            
            for (MoveEvent event : moveHistory) {
                String tid = event.tid;
                if ((finalCores.get(tid) == null || finalCores.get(tid).isEmpty())) {
                   
                    if (!knownStates.contains(event.to) && event.to != null && !event.to.isEmpty()) {
                        finalCores.put(tid, event.to);
                    }
                   
                    else if (!knownStates.contains(event.from) && event.from != null && !event.from.isEmpty()) {
                        finalCores.put(tid, event.from);
                    }
                }
            }
        }
        
        return finalCores;
    }

    /**
     * Export simulation data to CSV with 5 sections
     * 
     * @param engine The simulation engine
     * @param outputFile The CSV output file
     * @param moveHistory Snapshotted MoveEvent history
     * @param finalCoreIds Snapshotted core ID assignments
     * @return true if export succeeded, false on error
     */
    public static boolean exportToCSV(SimEngine engine, File outputFile, 
            java.util.List<MoveEvent> moveHistory, Map<String, String> finalCoreIds) {
        try {
            try (PrintWriter writer = new PrintWriter(outputFile)) {
                java.util.List<MoveEvent> historyToUse = (moveHistory != null && !moveHistory.isEmpty()) 
                    ? moveHistory : engine.getMoveHistory();
                Map<String, String> coreIdsToUse = (finalCoreIds != null && !finalCoreIds.isEmpty()) 
                    ? finalCoreIds : extractFinalCoreIds(historyToUse, engine);
                
                // Write all 5 sections to single file
                writeThreadDetails(writer, engine);
                writer.println();
                
                writeTimingStatistics(writer, engine);
                writer.println();
                
                writeThreadStatus(writer, engine, coreIdsToUse);
                writer.println();
                
                writeKernelThreadMap(writer, engine);
                writer.println();
                
                writeGanttChart(writer, engine, historyToUse);
            }

            return true;
        } catch (IOException e) {
            System.err.println("Export failed: " + e.getMessage());
            return false;
        }
    }



    /**
     * SECTION 1: Thread Details
     * BUG FIX #1: Only show priority for PRIORITY algorithm, show "-" otherwise
     */
    private static void writeThreadDetails(PrintWriter writer, SimEngine engine) {
        writer.println("===== SECTION 1: THREAD DETAILS =====");
        writer.println("PID,TID,Algorithm,BurstTime,TimeQuantum,Priority,Memory_MB");
        
        for (SimThread t : engine.allThreads()) {
            String algo = algoShortName(t.getAlgo());
            int quantum = (t.getAlgo() == SchedulingAlgo.ROUND_ROBIN) ? 
                engine.getTimeQuantum() : 0;
            
            // BUG FIX #1: Only show priority if using PRIORITY algorithm
            String priority = (t.getAlgo() == SchedulingAlgo.PRIORITY) ? 
                String.valueOf(t.getPriority()) : "—";
            
            writer.println(escapeCsvValue(t.getPid()) + "," +
                escapeCsvValue(t.getTid()) + "," +
                algo + "," +
                t.getBurstTime() + "," +
                quantum + "," +
                priority + "," +
                t.getMemoryMB());
        }
    }

    /**
     * SECTION 2: Timing Statistics
     * FIXES: 
     * - CompletionTick = final clock value when thread finishes
     * - Derive TurnaroundTime = CompletionTick - ArrivalTime
     * - Derive WaitTime = TurnaroundTime - BurstTime
     */
    private static void writeTimingStatistics(PrintWriter writer, SimEngine engine) {
        writer.println("===== SECTION 2: TIMING STATISTICS =====");
        writer.println("TID,ArrivalTime,WaitTime,TurnaroundTime,CompletionTick");
        
        for (SimThread t : engine.allThreads()) {
            int arrival = t.getStartTick() >= 0 ? t.getStartTick() : 0;
            // CompletionTick is when thread became DONE (finishTick + 1)
            int completion = t.getFinishTick() >= 0 ? (t.getFinishTick() + 1) : 0;
            
            // DERIVE from ticks, don't read from SimThread fields
            int tat = completion - arrival;  // TurnaroundTime
            int wait = tat - t.getBurstTime();  // WaitTime
            
            writer.println(escapeCsvValue(t.getTid()) + "," +
                arrival + "," +
                wait + "," +
                tat + "," +
                completion);
        }
    }

    /**
     * SECTION 3: Thread Status
     * BUG FIX #3: Use final core assignments from MoveEvent history instead of current state
     */
    private static void writeThreadStatus(PrintWriter writer, SimEngine engine, Map<String, String> finalCoreId) {
        writer.println("===== SECTION 3: THREAD STATUS =====");
        writer.println("TID,FinalStatus,CoreID,KernelThreadID");
        
        for (SimThread t : engine.allThreads()) {
            String status = t.getStatus().getLabel();
            
            // BUG FIX #3: Use extracted final core ID from history, not current state
            String coreId = finalCoreId.getOrDefault(t.getTid(), "");
            if (coreId.isEmpty()) {
                coreId = "—";
            } else if (coreId.matches("\\d+")) {
                // If it's just a number (e.g., "0"), format as "Core-X"
                coreId = "Core-" + coreId;
            }
            
            String kthreadId = !t.getKthreadId().isEmpty() ? t.getKthreadId() : "—";
            
            writer.println(escapeCsvValue(t.getTid()) + "," +
                escapeCsvValue(status) + "," +
                coreId + "," +
                kthreadId);
        }
    }

    /**
     * SECTION 4: Kernel Thread Map
     */
    private static void writeKernelThreadMap(PrintWriter writer, SimEngine engine) {
        writer.println("===== SECTION 4: KERNEL THREAD MAP =====");
        writer.println("UserThreadID,KernelThreadID,ThreadModel");
        
        String threadModel = engine.getThreadModel().toString();
        
        for (SimThread t : engine.allThreads()) {
            String kthreadId = !t.getKthreadId().isEmpty() ? t.getKthreadId() : "—";
            
            writer.println(escapeCsvValue(t.getTid()) + "," +
                kthreadId + "," +
                threadModel);
        }
    }

    /**
     * SECTION 5: Gantt Chart (execution timeline with RR slices)
     * Use snapshotted MoveEvent history to show individual RR slices
     * Fallback: Use SimThread start/finish ticks if history is unavailable
     */
    private static void writeGanttChart(PrintWriter writer, SimEngine engine, java.util.List<MoveEvent> moveHistory) {
        writer.println("===== SECTION 5: GANTT CHART =====");
        writer.println("TID,StartTick,EndTick,CoreID");
        
        boolean wroteData = false;
        
        // Try to parse move history first
        if (moveHistory != null && !moveHistory.isEmpty()) {
            Map<String, java.util.List<GanttSlice>> slices = extractGanttSlices(moveHistory);
            for (String tid : slices.keySet()) {
                for (GanttSlice slice : slices.get(tid)) {
                    writer.println(escapeCsvValue(tid) + "," +
                        slice.startTick + "," +
                        slice.endTick + "," +
                        slice.coreId);
                    wroteData = true;
                }
            }
        }
        
        // FALLBACK: If no data was written from history, use SimThread start/finish ticks
        if (!wroteData) {
            for (SimThread t : engine.allThreads()) {
                int start = t.getStartTick();
                int finish = t.getFinishTick();
                if (start >= 0 && finish >= 0) {
                    // Format core as "Core-X"
                    int coreNum = t.getAssignedCore();
                    String coreId = (coreNum >= 0) ? ("Core-" + coreNum) : "—";
                    
                    writer.println(escapeCsvValue(t.getTid()) + "," +
                        start + "," +
                        (finish + 1) + "," +
                        coreId);
                    wroteData = true;
                }
            }
        }
    }

    
    private static Map<String, java.util.List<GanttSlice>> extractGanttSlices(java.util.List<MoveEvent> moveHistory) {
        Map<String, java.util.List<GanttSlice>> slices = new HashMap<>();
        Map<String, Integer> sliceStart = new HashMap<>();  // Track when thread entered a core
        Map<String, String> sliceCore = new HashMap<>();    // Track which core thread is on
        
        for (MoveEvent event : moveHistory) {
            String tid = event.tid;
            
            slices.putIfAbsent(tid, new ArrayList<>());
            
            // Check if thread is entering a core (transition to "Core-X")
            if (event.to.startsWith("Core-")) {
                // Close any previous incomplete slice
                if (sliceStart.containsKey(tid)) {
                    int startTick = sliceStart.get(tid);
                    int endTick = event.tick;
                    String coreId = sliceCore.get(tid);
                    slices.get(tid).add(new GanttSlice(startTick, endTick, coreId));
                }
                // Start new slice
                sliceStart.put(tid, event.tick);
                sliceCore.put(tid, event.to);
            }
            // Check if thread is leaving a core (transition from Core-X to Ready/Waiting/Done)
            else if (event.from.startsWith("Core-") && sliceStart.containsKey(tid)) {
                int startTick = sliceStart.get(tid);
                int endTick = event.tick;
                String coreId = event.from;
                
                slices.get(tid).add(new GanttSlice(startTick, endTick, coreId));
                sliceStart.remove(tid);
                sliceCore.remove(tid);
            }
        }
        
        return slices;
    }

    /**
     * Helper class for Gantt slices
     */
    private static class GanttSlice {
        int startTick;
        int endTick;
        String coreId;
        
        GanttSlice(int startTick, int endTick, String coreId) {
            this.startTick = startTick;
            this.endTick = endTick;
            this.coreId = coreId;
        }
    }

    /**
     * Escape CSV values: wrap in quotes if contains comma/quote/newline, double-escape quotes
     */
    private static String escapeCsvValue(String value) {
        if (value == null) return "";
        
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Convert algorithm enum to short name for CSV
     */
    private static String algoShortName(SchedulingAlgo algo) {
        switch (algo) {
            case FCFS:         return "FCFS";
            case ROUND_ROBIN:  return "RR";
            case SJF:          return "SJF";
            case PRIORITY:     return "Pri";
            default:           return algo.toString();
        }
    }
}
