package com.geeksmetrics.attendance_mgmt.dto;

import com.geeksmetrics.attendance_mgmt.entity.Role;
import lombok.Data;
import java.util.Set;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Double hourlyRate;
    private Set<Role> roles;
}
