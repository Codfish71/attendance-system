package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.entity.CompanySettings;
import com.geeksmetrics.attendance_mgmt.repository.CompanySettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")

public class CompanySettingsController {

    private final CompanySettingsRepository settingsRepository;

    public CompanySettingsController(CompanySettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<CompanySettings> getSettings() {
        CompanySettings settings = settingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Settings not found"));
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<CompanySettings> updateSettings(
            @PathVariable Long id,
            @RequestBody CompanySettings settings) {
        settings.setId(id);
        return ResponseEntity.ok(settingsRepository.save(settings));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<CompanySettings> createSettings(@RequestBody CompanySettings settings) {
        return ResponseEntity.ok(settingsRepository.save(settings));
    }
}
