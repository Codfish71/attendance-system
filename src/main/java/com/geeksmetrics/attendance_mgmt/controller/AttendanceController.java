package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.*; // Import all DTOs
import com.geeksmetrics.attendance_mgmt.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor // Use Lombok for clean constructor injection
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<AttendanceDto> checkIn(@RequestBody CheckInRequest request, Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        // Service now returns AttendanceDto
        AttendanceDto attendance = attendanceService.checkIn(userId, request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<AttendanceDto> checkOut(@RequestBody CheckOutRequest request, Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        // Service now returns AttendanceDto
        AttendanceDto attendance = attendanceService.checkOut(userId, request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/my-attendances")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<List<AttendanceDto>> getMyAttendances(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        // Service now returns List<AttendanceDto>
        List<AttendanceDto> attendances = attendanceService.getUserAttendances(userId, start, end);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<List<AttendanceDto>> getPendingAttendances() {
        // Service now returns List<AttendanceDto>
        List<AttendanceDto> attendances = attendanceService.getPendingAttendances();
        return ResponseEntity.ok(attendances);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<AttendanceDto> approveAttendance(@PathVariable Long id, Authentication auth) {
        Long approverId = ((UserPrincipal) auth.getPrincipal()).getId();
        // Service now returns AttendanceDto
        AttendanceDto attendance = attendanceService.approveAttendance(id, approverId);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<AttendanceDto> rejectAttendance(
            @PathVariable Long id,
            @RequestBody RejectRequest request,
            Authentication auth) {
        Long approverId = ((UserPrincipal) auth.getPrincipal()).getId();
        // Service now returns AttendanceDto
        AttendanceDto attendance = attendanceService.rejectAttendance(id, approverId, request.getReason());
        return ResponseEntity.ok(attendance);
    }
}