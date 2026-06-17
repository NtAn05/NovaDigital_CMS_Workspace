package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        if (file.isEmpty()) {
            response.put("message", "File is empty");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Get file extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Generate unique filename
            String newFilename = UUID.randomUUID().toString() + extension;

            // Define directories
            String relativeUploadDir = "src/main/resources/static/uploads/";
            String classpathUploadDir = "target/classes/static/uploads/";

            // 1. Save to src/main/resources/static/uploads
            Path srcPath = Paths.get(relativeUploadDir).toAbsolutePath().normalize();
            if (!Files.exists(srcPath)) {
                Files.createDirectories(srcPath);
            }
            Path srcFilePath = srcPath.resolve(newFilename);
            Files.copy(file.getInputStream(), srcFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // 2. Save to target/classes/static/uploads if it exists (for immediate availability in dev mode)
            Path targetPath = Paths.get(classpathUploadDir).toAbsolutePath().normalize();
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }
            Path targetFilePath = targetPath.resolve(newFilename);
            Files.copy(srcFilePath, targetFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            response.put("success", true);
            response.put("url", "/uploads/" + newFilename);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("message", "File upload failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
