package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.PayrollDto;
import com.geeksmetrics.attendance_mgmt.dto.UserPrincipal;
// import com.geeksmetrics.attendance_mgmt.entity.Payroll; // No longer directly returning entity
import com.geeksmetrics.attendance_mgmt.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor // Use Lombok for constructor injection
public class PayrollController {
    private final PayrollService payrollService;

    // Constructor can be removed if @RequiredArgsConstructor is used and fields are final
    // public PayrollController(PayrollService payrollService) {
    //     this.payrollService = payrollService;
    // }

    @PostMapping("/generate/{userId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<PayrollDto> generatePayroll( // Return PayrollDto
                                                       @PathVariable Long userId,
                                                       @RequestParam int month,
                                                       @RequestParam int year) {
        PayrollDto payroll = payrollService.generatePayroll(userId, month, year); // Service returns DTO
        return ResponseEntity.ok(payroll);
    }

    @GetMapping("/month")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<PayrollDto>> getPayrollsByMonth( // Already updated, good
                                                                @RequestParam int month,
                                                                @RequestParam int year) {
        List<PayrollDto> payrolls = payrollService.getPayrollsByMonth(month, year);
        return ResponseEntity.ok(payrolls);
    }


    @GetMapping("/my-payroll")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<PayrollDto> getMyPayroll(
            @RequestParam int month,
            @RequestParam int year,
            Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        PayrollDto payroll = payrollService.getUserPayroll(userId, month, year);

        if (payroll == null) {
            return ResponseEntity.notFound().build(); // Return 404 Not Found
        }
        return ResponseEntity.ok(payroll); // Return 200 OK with payroll data
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<PayrollDto> processPayroll(@PathVariable Long id) { // Return PayrollDto
        PayrollDto payroll = payrollService.processPayroll(id); // Service returns DTO
        return ResponseEntity.ok(payroll);
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<PayrollDto> markAsPaid(@PathVariable Long id) { // Return PayrollDto
        PayrollDto payroll = payrollService.markAsPaid(id); // Service returns DTO
        return ResponseEntity.ok(payroll);
    }
}
