package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.ClockInRequestDto;
import com.geeksmetrics.attendance_mgmt.dto.MessageResponseDto;
import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.security.services.UserDetailsServiceImpl;
import com.geeksmetrics.attendance_mgmt.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@AuthenticationPrincipal UserDetailsServiceImpl userDetails, @RequestBody ClockInRequestDto request) {
        try {
            Attendance attendance = attendanceService.clockIn(userDetails.getId(), request);
            return ResponseEntity.ok(attendance);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/clock-out")
    public ResponseEntity<?> clockOut(@AuthenticationPrincipal UserDetailsServiceImpl userDetails) {
        try {
            Attendance attendance = attendanceService.clockOut(userDetails.getId());
            return ResponseEntity.ok(attendance);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage()));
        }
    }
}