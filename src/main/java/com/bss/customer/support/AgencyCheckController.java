package com.bss.customer.support;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/admin/agency")
public class AgencyCheckController {

    private final AgencyCheckService service;

    public AgencyCheckController(AgencyCheckService service) {
        this.service = service;
    }

    @PostMapping("/connectivity-check")
    @PreAuthorize("hasRole('ADMIN')")
    public AgencyCheckService.AgencyCheckResult check(@Valid @RequestBody CheckRequest request) {
        return service.check(request.url());
    }

    public record CheckRequest(@NotBlank @Size(max = 300) String url) {
    }
}
