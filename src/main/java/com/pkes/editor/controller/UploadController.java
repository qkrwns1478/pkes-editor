package com.pkes.editor.controller;

import com.pkes.editor.service.RxdataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final RxdataService rxdataService;

    public UploadController(RxdataService rxdataService) {
        this.rxdataService = rxdataService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Choose a file to upload."));
        }

        if (!file.getOriginalFilename().endsWith(".rxdata")) {
            return ResponseEntity.badRequest().body(Map.of("error", "You can upload only rxdata file."));
        }

        try {
            Map<String, Object> jsonData = rxdataService.parseRxdataFile(file);
            String fileId = UUID.randomUUID().toString();
            Map<String, Object> responseBody = Map.of(
                    "data", jsonData,
                    "fileId", fileId
            );
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error occurred when processing file: " + e.getMessage()));
        }
    }
}