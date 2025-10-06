package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.CheckInRequest;
import com.geeksmetrics.attendance_mgmt.dto.CheckOutRequest;
import com.geeksmetrics.attendance_mgmt.dto.RejectRequest;
import com.geeksmetrics.attendance_mgmt.dto.UserPrincipal;
import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")

public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<Attendance> checkIn(@RequestBody CheckInRequest request, Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        Attendance attendance = attendanceService.checkIn(userId, request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<Attendance> checkOut(@RequestBody CheckOutRequest request, Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        Attendance attendance = attendanceService.checkOut(userId, request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/my-attendances")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<List<Attendance>> getMyAttendances(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        List<Attendance> attendances = attendanceService.getUserAttendances(userId, start, end);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<List<Attendance>> getPendingAttendances() {
        List<Attendance> attendances = attendanceService.getPendingAttendances();
        return ResponseEntity.ok(attendances);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<Attendance> approveAttendance(@PathVariable Long id, Authentication auth) {
        Long approverId = ((UserPrincipal) auth.getPrincipal()).getId();
        Attendance attendance = attendanceService.approveAttendance(id, approverId);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<Attendance> rejectAttendance(
            @PathVariable Long id,
            @RequestBody RejectRequest request,
            Authentication auth) {
        Long approverId = ((UserPrincipal) auth.getPrincipal()).getId();
        Attendance attendance = attendanceService.rejectAttendance(id, approverId, request.getReason());
        return ResponseEntity.ok(attendance);
    }
}
