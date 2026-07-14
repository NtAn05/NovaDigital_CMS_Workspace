package com.example.demo.controller;

import com.example.demo.dto.MilestoneCreateRequest;
import com.example.demo.dto.MilestoneSyncRequest;
import com.example.demo.entity.MilestoneMutationLog;
import com.example.demo.entity.Project;
import com.example.demo.entity.ProjectMilestone;
import com.example.demo.entity.enums.MilestoneStatus;
import com.example.demo.entity.enums.MutationActionType;
import com.example.demo.repository.MilestoneMutationLogRepository;
import com.example.demo.repository.ProjectMilestoneRepository;
import com.example.demo.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Integration tests for ProjectMilestoneController.
 * Verifies validation rules, sync behavior, authentication controls,
 * and data mutation logging for UC-12.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProjectMilestoneControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneMutationLogRepository mutationLogRepository;

    @Autowired
    private com.example.demo.repository.UserRepository userRepository;

    @Autowired
    private com.example.demo.repository.ProjectAssignmentRepository assignmentRepository;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private Project testProject;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clear repositories to ensure test isolation
        mutationLogRepository.deleteAll();
        milestoneRepository.deleteAll();
        assignmentRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // Create and persist a parent project
        Project project = new Project();
        project.setTitle("NovaDigital Web App");
        project.setDescription("Enterprise CMS System development");
        project.setCategory("Web Application");
        project.setTechnologies("Spring Boot, Hibernate, MySQL, JavaScript");
        testProject = projectRepository.save(project);
    }

    private void assignProjectPm(String username) {
        com.example.demo.entity.User user = new com.example.demo.entity.User();
        user.setUsername(username);
        user.setPassword("password");
        user.setEmail(username);
        user.setFullName("Test User " + username);
        user.setRole("ROLE_MEMBER");
        user.setEnabled(true);
        user = userRepository.save(user);

        com.example.demo.entity.ProjectAssignment assignment = new com.example.demo.entity.ProjectAssignment();
        assignment.setProject(testProject);
        assignment.setUser(user);
        assignment.setProjectRole(com.example.demo.entity.ProjectAssignment.ProjectRole.PM);
        assignmentRepository.save(assignment);
    }

    // ── 1. GET ALL MILESTONES (PUBLIC ACCESS) ───────────────────────────────

    @Test
    public void getMilestones_ShouldReturnList_WhenProjectExists() throws Exception {
        // Prepare data
        ProjectMilestone m1 = new ProjectMilestone();
        m1.setProject(testProject);
        m1.setName("Milestone 1");
        m1.setStatus(MilestoneStatus.PENDING);
        m1.setProgressPercentage(0);
        milestoneRepository.save(m1);

        // Execute & Assert
        mockMvc.perform(get("/api/projects/{projectId}/milestones", testProject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Milestone 1")))
                .andExpect(jsonPath("$[0].progressPercentage", is(0)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    public void getMilestones_ShouldReturn404_WhenProjectDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/projects/9999/milestones"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    // ── 2. CREATE MILESTONE (ROLE PROTECTED) ────────────────────────────────

    @Test
    @WithMockUser(username = "pm@novadigital.vn", roles = {"MEMBER"})
    public void createMilestone_ShouldSucceed_WhenAuthorized() throws Exception {
        assignProjectPm("pm@novadigital.vn");
        MilestoneCreateRequest req = new MilestoneCreateRequest();
        req.setName("Database Schema Design");
        req.setDescription("Design relational schemas and write migrations");
        req.setStatus(MilestoneStatus.IN_PROGRESS);
        req.setProgressPercentage(20);
        req.setDueDate(LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/projects/{projectId}/milestones", testProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Database Schema Design")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.progressPercentage", is(20)));

        // Verify Mutation Log was written
        List<MilestoneMutationLog> logs = mutationLogRepository.findByProjectIdOrderByPerformedAtDesc(testProject.getId());
        assertThat(logs, hasSize(1));
        assertThat(logs.get(0).getActionType(), is(MutationActionType.CREATE));
        assertThat(logs.get(0).getPerformedBy(), is("pm@novadigital.vn"));
    }

    @Test
    public void createMilestone_ShouldReturn403_WhenAnonymous() throws Exception {
        MilestoneCreateRequest req = new MilestoneCreateRequest();
        req.setName("Unauthorized Milestone");

        mockMvc.perform(post("/api/projects/{projectId}/milestones", testProject.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── 3. SYNC MILESTONE (THE CORE UC-12 BUSINESS SCENARIO) ─────────────────

    @Test
    @WithMockUser(username = "lead-dev@novadigital.vn", roles = {"MEMBER"})
    public void syncMilestone_ShouldUpdateFieldsAndLogMutations_WhenValid() throws Exception {
        assignProjectPm("lead-dev@novadigital.vn");
        // Save initial milestone state
        ProjectMilestone milestone = new ProjectMilestone();
        milestone.setProject(testProject);
        milestone.setName("Phase 2: Core Coding");
        milestone.setStatus(MilestoneStatus.IN_PROGRESS);
        milestone.setProgressPercentage(30); // Old value
        ProjectMilestone savedMilestone = milestoneRepository.save(milestone);

        // Request update
        MilestoneSyncRequest syncReq = new MilestoneSyncRequest();
        syncReq.setStatus(MilestoneStatus.COMPLETED); // changed
        syncReq.setProgressPercentage(100); // changed

        // Execute sync
        mockMvc.perform(put("/api/projects/{projectId}/milestones/{id}/sync", testProject.getId(), savedMilestone.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncReq))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPercentage", is(100)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        // Verify Database state
        ProjectMilestone updated = milestoneRepository.findById(savedMilestone.getId()).orElseThrow();
        assertThat(updated.getProgressPercentage(), is(100));
        assertThat(updated.getStatus(), is(MilestoneStatus.COMPLETED));

        // Verify two separate mutation log entries were created (one for progress, one for status)
        List<MilestoneMutationLog> logs = mutationLogRepository.findByMilestoneIdOrderByPerformedAtDesc(savedMilestone.getId());
        assertThat(logs, hasSize(2));

        MilestoneMutationLog progressLog = logs.stream().filter(l -> "progressPercentage".equals(l.getFieldName())).findFirst().orElseThrow();
        assertThat(progressLog.getOldValue(), is("30"));
        assertThat(progressLog.getNewValue(), is("100"));
        assertThat(progressLog.getActionType(), is(MutationActionType.SYNC_UPDATE));
        assertThat(progressLog.getPerformedBy(), is("lead-dev@novadigital.vn"));

        MilestoneMutationLog statusLog = logs.stream().filter(l -> "status".equals(l.getFieldName())).findFirst().orElseThrow();
        assertThat(statusLog.getOldValue(), is("IN_PROGRESS"));
        assertThat(statusLog.getNewValue(), is("COMPLETED"));
        assertThat(statusLog.getActionType(), is(MutationActionType.SYNC_UPDATE));
        assertThat(statusLog.getPerformedBy(), is("lead-dev@novadigital.vn"));
    }

    // ── 4. INPUT VALIDATION (BOUNDARY CONDITIONS) ───────────────────────────

    @Test
    @WithMockUser(username = "pm@novadigital.vn", roles = {"MEMBER"})
    public void syncMilestone_ShouldReturn400_WhenProgressExceeds100() throws Exception {
        assignProjectPm("pm@novadigital.vn");
        ProjectMilestone milestone = new ProjectMilestone();
        milestone.setProject(testProject);
        milestone.setName("Figma UX Designs");
        ProjectMilestone saved = milestoneRepository.save(milestone);

        MilestoneSyncRequest syncReq = new MilestoneSyncRequest();
        syncReq.setStatus(MilestoneStatus.IN_PROGRESS);
        syncReq.setProgressPercentage(101); // Exceeds upper bound

        mockMvc.perform(put("/api/projects/{projectId}/milestones/{id}/sync", testProject.getId(), saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncReq))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "pm@novadigital.vn", roles = {"MEMBER"})
    public void syncMilestone_ShouldReturn400_WhenProgressLessThan0() throws Exception {
        assignProjectPm("pm@novadigital.vn");
        ProjectMilestone milestone = new ProjectMilestone();
        milestone.setProject(testProject);
        milestone.setName("Figma UX Designs");
        ProjectMilestone saved = milestoneRepository.save(milestone);

        MilestoneSyncRequest syncReq = new MilestoneSyncRequest();
        syncReq.setStatus(MilestoneStatus.IN_PROGRESS);
        syncReq.setProgressPercentage(-5); // Exceeds lower bound

        mockMvc.perform(put("/api/projects/{projectId}/milestones/{id}/sync", testProject.getId(), saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncReq))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ── 5. DELETE MILESTONE (ADMIN ONLY) ─────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@novadigital.vn", roles = {"ADMIN"})
    public void deleteMilestone_ShouldSucceed_WhenUserIsAdmin() throws Exception {
        ProjectMilestone m = new ProjectMilestone();
        m.setProject(testProject);
        m.setName("Milestone to Delete");
        ProjectMilestone saved = milestoneRepository.save(m);

        mockMvc.perform(delete("/api/projects/{projectId}/milestones/{id}", testProject.getId(), saved.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        assertThat(milestoneRepository.existsById(saved.getId()), is(false));

        // Check log
        List<MilestoneMutationLog> logs = mutationLogRepository.findByProjectIdOrderByPerformedAtDesc(testProject.getId());
        assertThat(logs, hasSize(1));
        assertThat(logs.get(0).getActionType(), is(MutationActionType.DELETE));
        assertThat(logs.get(0).getFieldName(), is("milestone"));
        assertThat(logs.get(0).getOldValue(), is("Milestone to Delete"));
    }

    @Test
    @WithMockUser(username = "member@novadigital.vn", roles = {"MEMBER"})
    public void deleteMilestone_ShouldReturn403_WhenUserIsMember() throws Exception {
        ProjectMilestone m = new ProjectMilestone();
        m.setProject(testProject);
        m.setName("Milestone to Delete");
        ProjectMilestone saved = milestoneRepository.save(m);

        // Members are forbidden to DELETE milestones (only Admins can do it)
        mockMvc.perform(delete("/api/projects/{projectId}/milestones/{id}", testProject.getId(), saved.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "non-assigned-member@novadigital.vn", roles = {"MEMBER"})
    public void syncMilestone_ShouldReturn403_WhenUserIsNotProjectPm() throws Exception {
        // Create user in DB but do not assign them as PM
        com.example.demo.entity.User user = new com.example.demo.entity.User();
        user.setUsername("non-assigned-member@novadigital.vn");
        user.setPassword("password");
        user.setEmail("non-assigned-member@novadigital.vn");
        user.setFullName("Non Assigned Member");
        user.setRole("ROLE_MEMBER");
        user.setEnabled(true);
        userRepository.save(user);

        ProjectMilestone milestone = new ProjectMilestone();
        milestone.setProject(testProject);
        milestone.setName("Figma UX Designs");
        ProjectMilestone saved = milestoneRepository.save(milestone);

        MilestoneSyncRequest syncReq = new MilestoneSyncRequest();
        syncReq.setStatus(MilestoneStatus.IN_PROGRESS);
        syncReq.setProgressPercentage(50);

        mockMvc.perform(put("/api/projects/{projectId}/milestones/{id}/sync", testProject.getId(), saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncReq))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
