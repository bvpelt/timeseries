package com.bsoft.timeseries.controller;

import com.bsoft.timeseries.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api-keys")
@RequiredArgsConstructor
public class ApiKeyAdminController {

    private final ApiKeyRepository repository;

    @PostMapping
    @PreAuthorize("hasAuthority('READ_WRITE')")
    public ResponseEntity<String> createKey(@RequestParam String owner,
                                            @RequestParam String permission) {
        // In production: use a cryptographically secure token generator
        String newKey = UUID.randomUUID().toString().replace("-", "");
        // persist via a dedicated service/entity factory in production
        return ResponseEntity.ok("Key created: " + newKey + " for " + owner);
    }
}