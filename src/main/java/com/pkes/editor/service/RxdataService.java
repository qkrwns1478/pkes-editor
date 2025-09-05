package com.pkes.editor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jruby.embed.ScriptingContainer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class RxdataService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScriptingContainer jrubyContainer = new ScriptingContainer();

    public Map<String, Object> parseRxdataFile(MultipartFile file) throws IOException {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("upload-", ".rxdata");
            file.transferTo(tempFile);

            jrubyContainer.put("rxdata_file_path", tempFile.toAbsolutePath().toString());

            ClassPathResource scriptResource = new ClassPathResource("scripts/parser.rb");
            try (InputStream scriptStream = scriptResource.getInputStream()) {
                Object result = jrubyContainer.runScriptlet(scriptStream, "parser.rb");

                if (result == null) {
                    throw new IOException("Ruby script returned null.");
                }
                if (!(result instanceof String)) {
                    throw new IOException("Ruby script did not return a String. Returned type: " + result.getClass().getName());
                }

                String resultString = (String) result;

                if (resultString.startsWith("Error:")) {
                    throw new IOException(resultString);
                }

                return objectMapper.readValue(resultString, new TypeReference<>() {});
            }
        } finally {
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    public byte[] createRxdataFromMap(Map<String, Object> jsonData) throws IOException {
        Path tempJsonFile = null;
        Path tempRxdataFile = null;
        try {
            String jsonString = objectMapper.writeValueAsString(jsonData);
            tempJsonFile = Files.createTempFile("data-", ".json");
            Files.writeString(tempJsonFile, jsonString);

            tempRxdataFile = Files.createTempFile("output-", ".rxdata");

            jrubyContainer.put("json_file_path", tempJsonFile.toAbsolutePath().toString());
            jrubyContainer.put("rxdata_output_path", tempRxdataFile.toAbsolutePath().toString());

            ClassPathResource scriptResource = new ClassPathResource("scripts/generator.rb");
            try (InputStream scriptStream = scriptResource.getInputStream()) {
                jrubyContainer.runScriptlet(scriptStream, "generator.rb");
            }

            return Files.readAllBytes(tempRxdataFile);

        } finally {
            if (tempJsonFile != null) {
                Files.deleteIfExists(tempJsonFile);
            }
            if (tempRxdataFile != null) {
                Files.deleteIfExists(tempRxdataFile);
            }
        }
    }
}