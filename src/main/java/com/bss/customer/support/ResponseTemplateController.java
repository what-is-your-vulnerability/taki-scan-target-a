package com.bss.customer.support;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consultant/response-templates")
public class ResponseTemplateController {

    private final ResponseTemplateService service;

    public ResponseTemplateController(ResponseTemplateService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<String> read(@RequestParam("name") String name) {
        return ResponseEntity.ok().contentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8))
                .body(service.read(name));
    }
}
