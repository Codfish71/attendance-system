package com.geeksmetrics.attendance_mgmt.service;

import com.geeksmetrics.attendance_mgmt.dto.UserDto;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.mapper.UserMapper;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // --- Existing Methods (unchanged) ---

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return userMapper.toDetailDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAllWithRoles();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- New Methods for Detailed DTO ---

    /**
     * Fetches a single user by ID with all details.
     * @param id The ID of the user.
     * @return A UserDetailDto with comprehensive user information.
     */
    @Transactional(readOnly = true)
    public UserDto getUserDetailsById(Long id) {
        User user = userRepository.findByIdWithRoles(id) // Use the new efficient method
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return userMapper.toDetailDto(user);
    }

    /**
     * Fetches all users with their complete details.
     * @return A list of UserDetailDto objects.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUserDetails() {
        List<User> users = userRepository.findAllWithRoles();
        return users.stream()
                .map(userMapper::toDetailDto)
                .collect(Collectors.toList());
    }
}
