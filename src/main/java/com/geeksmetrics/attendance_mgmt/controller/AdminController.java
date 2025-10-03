package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.service.AttendanceService;
import com.geeksmetrics.attendance_mgmt.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private PayrollService payrollService;

    @GetMapping("/attendance/pending")
    public ResponseEntity<List<Attendance>> getPendingAttendance() {
        return ResponseEntity.ok(attendanceService.getPendingAttendance());
    }

    @PostMapping("/attendance/{id}/approve")
    public ResponseEntity<Attendance> approveAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.approveAttendance(id));
    }

    @PostMapping("/payroll/calculate")
    public ResponseEntity<Double> calculatePay(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        double pay = payrollService.calculatePayForPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(pay);
    }

    // Endpoints for leave approval, etc.
}
