package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.PayrollDto;
import com.geeksmetrics.attendance_mgmt.dto.UserPrincipal;
import com.geeksmetrics.attendance_mgmt.entity.Payroll;
import com.geeksmetrics.attendance_mgmt.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payroll")

public class PayrollController {
    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/generate/{userId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Payroll> generatePayroll(
            @PathVariable Long userId,
            @RequestParam int month,
            @RequestParam int year) {
        Payroll payroll = payrollService.generatePayroll(userId, month, year);
        return ResponseEntity.ok(payroll);
    }

    @GetMapping("/month")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    // Update the response type to use the DTO
    public ResponseEntity<List<PayrollDto>> getPayrollsByMonth(
            @RequestParam int month,
            @RequestParam int year) {
        // The service now returns a list of DTOs
        List<PayrollDto> payrolls = payrollService.getPayrollsByMonth(month, year);
        return ResponseEntity.ok(payrolls);
    }


    @GetMapping("/my-payroll")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<Payroll> getMyPayroll(
            @RequestParam int month,
            @RequestParam int year,
            Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        Payroll payroll = payrollService.getUserPayroll(userId, month, year);
        return ResponseEntity.ok(payroll);
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Payroll> processPayroll(@PathVariable Long id) {
        Payroll payroll = payrollService.processPayroll(id);
        return ResponseEntity.ok(payroll);
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<Payroll> markAsPaid(@PathVariable Long id) {
        Payroll payroll = payrollService.markAsPaid(id);
        return ResponseEntity.ok(payroll);
    }
}
