package com.example.demo.controller;

import com.example.demo.dto.ProjectResponse;
import com.example.demo.entity.Project;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.service.ProjectService;
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

    // ── GET ALL ──────────────────────────────────────────
    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects().stream()
                .map(p -> new ProjectResponse(p.getId(), p.getTitle(), p.getDescription(),
                        p.getCategory(), p.getImageUrl(), p.getTechnologies()))
                .collect(Collectors.toList());
    }

    // ── CREATE ───────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Project request) {
        Map<String, Object> error = new HashMap<>();
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            error.put("message", "Project's name cannot empty");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            error.put("message", "The category cannot be left blank.");
            return ResponseEntity.badRequest().body(error);
        }

        Project project = new Project();
        project.setTitle(request.getTitle().trim());
        project.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        project.setCategory(request.getCategory().trim());
        project.setImageUrl(request.getImageUrl());
        project.setTechnologies(request.getTechnologies() != null ? request.getTechnologies().trim() : "");

        Project saved = projectRepository.save(project);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProjectResponse(saved.getId(), saved.getTitle(), saved.getDescription(),
                        saved.getCategory(), saved.getImageUrl(), saved.getTechnologies()));
    }

    // ── UPDATE ───────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Project request) {
        Map<String, Object> error = new HashMap<>();
        Optional<Project> optional = projectRepository.findById(id);
        if (optional.isEmpty()) {
            error.put("message", "Project with id = not found " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Project project = optional.get();
        if (request.getTitle()       != null && !request.getTitle().isBlank())    project.setTitle(request.getTitle().trim());
        if (request.getDescription() != null && !request.getDescription().isBlank()) project.setDescription(request.getDescription().trim());
        if (request.getCategory()    != null && !request.getCategory().isBlank()) project.setCategory(request.getCategory().trim());
        if (request.getImageUrl()    != null) project.setImageUrl(request.getImageUrl());
        if (request.getTechnologies() != null) project.setTechnologies(request.getTechnologies().trim());

        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(new ProjectResponse(saved.getId(), saved.getTitle(), saved.getDescription(),
                saved.getCategory(), saved.getImageUrl(), saved.getTechnologies()));
    }

    // ── DELETE ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        if (!projectRepository.existsById(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Project with id = not found " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        projectRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Project deleted successfully.");
        return ResponseEntity.ok(result);
    }
}
