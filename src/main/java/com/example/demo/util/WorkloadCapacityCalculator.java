package com.example.demo.util;

import com.example.demo.entity.ResourceAllocation;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Date-range sweep used by UC-14 to prevent staff workload from exceeding 100%. */
public final class WorkloadCapacityCalculator {

    private WorkloadCapacityCalculator() {
    }

    public record CapacityConflict(LocalDate date, int workloadPercentage) {
    }

    public static Optional<CapacityConflict> findConflict(
            Iterable<ResourceAllocation> existing,
            LocalDate newStart,
            LocalDate newEnd,
            int newPercentage) {

        Map<LocalDate, Integer> events = new TreeMap<>();
        addWindow(events, newStart, newEnd, newPercentage);

        for (ResourceAllocation allocation : existing) {
            addWindow(events,
                    allocation.getStartDate(),
                    allocation.getEndDate(),
                    allocation.getAllocationPercentage());
        }

        int running = 0;
        for (Map.Entry<LocalDate, Integer> event : events.entrySet()) {
            running += event.getValue();
            LocalDate date = event.getKey();
            if (!date.isBefore(newStart) && !date.isAfter(newEnd) && running > 100) {
                return Optional.of(new CapacityConflict(date, running));
            }
        }
        return Optional.empty();
    }

    private static void addWindow(Map<LocalDate, Integer> events,
                                  LocalDate start,
                                  LocalDate end,
                                  int percentage) {
        events.merge(start, percentage, Integer::sum);
        events.merge(end.plusDays(1), -percentage, Integer::sum);
    }
}
