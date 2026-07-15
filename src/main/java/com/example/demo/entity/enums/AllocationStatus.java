package com.example.demo.entity.enums;

/**
 * Lifecycle of a staff resource allocation.
 * PLANNED and ACTIVE consume capacity; COMPLETED and CANCELLED do not.
 */
public enum AllocationStatus {
    PLANNED,
    ACTIVE,
    COMPLETED,
    CANCELLED
}
