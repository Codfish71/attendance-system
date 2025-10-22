package com.geeksmetrics.attendance_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.UserDto;
import com.geeksmetrics.attendance_mgmt.dto.UserPrincipal;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.mapper.UserMapper;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'SITE_LEAD', 'PROJECT_MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<UserDto> getCurrentUser(Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userMapper.toDetailDto(user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'PROJECT_MANAGER', 'SITE_LEAD')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream()
                .map(userMapper::toDetailDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userMapper.toDetailDto(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'SITE_LEAD', 'PROJECT_MANAGER', 'HR', 'ADMIN')")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Authentication auth) {
        Long currentUserId = ((UserPrincipal) auth.getPrincipal()).getId();

        // Users can only update their own profile
        if (!currentUserId.equals(id)) {
            throw new RuntimeException("You can only update your own profile");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update only allowed fields
        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("dateOfBirth")) {
            user.setDateOfBirth(java.time.LocalDate.parse((String) updates.get("dateOfBirth")));
        }
        if (updates.containsKey("profilePhotoUrl")) {
            user.setProfilePhotoUrl((String) updates.get("profilePhotoUrl"));
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(userMapper.toDetailDto(updatedUser));
    }
}