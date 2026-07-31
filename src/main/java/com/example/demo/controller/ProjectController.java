package com.example.demo.controller;

import com.example.demo.dto.ProjectResponse;
import com.example.demo.entity.Project;
import com.example.demo.entity.ProjectClient;
import com.example.demo.entity.User;
import com.example.demo.repository.ProjectClientRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProjectService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.demo.repository.ProjectMilestoneRepository milestoneRepository;

    @Autowired
    private com.example.demo.repository.ProjectAssignmentRepository assignmentRepository;

    @Autowired
    private com.example.demo.repository.ResourceAllocationRepository resourceAllocationRepository;

    @Autowired
    private com.example.demo.repository.QuotationRepository quotationRepository;

    @Autowired
    private com.example.demo.repository.PaymentTransactionRepository paymentTransactionRepository;


    // ── GET ALL ──────────────────────────────────────────
    @GetMapping
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects().stream()
                .map(p -> {
                    ProjectResponse resp = new ProjectResponse(p.getId(), p.getTitle(), p.getDescription(),
                            p.getCategory(), p.getImageUrl(), p.getTechnologies());
                    clientRepository.findByProjectId(p.getId()).stream().findFirst().ifPresent(pc -> {
                        resp.setClientId(pc.getUser().getId());
                        resp.setClientName(pc.getUser().getFullName() != null && !pc.getUser().getFullName().isBlank() ? pc.getUser().getFullName() : pc.getUser().getUsername());
                        resp.setClientEmail(pc.getUser().getEmail());
                    });
                    resp.setDepositAmount(p.getDepositAmount());
                    resp.setDepositPaid(p.getDepositPaid());
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        Optional<Project> optional = projectRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Project not found"));
        }
        Project p = optional.get();
        ProjectResponse resp = new ProjectResponse(p.getId(), p.getTitle(), p.getDescription(),
                p.getCategory(), p.getImageUrl(), p.getTechnologies());
        clientRepository.findByProjectId(p.getId()).stream().findFirst().ifPresent(pc -> {
            resp.setClientId(pc.getUser().getId());
            resp.setClientName(pc.getUser().getFullName() != null && !pc.getUser().getFullName().isBlank() ? pc.getUser().getFullName() : pc.getUser().getUsername());
            resp.setClientEmail(pc.getUser().getEmail());
        });
        resp.setDepositAmount(p.getDepositAmount());
        resp.setDepositPaid(p.getDepositPaid());
        return ResponseEntity.ok(resp);
    }

    // ── CREATE ───────────────────────────────────────────
    @PostMapping
    @Transactional
    public ResponseEntity<?> createProject(@RequestBody Map<String, Object> body) {
        Map<String, Object> error = new HashMap<>();
        String title = (String) body.get("title");
        String category = (String) body.get("category");
        String description = (String) body.get("description");
        String imageUrl = (String) body.get("imageUrl");
        String technologies = (String) body.get("technologies");

        if (title == null || title.isBlank()) {
            error.put("message", "Project's name cannot empty");
            return ResponseEntity.badRequest().body(error);
        }
        if (category == null || category.isBlank()) {
            error.put("message", "The category cannot be left blank.");
            return ResponseEntity.badRequest().body(error);
        }

        Project project = new Project();
        project.setTitle(title.trim());
        project.setDescription(description != null ? description.trim() : "");
        project.setCategory(category.trim());
        project.setImageUrl(imageUrl);
        project.setTechnologies(technologies != null ? technologies.trim() : "");
        
        Double depositAmount = parseDouble(body.get("depositAmount"));
        project.setDepositAmount(depositAmount != null ? depositAmount : 0.0);
        project.setDepositPaid(false);

        Project saved;
        try {
            saved = projectRepository.save(project);
        } catch (Exception e) {
            if (imageUrl != null && imageUrl.length() > 255) {
                project.setImageUrl(imageUrl.substring(0, 255));
                saved = projectRepository.save(project);
            } else {
                throw e;
            }
        }

        Long clientId = parseLong(body.get("clientId"));
        String clientName = null;
        String clientEmail = null;
        if (clientId != null && clientId > 0) {
            Optional<User> clientUserOpt = userRepository.findById(clientId);
            if (clientUserOpt.isPresent()) {
                User clientUser = clientUserOpt.get();
                ProjectClient link = new ProjectClient();
                link.setProject(saved);
                link.setUser(clientUser);
                clientRepository.save(link);

                clientName = clientUser.getFullName() != null && !clientUser.getFullName().isBlank() ? clientUser.getFullName() : clientUser.getUsername();
                clientEmail = clientUser.getEmail();
            }
        }

        ProjectResponse resp = new ProjectResponse(saved.getId(), saved.getTitle(), saved.getDescription(),
                saved.getCategory(), saved.getImageUrl(), saved.getTechnologies(), clientId, clientName, clientEmail,
                saved.getDepositAmount(), saved.getDepositPaid());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // ── UPDATE ───────────────────────────────────────────
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> error = new HashMap<>();
        Optional<Project> optional = projectRepository.findById(id);
        if (optional.isEmpty()) {
            error.put("message", "Project with id = not found " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Project project = optional.get();
        if (body.containsKey("title") && body.get("title") != null) {
            String title = (String) body.get("title");
            if (!title.isBlank()) project.setTitle(title.trim());
        }
        if (body.containsKey("description") && body.get("description") != null) {
            project.setDescription(((String) body.get("description")).trim());
        }
        if (body.containsKey("category") && body.get("category") != null) {
            String category = (String) body.get("category");
            if (!category.isBlank()) project.setCategory(category.trim());
        }
        if (body.containsKey("imageUrl")) {
            project.setImageUrl((String) body.get("imageUrl"));
        }
        if (body.containsKey("technologies") && body.get("technologies") != null) {
            project.setTechnologies(((String) body.get("technologies")).trim());
        }
        if (body.containsKey("depositAmount")) {
            project.setDepositAmount(parseDouble(body.get("depositAmount")));
        }
        if (body.containsKey("depositPaid")) {
            project.setDepositPaid(Boolean.valueOf(body.get("depositPaid").toString()));
        }

        Project saved = projectRepository.save(project);

        Long clientId = null;
        String clientName = null;
        String clientEmail = null;

        if (body.containsKey("clientId")) {
            clientId = parseLong(body.get("clientId"));
            
            Optional<ProjectClient> existing = clientRepository.findByProjectId(saved.getId()).stream().findFirst();
            boolean needsUpdate = true;
            if (existing.isPresent()) {
                if (existing.get().getUser().getId().equals(clientId)) {
                    needsUpdate = false;
                    User clientUser = existing.get().getUser();
                    clientName = clientUser.getFullName() != null && !clientUser.getFullName().isBlank() ? clientUser.getFullName() : clientUser.getUsername();
                    clientEmail = clientUser.getEmail();
                }
            }
            
            if (needsUpdate) {
                clientRepository.deleteByProjectId(saved.getId());
                clientRepository.flush();
                if (clientId != null && clientId > 0) {
                    Optional<User> clientUserOpt = userRepository.findById(clientId);
                    if (clientUserOpt.isPresent()) {
                        User clientUser = clientUserOpt.get();
                        ProjectClient link = new ProjectClient();
                        link.setProject(saved);
                        link.setUser(clientUser);
                        clientRepository.save(link);
                        clientRepository.flush();

                        clientName = clientUser.getFullName() != null && !clientUser.getFullName().isBlank() ? clientUser.getFullName() : clientUser.getUsername();
                        clientEmail = clientUser.getEmail();
                    }
                }
            }
        } else {
            Optional<ProjectClient> existing = clientRepository.findByProjectId(saved.getId()).stream().findFirst();
            if (existing.isPresent()) {
                User clientUser = existing.get().getUser();
                clientId = clientUser.getId();
                clientName = clientUser.getFullName() != null && !clientUser.getFullName().isBlank() ? clientUser.getFullName() : clientUser.getUsername();
                clientEmail = clientUser.getEmail();
            }
        }

        ProjectResponse resp = new ProjectResponse(saved.getId(), saved.getTitle(), saved.getDescription(),
                saved.getCategory(), saved.getImageUrl(), saved.getTechnologies(), clientId, clientName, clientEmail,
                saved.getDepositAmount(), saved.getDepositPaid());
        return ResponseEntity.ok(resp);
    }

    // ── DELETE ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        if (!projectRepository.existsById(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Project with id = not found " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // Clean up child tables to prevent foreign key constraint violations
        milestoneRepository.deleteAllByProjectId(id);
        assignmentRepository.deleteByProjectId(id);
        resourceAllocationRepository.deleteByProjectId(id);
        clientRepository.deleteByProjectId(id);

        quotationRepository.findByConvertedProjectId(id).ifPresent(q -> {
            q.setConvertedProject(null);
            quotationRepository.save(q);
        });

        paymentTransactionRepository.findByProjectId(id).forEach(pt -> {
            pt.setProjectId(null);
            paymentTransactionRepository.save(pt);
        });

        projectRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Project deleted successfully.");
        return ResponseEntity.ok(result);
    }


    private Long parseLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            String str = val.toString().trim();
            if (str.isEmpty()) return null;
            return Long.parseLong(str);
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try {
            String str = val.toString().trim();
            if (str.isEmpty()) return null;
            return Double.parseDouble(str);
        } catch (Exception e) {
            return null;
        }
    }
}

