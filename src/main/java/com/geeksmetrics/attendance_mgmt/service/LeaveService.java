package com.geeksmetrics.attendance_mgmt.service;

import com.geeksmetrics.attendance_mgmt.dto.LeaveDto;
import com.geeksmetrics.attendance_mgmt.entity.Leave;
import com.geeksmetrics.attendance_mgmt.entity.LeaveStatus;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.mapper.LeaveMapper;
import com.geeksmetrics.attendance_mgmt.repository.LeaveRepository;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final LeaveMapper leaveMapper; // Inject the mapper


    @Transactional
    public LeaveDto applyLeave(Long userId, Leave leave) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        leave.setUser(user);
        leave.setStatus(LeaveStatus.PENDING);

        Leave savedLeave = leaveRepository.save(leave);
        return leaveMapper.toDto(savedLeave); // Map to DTO
    }

    @Transactional
    public LeaveDto approveLeave(Long leaveId, Long approverId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approver);
        leave.setApprovedAt(LocalDateTime.now());

        Leave savedLeave = leaveRepository.save(leave);
        return leaveMapper.toDto(savedLeave); // Map to DTO
    }

    @Transactional
    public LeaveDto rejectLeave(Long leaveId, Long approverId, String reason) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(approver);
        leave.setApprovedAt(LocalDateTime.now());
        leave.setRejectionReason(reason);

        Leave savedLeave = leaveRepository.save(leave);
        return leaveMapper.toDto(savedLeave); // Map to DTO
    }

    public List<LeaveDto> getPendingLeaves() {
        List<Leave> leaves = leaveRepository.findByStatus(LeaveStatus.PENDING);
        return leaves.stream()
                .map(leaveMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<LeaveDto> getUserLeaves(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Leave> leaves = leaveRepository.findByUserWithUser(user);
        return leaves.stream()
                .map(leaveMapper::toDto)
                .collect(Collectors.toList());
    }
}