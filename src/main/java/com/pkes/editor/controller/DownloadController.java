package com.pkes.editor.controller;

import com.pkes.editor.service.RxdataService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DownloadController {

    private final RxdataService rxdataService;

    public DownloadController(RxdataService rxdataService) {
        this.rxdataService = rxdataService;
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> handleRxdataDownload(@RequestBody Map<String, Object> jsonDataWrapper) {
        if (jsonDataWrapper == null || !jsonDataWrapper.containsKey("data")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> actualSaveData = (Map<String, Object>) jsonDataWrapper.get("data");

            byte[] rxdataBytes = rxdataService.createRxdataFromMap(actualSaveData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "edited_save.rxdata");

            return new ResponseEntity<>(rxdataBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}