package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.entity.ProjectAssignment.ProjectRole;
import com.example.demo.entity.enums.AllocationStatus;
import com.example.demo.repository.*;
import com.example.demo.util.WorkloadCapacityCalculator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ResourceAllocationService {

    private static final List<AllocationStatus> CAPACITY_STATUSES =
            List.of(AllocationStatus.PLANNED, AllocationStatus.ACTIVE);

    @Autowired private ResourceAllocationRepository allocationRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectMilestoneRepository milestoneRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProjectAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public ResourceMatrixResponse getMatrix(Long requestedProjectId,
                                            LocalDate focusDate,
                                            Authentication authentication) {
        User actor = resolveActor(authentication);
        LocalDate effectiveDate = focusDate != null ? focusDate : LocalDate.now();
        List<Project> accessibleProjects = getAccessibleProjects(actor);
        if (!isAdmin(actor) && accessibleProjects.isEmpty()) {
            throw new AccessDeniedException(
                    "Access denied: this account is not assigned as PM on any project.");
        }

        Long selectedProjectId = requestedProjectId;
        if (selectedProjectId == null && !accessibleProjects.isEmpty()) {
            selectedProjectId = accessibleProjects.get(0).getId();
        }
        if (selectedProjectId != null) {
            assertCanManageProject(actor, selectedProjectId);
        }
        final Long selectedId = selectedProjectId;

        Project selectedProject = selectedId == null ? null : projectRepository.findById(selectedId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + selectedId));

        List<User> staffUsers = userRepository.findAll().stream()
                .filter(User::isEnabled)
                .filter(u -> "ROLE_MEMBER".equalsIgnoreCase(u.getRole()))
                .sorted(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<Long, Member> memberProfiles = memberRepository.findAll().stream()
                .filter(m -> m.getUserId() != null)
                .collect(Collectors.toMap(Member::getUserId, Function.identity(), (left, right) -> left));

        List<Long> staffIds = staffUsers.stream().map(User::getId).toList();
        List<ResourceAllocation> focusAllocations = staffIds.isEmpty()
                ? List.of()
                : allocationRepository.findCapacityAllocationsAtDate(staffIds, effectiveDate, CAPACITY_STATUSES);

        Map<Long, Integer> totalWorkloadByUser = focusAllocations.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId(),
                        Collectors.summingInt(ResourceAllocation::getAllocationPercentage)));

        Map<Long, Integer> selectedProjectWorkloadByUser = selectedId == null
                ? Map.of()
                : focusAllocations.stream()
                    .filter(a -> selectedId.equals(a.getProject().getId()))
                    .collect(Collectors.groupingBy(a -> a.getUser().getId(),
                            Collectors.summingInt(ResourceAllocation::getAllocationPercentage)));

        Set<Long> assignedUserIds = selectedId == null
                ? Set.of()
                : assignmentRepository.findByProjectId(selectedId).stream()
                    .map(a -> a.getUser().getId())
                    .collect(Collectors.toSet());

        List<ResourceStaffRow> staffRows = staffUsers.stream().map(user -> {
            Member profile = memberProfiles.get(user.getId());
            List<String> skills = parseCsv(profile != null && hasText(profile.getSkills())
                    ? profile.getSkills() : user.getSkills());
            int workload = totalWorkloadByUser.getOrDefault(user.getId(), 0);
            int projectWorkload = selectedProjectWorkloadByUser.getOrDefault(user.getId(), 0);
            int availability = Math.max(0, 100 - workload);
            int skillMatch = selectedProject == null ? 0
                    : calculateSkillMatch(skills, parseCsv(selectedProject.getTechnologies()));

            return new ResourceStaffRow(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getAvatarUrl(),
                    skills,
                    skillMatch,
                    workload,
                    projectWorkload,
                    availability,
                    workload > 100,
                    assignedUserIds.contains(user.getId())
            );
        }).toList();

        List<ResourceProjectOption> projectOptions = accessibleProjects.stream()
                .map(p -> new ResourceProjectOption(p.getId(), p.getTitle(), p.getCategory(), p.getTechnologies()))
                .toList();

        List<ResourceTaskOption> tasks = selectedId == null ? List.of()
                : milestoneRepository.findByProjectIdOrderByCreatedAtAsc(selectedId).stream()
                    .map(m -> new ResourceTaskOption(m.getId(), m.getName(), m.getStatus().name(),
                            m.getProgressPercentage(), m.getDueDate()))
                    .toList();

        List<ResourceAllocationResponse> allocations = selectedId == null ? List.of()
                : allocationRepository.findByProjectIdOrderByStartDateAscIdAsc(selectedId).stream()
                    .map(this::toResponse)
                    .toList();

        return new ResourceMatrixResponse(
                isAdmin(actor) ? "HR_ADMIN" : "PROJECT_MANAGER",
                actor.getFullName(),
                effectiveDate,
                selectedId,
                projectOptions,
                tasks,
                staffRows,
                allocations
        );
    }

    @Transactional
    public ResourceAllocationResponse create(ResourceAllocationRequest request,
                                             Authentication authentication) {
        User actor = resolveActor(authentication);
        Project project = findProject(request.getProjectId());
        assertCanManageProject(actor, project.getId());

        User staff = findStaff(request.getUserId());
        ProjectMilestone milestone = resolveMilestone(project, request.getMilestoneId());
        AllocationStatus status = parseStatus(request.getStatus());
        validateDates(request.getStartDate(), request.getEndDate());
        validateNoDuplicateAndCapacity(null, staff, project, milestone, status,
                request.getAllocationPercentage(), request.getStartDate(), request.getEndDate());

        ResourceAllocation allocation = new ResourceAllocation();
        applyRequest(allocation, request, project, milestone, staff, status);
        allocation.setAssignedBy(actor.getUsername());

        ensureProjectStaffAssignment(project, staff);
        return toResponse(allocationRepository.save(allocation));
    }

    @Transactional
    public ResourceAllocationResponse update(Long id,
                                             ResourceAllocationRequest request,
                                             Authentication authentication) {
        User actor = resolveActor(authentication);
        ResourceAllocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource allocation not found: " + id));

        // A PM must be authorized for both the existing project and any target project.
        assertCanManageProject(actor, allocation.getProject().getId());
        Project project = findProject(request.getProjectId());
        assertCanManageProject(actor, project.getId());

        User staff = findStaff(request.getUserId());
        ProjectMilestone milestone = resolveMilestone(project, request.getMilestoneId());
        AllocationStatus status = parseStatus(request.getStatus());
        validateDates(request.getStartDate(), request.getEndDate());
        validateNoDuplicateAndCapacity(id, staff, project, milestone, status,
                request.getAllocationPercentage(), request.getStartDate(), request.getEndDate());

        applyRequest(allocation, request, project, milestone, staff, status);
        allocation.setAssignedBy(actor.getUsername());
        ensureProjectStaffAssignment(project, staff);
        return toResponse(allocationRepository.save(allocation));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        User actor = resolveActor(authentication);
        ResourceAllocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource allocation not found: " + id));
        assertCanManageProject(actor, allocation.getProject().getId());
        allocationRepository.delete(allocation);
    }

    private void applyRequest(ResourceAllocation allocation,
                              ResourceAllocationRequest request,
                              Project project,
                              ProjectMilestone milestone,
                              User staff,
                              AllocationStatus status) {
        allocation.setProject(project);
        allocation.setMilestone(milestone);
        allocation.setUser(staff);
        allocation.setAllocationPercentage(request.getAllocationPercentage());
        allocation.setStartDate(request.getStartDate());
        allocation.setEndDate(request.getEndDate());
        allocation.setStatus(status);
        allocation.setNotes(hasText(request.getNotes()) ? request.getNotes().trim() : null);
    }

    private void validateNoDuplicateAndCapacity(Long excludedId,
                                                User staff,
                                                Project project,
                                                ProjectMilestone milestone,
                                                AllocationStatus status,
                                                int percentage,
                                                LocalDate startDate,
                                                LocalDate endDate) {
        if (!consumesCapacity(status)) {
            return;
        }

        List<ResourceAllocation> overlapping = allocationRepository
                .findCapacityAllocationsOverlapping(staff.getId(), startDate, endDate, CAPACITY_STATUSES)
                .stream()
                .filter(a -> excludedId == null || !excludedId.equals(a.getId()))
                .toList();

        boolean duplicate = overlapping.stream().anyMatch(existing ->
                project.getId().equals(existing.getProject().getId())
                        && Objects.equals(milestoneId(milestone), milestoneId(existing.getMilestone())));
        if (duplicate) {
            String target = milestone == null ? "this project-level assignment" : "task '" + milestone.getName() + "'";
            throw new IllegalArgumentException(
                    staff.getFullName() + " already has an overlapping allocation for " + target + ".");
        }

        WorkloadCapacityCalculator.findConflict(overlapping, startDate, endDate, percentage)
                .ifPresent(conflict -> {
                    throw new IllegalArgumentException(
                            "Workload limit exceeded: " + staff.getFullName() + " would reach "
                                    + conflict.workloadPercentage() + "% on " + conflict.date()
                                    + ". Maximum allowed workload is 100%.");
                });
    }

    private void ensureProjectStaffAssignment(Project project, User staff) {
        assignmentRepository.findByProjectIdAndUserId(project.getId(), staff.getId())
                .orElseGet(() -> {
                    ProjectAssignment assignment = new ProjectAssignment();
                    assignment.setProject(project);
                    assignment.setUser(staff);
                    assignment.setProjectRole(ProjectRole.STAFF);
                    return assignmentRepository.save(assignment);
                });
    }

    private ProjectMilestone resolveMilestone(Project project, Long milestoneId) {
        if (milestoneId == null) {
            return null;
        }
        ProjectMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Task/milestone not found: " + milestoneId));
        if (!project.getId().equals(milestone.getProject().getId())) {
            throw new IllegalArgumentException(
                    "Selected task/milestone does not belong to project '" + project.getTitle() + "'.");
        }
        return milestone;
    }

    private User findStaff(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Staff member not found: " + userId));
        if (!"ROLE_MEMBER".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException("Only internal users with ROLE_MEMBER can be allocated.");
        }
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("The selected staff account is disabled.");
        }
        return user;
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
    }

    private User resolveActor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required.");
        }
        String identity = authentication.getName();
        User actor = userRepository.findByUsernameOrEmail(identity, identity)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user was not found."));
        if (!actor.isEnabled()) {
            throw new AccessDeniedException("Your account is disabled.");
        }
        if (!isAdmin(actor) && !"ROLE_MEMBER".equalsIgnoreCase(actor.getRole())) {
            throw new AccessDeniedException("Only HR/Admin or an assigned Project Manager can manage resources.");
        }
        return actor;
    }

    private List<Project> getAccessibleProjects(User actor) {
        if (isAdmin(actor)) {
            return projectRepository.findAll().stream()
                    .sorted(Comparator.comparing(Project::getTitle, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }
        return assignmentRepository.findByUserIdAndProjectRole(actor.getId(), ProjectRole.PM).stream()
                .map(ProjectAssignment::getProject)
                .sorted(Comparator.comparing(Project::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void assertCanManageProject(User actor, Long projectId) {
        if (isAdmin(actor)) {
            return;
        }
        boolean isProjectManager = assignmentRepository
                .existsByProjectIdAndUserIdAndProjectRole(projectId, actor.getId(), ProjectRole.PM);
        if (!isProjectManager) {
            throw new AccessDeniedException(
                    "Access denied: you can only manage resources for projects where you are assigned as PM.");
        }
    }

    private AllocationStatus parseStatus(String rawStatus) {
        try {
            return AllocationStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid allocation status. Use PLANNED, ACTIVE, COMPLETED, or CANCELLED.");
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date.");
        }
    }

    private boolean consumesCapacity(AllocationStatus status) {
        return CAPACITY_STATUSES.contains(status);
    }

    private Long milestoneId(ProjectMilestone milestone) {
        return milestone == null ? null : milestone.getId();
    }

    private boolean isAdmin(User user) {
        return "ROLE_ADMIN".equalsIgnoreCase(user.getRole());
    }

    private int calculateSkillMatch(List<String> staffSkills, List<String> requiredTechnologies) {
        if (requiredTechnologies.isEmpty()) {
            return 0;
        }
        Set<String> normalizedSkills = staffSkills.stream()
                .map(this::normalizeSkill)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
        long matches = requiredTechnologies.stream()
                .map(this::normalizeSkill)
                .filter(normalizedSkills::contains)
                .count();
        return (int) Math.round(matches * 100.0 / requiredTechnologies.size());
    }

    private String normalizeSkill(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#.]", "");
    }

    private List<String> parseCsv(String csv) {
        if (!hasText(csv)) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(this::hasText)
                .distinct()
                .toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ResourceAllocationResponse toResponse(ResourceAllocation allocation) {
        return new ResourceAllocationResponse(
                allocation.getId(),
                allocation.getProject().getId(),
                allocation.getProject().getTitle(),
                allocation.getMilestone() == null ? null : allocation.getMilestone().getId(),
                allocation.getMilestone() == null ? null : allocation.getMilestone().getName(),
                allocation.getUser().getId(),
                allocation.getUser().getUsername(),
                allocation.getUser().getFullName(),
                allocation.getAllocationPercentage(),
                allocation.getStartDate(),
                allocation.getEndDate(),
                allocation.getStatus().name(),
                allocation.getNotes(),
                allocation.getAssignedBy(),
                allocation.getCreatedAt(),
                allocation.getUpdatedAt()
        );
    }
}
