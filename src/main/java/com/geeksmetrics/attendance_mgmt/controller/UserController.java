package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.UserDto;
import com.geeksmetrics.attendance_mgmt.dto.UserPrincipal;
import com.geeksmetrics.attendance_mgmt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- Existing Endpoints using UserDto ---

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<UserDto> getCurrentUser(Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // --- New Endpoints for Detailed User Information ---

    /**
     * Endpoint for admins/HR to get a complete profile of a user.
     */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<UserDto> getUserDetailsById(@PathVariable Long id) {
        UserDto userDetails = userService.getUserDetailsById(id);
        return ResponseEntity.ok(userDetails);
    }

    /**
     * Endpoint for admins/HR to get a list of all users with their complete profiles.
     */
    @GetMapping("/details")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUserDetails() {
        List<UserDto> allUserDetails = userService.getAllUserDetails();
        return ResponseEntity.ok(allUserDetails);
    }
}