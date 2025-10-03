package com.geeksmetrics.attendance_mgmt.controller;


import com.geeksmetrics.attendance_mgmt.dto.AuthRequest;
import com.geeksmetrics.attendance_mgmt.dto.AuthResponse;
import com.geeksmetrics.attendance_mgmt.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest authRequest) {
        String token = authService.loginUser(authRequest);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // Add a /signup endpoint here
}