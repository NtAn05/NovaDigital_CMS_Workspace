package com.example.demo.controller;

import com.example.demo.dto.ResourceAllocationRequest;
import com.example.demo.dto.ResourceAllocationResponse;
import com.example.demo.dto.ResourceMatrixResponse;
import com.example.demo.service.ResourceAllocationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** REST API for UC-14 Staff Resource Allocation Matrix. */
@RestController
@RequestMapping("/api/resource-allocations")
public class ResourceAllocationController {

    @Autowired
    private ResourceAllocationService resourceAllocationService;

    @GetMapping("/matrix")
    public ResponseEntity<?> getMatrix(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) LocalDate focusDate,
            Authentication authentication) {
        try {
            ResourceMatrixResponse response = resourceAllocationService
                    .getMatrix(projectId, focusDate, authentication);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            return error(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ResourceAllocationRequest request,
                                    Authentication authentication) {
        try {
            ResourceAllocationResponse created = resourceAllocationService.create(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (EntityNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            return error(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody ResourceAllocationRequest request,
                                    Authentication authentication) {
        try {
            return ResponseEntity.ok(resourceAllocationService.update(id, request, authentication));
        } catch (EntityNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            return error(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        try {
            resourceAllocationService.delete(id, authentication);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", true);
            body.put("message", "Resource allocation deleted successfully.");
            return ResponseEntity.ok(body);
        } catch (EntityNotFoundException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            return error(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Validation Failed");
        body.put("message", fieldErrors.values().stream().findFirst().orElse("Invalid request."));
        body.put("fields", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
