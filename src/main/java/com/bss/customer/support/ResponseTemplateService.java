package com.bss.customer.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bss.customer.common.ApiException;

@Service
public class ResponseTemplateService {

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-]{1,50}\\.txt");

    private final Path templateDir;

    public ResponseTemplateService(@Value("${app.template.dir}") String templateDir) {
        this.templateDir = Path.of(templateDir).toAbsolutePath().normalize();
    }

    public String read(String name) {
        if (name == null || name.isBlank() || name.startsWith("..")) {
            throw ApiException.badRequest();
        }
        Path file = templateDir.resolve(name);
        if (!Files.isRegularFile(file)) {
            throw ApiException.notFound();
        }
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw ApiException.notFound();
        }
    }
}
