package com.example.demo.entity.enums;

/**
 * Classifies the type of data mutation that was performed on a Milestone.
 * Used in MilestoneMutationLog for Audit Trail categorization.
 */
public enum MutationActionType {
    CREATE,         // New milestone created
    SYNC_UPDATE,    // Updated via Sync Endpoint (dragged progress bar by PM)
    MANUAL_UPDATE,  // Manual edit on Admin UI
    DELETE          // Milestone deleted
}
