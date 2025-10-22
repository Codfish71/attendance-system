package com.geeksmetrics.attendance_mgmt.dto;

import com.geeksmetrics.attendance_mgmt.entity.Role;
import com.geeksmetrics.attendance_mgmt.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Double hourlyRate;
    private Double weekendOvertimeMultiplier;
    private Double holidayOvertimeMultiplier;
    private Double regularOvertimeMultiplier;
    private Set<Role> roles;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String employeeId;
    private LocalDate dateOfBirth;
    private String profilePhotoUrl;
    private User reportsTo;
}