package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.entity.PublicHoliday;
import com.geeksmetrics.attendance_mgmt.repository.PublicHolidayRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/holidays")
public class PublicHolidayController {

    private final PublicHolidayRepository holidayRepository;

    public PublicHolidayController(PublicHolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<List<PublicHoliday>> getAllHolidays() {
        return ResponseEntity.ok(holidayRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<PublicHoliday> createHoliday(@RequestBody PublicHoliday holiday) {
        return ResponseEntity.ok(holidayRepository.save(holiday));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        holidayRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
