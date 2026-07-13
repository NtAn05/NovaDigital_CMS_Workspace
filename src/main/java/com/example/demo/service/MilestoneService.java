package com.example.demo.service;

import com.example.demo.dto.MilestoneBroadcastPayload;
import com.example.demo.dto.MilestoneCreateRequest;
import com.example.demo.dto.MilestoneResponse;
import com.example.demo.dto.MilestoneSyncRequest;
import com.example.demo.entity.MilestoneMutationLog;
import com.example.demo.entity.Project;
import com.example.demo.entity.ProjectAssignment;
import com.example.demo.entity.ProjectAssignment.ProjectRole;
import com.example.demo.entity.ProjectMilestone;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.MutationActionType;
import com.example.demo.repository.MilestoneMutationLogRepository;
import com.example.demo.repository.ProjectAssignmentRepository;
import com.example.demo.repository.ProjectMilestoneRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core business logic for the Project Milestone Sync Node (UC-12).
 *
 * Key principles applied:
 * 1. @Transactional on mutating methods: DB save + MutationLog write are atomic.
 *    If the log write fails, the milestone update is also rolled back.
 * 2. Mutation Log is only written when a field value ACTUALLY changes.
 *    This prevents log spam when PM slides the bar back to the same value.
 * 3. SSE broadcast is triggered AFTER the transaction commits successfully,
 *    called via SseBroadcastService which runs @Async in a separate thread.
 */
@Service
public class MilestoneService {

    @Autowired
    private ProjectMilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneMutationLogRepository mutationLogRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SseBroadcastService sseBroadcastService;

    @Autowired
    private ProjectAssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    // ── READ ─────────────────────────────────────────────────────────────────

