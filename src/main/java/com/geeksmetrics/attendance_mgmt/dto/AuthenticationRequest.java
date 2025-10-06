package com.geeksmetrics.attendance_mgmt.dto;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String email;
    private String password;
}