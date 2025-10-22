package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.LeaveDto;
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
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'SITE_LEAD', 'PROJECT_MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<LeaveDto> applyLeave(@RequestBody Leave leaveRequest, Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        LeaveDto savedLeave = leaveService.applyLeave(userId, leaveRequest);
        return ResponseEntity.ok(savedLeave);
    }

    @GetMapping("/my-leaves")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'SITE_LEAD', 'PROJECT_MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<List<LeaveDto>> getMyLeaves(Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        List<LeaveDto> leaves = leaveService.getUserLeaves(userId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<List<LeaveDto>> getPendingLeaves() {
        List<LeaveDto> leaves = leaveService.getPendingLeaves();
        return ResponseEntity.ok(leaves);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<LeaveDto> approveLeave(@PathVariable Long id, Authentication auth) {
        Long approverId = ((UserPrincipal) auth.getPrincipal()).getId();
        LeaveDto leave = leaveService.approveLeave(id, approverId);
        return ResponseEntity.ok(leave);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<LeaveDto> rejectLeave(
            @PathVariable Long id,
            @RequestBody RejectRequest request,
            Authentication auth) {
        Long approverId = ((UserPrincipal) auth.getPrincipal()).getId();
        LeaveDto leave = leaveService.rejectLeave(id, approverId, request.getReason());
        return ResponseEntity.ok(leave);
    }
}