    /**
     * Returns all milestones for a given project, ordered by creation date.
     *
     * @throws EntityNotFoundException if the project does not exist
     */
    public List<MilestoneResponse> getMilestonesByProject(Long projectId) {
        ensureProjectExists(projectId);
        return milestoneRepository.findByProjectIdOrderByCreatedAtAsc(projectId)
                .stream()
                .map(MilestoneResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Returns the full mutation history for a specific milestone, newest first.
     * Used by the Audit Trail panel on the Admin UI.
     *
     * @throws EntityNotFoundException if the milestone does not exist
     */
    public List<MilestoneMutationLog> getMutationLogs(Long milestoneId) {
        ensureMilestoneExists(milestoneId);
        return mutationLogRepository.findByMilestoneIdOrderByPerformedAtDesc(milestoneId);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new milestone for a project and broadcasts the event.
     *
     * @param projectId   the owning project
     * @param request     validated creation payload
     * @param performedBy username from JWT Security Context (cannot be spoofed)
     * @return the saved milestone as a response DTO
     * @throws EntityNotFoundException  if project not found
     * @throws IllegalArgumentException if a milestone with the same name already exists in the project
     */
    @Transactional
    public MilestoneResponse createMilestone(Long projectId,
                                             MilestoneCreateRequest request,
                                             String performedBy) {
        Project project = findProjectOrThrow(projectId);

        // Authorization: Only the PM assigned to THIS project can create milestones.
        User caller = userRepository.findByUsernameOrEmail(performedBy, performedBy)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + performedBy));

        boolean isPm = assignmentRepository.existsByProjectIdAndUserIdAndProjectRole(
                projectId, caller.getId(), ProjectRole.PM);

        if (!isPm) {
            throw new AccessDeniedException(
                    "Access denied: only the PM assigned to project " + projectId + " can create milestones.");
        }

        // Prevent duplicate milestone names within the same project
        milestoneRepository.findByProjectIdAndName(projectId, request.getName().trim())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "A milestone named '" + request.getName() + "' already exists in this project.");
                });

        ProjectMilestone milestone = new ProjectMilestone();
        milestone.setProject(project);
        milestone.setName(request.getName().trim());
        milestone.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        milestone.setStatus(request.getStatus());
        milestone.setProgressPercentage(request.getProgressPercentage());
        milestone.setDueDate(request.getDueDate());

        ProjectMilestone saved = milestoneRepository.save(milestone);

        // Record a CREATE log entry
        MilestoneMutationLog createLog = new MilestoneMutationLog();
        createLog.setMilestoneId(saved.getId());
        createLog.setProjectId(projectId);
        createLog.setActionType(MutationActionType.CREATE);
        createLog.setFieldName("milestone");
        createLog.setOldValue(null);
        createLog.setNewValue(saved.getName());
        createLog.setPerformedBy(performedBy);
        mutationLogRepository.save(createLog);

        // Broadcast to Layout Nodes asynchronously
        MilestoneResponse response = MilestoneResponse.from(saved);
        sseBroadcastService.broadcast(new MilestoneBroadcastPayload(
                "MILESTONE_CREATED",
                projectId,
                response,
                "New milestone '" + saved.getName() + "' created by " + performedBy,
                LocalDateTime.now()
        ));

        return response;
    }

    // ── SYNC (Core Endpoint) ──────────────────────────────────────────────────

    /**
     * Syncs a milestone's progress and status — the core of UC-12.
     *
     * This method:
     * 1. Loads the existing milestone (404 if not found)
     * 2. Compares old vs new values field-by-field
     * 3. Only updates fields that have actually changed
     * 4. Writes one MilestoneMutationLog row per changed field (granular audit trail)
     * 5. Saves the updated milestone within the same @Transactional boundary
     * 6. Broadcasts the full update payload via SSE (async, does not block response)
     *
     * @param projectId   validated project context
     * @param milestoneId the milestone to update
     * @param request     validated sync payload (progressPercentage + status)
     * @param performedBy username from JWT Security Context
     * @return the updated milestone as a response DTO
     * @throws EntityNotFoundException  if project or milestone not found
     * @throws IllegalArgumentException if milestone does not belong to the specified project
     */
    @Transactional
    public MilestoneResponse syncMilestone(Long projectId,
                                           Long milestoneId,
                                           MilestoneSyncRequest request,
                                           String performedBy) {
        ensureProjectExists(projectId);
        ProjectMilestone milestone = findMilestoneOrThrow(milestoneId);

        // Security: Verify the milestone actually belongs to this project
        if (!milestone.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException(
                    "Milestone " + milestoneId + " does not belong to project " + projectId);
        }

        // Authorization: Only the PM assigned to THIS project can sync milestones.
        // Find the calling user's account and verify their project-level PM assignment.
        User caller = userRepository.findByUsernameOrEmail(performedBy, performedBy)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + performedBy));

        boolean isPm = assignmentRepository.existsByProjectIdAndUserIdAndProjectRole(
                projectId, caller.getId(), ProjectRole.PM);

        if (!isPm) {
            throw new AccessDeniedException(
                    "Access denied: only the PM assigned to project " + projectId + " can sync milestones.");
        }

        List<MilestoneMutationLog> logs = new ArrayList<>();

        // ── Field-level change detection ─────────────────────────────────────
        // Only log and update fields that have actually changed in value.
        // This prevents noise in the Audit Trail when PM re-submits with identical data.

        if (!request.getProgressPercentage().equals(milestone.getProgressPercentage())) {
            logs.add(buildLog(milestoneId, projectId,
                    MutationActionType.SYNC_UPDATE,
                    "progressPercentage",
                    String.valueOf(milestone.getProgressPercentage()),
                    String.valueOf(request.getProgressPercentage()),
                    performedBy));
            milestone.setProgressPercentage(request.getProgressPercentage());
        }

        if (!request.getStatus().equals(milestone.getStatus())) {
            logs.add(buildLog(milestoneId, projectId,
                    MutationActionType.SYNC_UPDATE,
                    "status",
                    milestone.getStatus().name(),
                    request.getStatus().name(),
                    performedBy));
            milestone.setStatus(request.getStatus());
        }

        // If nothing actually changed, return early without persisting anything
        if (logs.isEmpty()) {
            return MilestoneResponse.from(milestone);
        }

        ProjectMilestone saved = milestoneRepository.save(milestone);
        mutationLogRepository.saveAll(logs);

        // Build a human-readable summary of all changes for toast notifications
        String mutationSummary = buildMutationSummary(logs, performedBy);

        // Broadcast asynchronously — does NOT block this HTTP response thread
        MilestoneResponse response = MilestoneResponse.from(saved);
        sseBroadcastService.broadcast(new MilestoneBroadcastPayload(
                "MILESTONE_UPDATED",
                projectId,
                response,
                mutationSummary,
                LocalDateTime.now()
        ));

        return response;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes a milestone and broadcasts the removal event.
     *
     * @throws EntityNotFoundException  if milestone not found
     * @throws IllegalArgumentException if milestone does not belong to the specified project
     */
    @Transactional
    public void deleteMilestone(Long projectId, Long milestoneId, String performedBy) {
        ensureProjectExists(projectId);
        ProjectMilestone milestone = findMilestoneOrThrow(milestoneId);

        if (!milestone.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException(
                    "Milestone " + milestoneId + " does not belong to project " + projectId);
        }

        String milestoneName = milestone.getName();
        milestoneRepository.deleteById(milestoneId);

        // Log the deletion
        MilestoneMutationLog deleteLog = buildLog(milestoneId, projectId,
                MutationActionType.DELETE,
                "milestone", milestoneName, null, performedBy);
        mutationLogRepository.save(deleteLog);

        // Broadcast deletion event (milestone payload is null for DELETE)
        sseBroadcastService.broadcast(new MilestoneBroadcastPayload(
                "MILESTONE_DELETED",
                projectId,
                null,
                "Milestone '" + milestoneName + "' deleted by " + performedBy,
                LocalDateTime.now()
        ));
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
    }

    private ProjectMilestone findMilestoneOrThrow(Long milestoneId) {
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Milestone not found with id: " + milestoneId));
    }

    private void ensureProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }
    }

    private void ensureMilestoneExists(Long milestoneId) {
        if (!milestoneRepository.existsById(milestoneId)) {
            throw new EntityNotFoundException("Milestone not found with id: " + milestoneId);
        }
    }

    private MilestoneMutationLog buildLog(Long milestoneId, Long projectId,
                                          MutationActionType actionType,
                                          String fieldName, String oldValue,
                                          String newValue, String performedBy) {
        MilestoneMutationLog log = new MilestoneMutationLog();
        log.setMilestoneId(milestoneId);
        log.setProjectId(projectId);
        log.setActionType(actionType);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(performedBy);
        return log;
    }

    private String buildMutationSummary(List<MilestoneMutationLog> logs, String performedBy) {
        StringBuilder sb = new StringBuilder();
        for (MilestoneMutationLog log : logs) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(log.getFieldName())
              .append(" changed from ")
              .append(log.getOldValue())
              .append(" to ")
              .append(log.getNewValue());
        }
        sb.append(" — by ").append(performedBy);
        return sb.toString();
    }
}
