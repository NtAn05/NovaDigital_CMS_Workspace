package com.example.demo.util;

import com.example.demo.entity.ResourceAllocation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkloadCapacityCalculatorTest {

    @Test
    void detectsWorkloadAboveOneHundredPercent() {
        ResourceAllocation existing = allocation("2026-07-01", "2026-07-31", 70);

        var conflict = WorkloadCapacityCalculator.findConflict(
                List.of(existing),
                LocalDate.parse("2026-07-10"),
                LocalDate.parse("2026-07-20"),
                40);

        assertTrue(conflict.isPresent());
        assertEquals(110, conflict.get().workloadPercentage());
        assertEquals(LocalDate.parse("2026-07-10"), conflict.get().date());
    }

    @Test
    void acceptsExactlyOneHundredPercent() {
        ResourceAllocation existing = allocation("2026-07-01", "2026-07-31", 60);

        var conflict = WorkloadCapacityCalculator.findConflict(
                List.of(existing),
                LocalDate.parse("2026-07-10"),
                LocalDate.parse("2026-07-20"),
                40);

        assertTrue(conflict.isEmpty());
    }

    @Test
    void ignoresNonOverlappingWindows() {
        ResourceAllocation existing = allocation("2026-06-01", "2026-06-30", 100);

        var conflict = WorkloadCapacityCalculator.findConflict(
                List.of(existing),
                LocalDate.parse("2026-07-01"),
                LocalDate.parse("2026-07-31"),
                100);

        assertTrue(conflict.isEmpty());
    }

    private ResourceAllocation allocation(String start, String end, int percentage) {
        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setStartDate(LocalDate.parse(start));
        allocation.setEndDate(LocalDate.parse(end));
        allocation.setAllocationPercentage(percentage);
        return allocation;
    }
}
