package com.geeksmetrics.attendance_mgmt.service;

import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeIdService {
    private final UserRepository userRepository;

    @Transactional
    public String generateEmployeeId() {
        // Get the count of existing users
        long userCount = userRepository.count();

        // Generate next employee ID
        String employeeId;
        int counter = 1;

        do {
            // Format: E00001, E00002, etc.
            employeeId = String.format("E%05d", userCount + counter);
            counter++;
        } while (userRepository.existsByEmployeeId(employeeId));

        return employeeId;
    }

    @Transactional
    public String generateEmployeeIdWithPrefix(String prefix) {
        // For specific prefixes like PM (Project Manager), SL (Site Lead), CO (Coordinator)
        long count = userRepository.countByEmployeeIdStartingWith(prefix);

        String employeeId;
        int counter = 1;

        do {
            employeeId = String.format("%s%05d", prefix, count + counter);
            counter++;
        } while (userRepository.existsByEmployeeId(employeeId));

        return employeeId;
    }

    public String getPrefixForRole(String role) {
        switch (role) {
            case "ROLE_PROJECT_MANAGER":
                return "PM";
            case "ROLE_SITE_LEAD":
                return "SL";
            case "ROLE_COORDINATOR":
                return "CO";
            case "ROLE_HR":
                return "HR";
            case "ROLE_ADMIN":
                return "AD";
            default:
                return "E";
        }
    }
}
