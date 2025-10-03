package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.ClockInRequest;
import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.security.services.UserDetailsImpl;
import com.geeksmetrics.attendance_mgmt.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee/attendance")
public class EmployeeController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<Attendance> clockIn(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestBody ClockInRequest clockInRequest) {
        Attendance attendance = attendanceService.clockIn(userDetails.getId(), clockInRequest);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<Attendance> clockOut(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Attendance attendance = attendanceService.clockOut(userDetails.getId());
        return ResponseEntity.ok(attendance);
    }
}