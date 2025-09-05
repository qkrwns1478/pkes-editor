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
            InputStream scriptStream = scriptResource.getInputStream();

            Object result = jrubyContainer.runScriptlet(scriptStream, "parser.rb");

            if (result == null) {
                throw new IOException("Ruby script returned null. This can happen if the script is empty or ends with a 'puts' statement.");
            }
            if (!(result instanceof String)) {
                throw new IOException("Ruby script did not return a String. Returned type: " + result.getClass().getName());
            }

            String resultString = (String) result;

            if (resultString.startsWith("Error:")) {
                throw new IOException(resultString);
            }

            return objectMapper.readValue(resultString, new TypeReference<>() {});

        } finally {
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
        }
    }
}