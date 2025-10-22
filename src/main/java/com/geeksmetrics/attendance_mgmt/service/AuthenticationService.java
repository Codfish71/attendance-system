package com.geeksmetrics.attendance_mgmt.service;


import com.geeksmetrics.attendance_mgmt.dto.AuthenticationRequest;
import com.geeksmetrics.attendance_mgmt.dto.AuthenticationResponse;
import com.geeksmetrics.attendance_mgmt.dto.RegisterRequest;
import com.geeksmetrics.attendance_mgmt.dto.UserPrincipal;
import com.geeksmetrics.attendance_mgmt.entity.Role;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import com.geeksmetrics.attendance_mgmt.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmployeeIdService employeeIdService;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setHourlyRate(request.getHourlyRate());

        // Set roles
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>());
            user.getRoles().add(Role.ROLE_COORDINATOR); // Default role
        } else {
            user.setRoles(request.getRoles());
        }

        // Generate Employee ID based on primary role
        String primaryRole = user.getRoles().stream()
                .findFirst()
                .orElse(Role.ROLE_COORDINATOR)
                .name();

        String prefix = employeeIdService.getPrefixForRole(primaryRole);
        String employeeId = employeeIdService.generateEmployeeIdWithPrefix(prefix);
        user.setEmployeeId(employeeId);

        user.setActive(true);

        User savedUser = userRepository.save(user);

        var userPrincipal = new UserPrincipal(savedUser);
        var jwtToken = jwtService.generateToken(userPrincipal);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .userId(savedUser.getId())
                .employeeId(savedUser.getEmployeeId())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var userPrincipal = new UserPrincipal(user);
        var jwtToken = jwtService.generateToken(userPrincipal);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .build();
    }
}