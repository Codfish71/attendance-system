package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.RejectRequest;
import com.geeksmetrics.attendance_mgmt.dto.UserPrincipal;
import com.geeksmetrics.attendance_mgmt.entity.Leave;
import com.geeksmetrics.attendance_mgmt.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")

public class LeaveController {
    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<Leave> applyLeave(@RequestBody Leave leave, Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        Leave savedLeave = leaveService.applyLeave(userId, leave);
        return ResponseEntity.ok(savedLeave);
    }

    @GetMapping("/my-leaves")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<List<Leave>> getMyLeaves(Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        List<Leave> leaves = leaveService.getUserLeaves(userId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<List<Leave>> getPendingLeaves() {
        List<Leave> leaves = leaveService.getPendingLeaves();
        return ResponseEntity.ok(leaves);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<Leave> approveLeave(@PathVariable Long id, Authentication auth) {
        Long approverId = ((UserPrincipal) auth.getPrincipal()).getId();
        Leave leave = leaveService.approveLeave(id, approverId);
        return ResponseEntity.ok(leave);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<Leave> rejectLeave(
            @PathVariable Long id,
            @RequestBody RejectRequest request,
            Authentication auth) {
        Long approverId = ((UserPrincipal) auth.getPrincipal()).getId();
        Leave leave = leaveService.rejectLeave(id, approverId, request.getReason());
        return ResponseEntity.ok(leave);
    }
}

