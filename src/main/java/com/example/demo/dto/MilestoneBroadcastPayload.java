package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The payload object pushed to all subscribed Layout Nodes via SSE
 * whenever a milestone is mutated (created, synced, or deleted).
 *
 * Frontend EventSource handlers parse this JSON to update the DOM
 * in real time without requiring a page reload.
 *
 * Example SSE event received on the client:
 * <pre>
 * data: {
 *   "eventType": "MILESTONE_UPDATED",
 *   "projectId": 3,
 *   "milestone": { ... MilestoneResponse fields ... },
 *   "mutationSummary": "progressPercentage changed from 50 to 70 by admin@nova.vn",
 *   "broadcastAt": "2026-07-09T15:30:00"
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneBroadcastPayload {

    /** Event type discriminator — allows frontend to handle different event kinds */
    private String eventType; // "MILESTONE_CREATED" | "MILESTONE_UPDATED" | "MILESTONE_DELETED"

    /** Allows Layout Nodes to filter events relevant to their displayed project */
    private Long projectId;

    /** Full updated milestone snapshot (null for DELETE events) */
    private MilestoneResponse milestone;

    /** Human-readable summary of what changed — shown as toast notification on UI */
    private String mutationSummary;

    /** Server-side timestamp when this event was broadcast */
    private LocalDateTime broadcastAt;
}
