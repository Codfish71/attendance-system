package com.geeksmetrics.attendance_mgmt.mapper;

import com.geeksmetrics.attendance_mgmt.dto.LeaveDto;
import com.geeksmetrics.attendance_mgmt.entity.Leave;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaveMapper {

    private final UserMapper userMapper;

    public LeaveDto toDto(Leave leave) {
        if (leave == null) {
            return null;
        }

        LeaveDto dto = new LeaveDto();
        dto.setId(leave.getId());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setLeaveType(leave.getLeaveType());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setRejectionReason(leave.getRejectionReason());
        dto.setApprovedAt(leave.getApprovedAt());
        dto.setCreatedAt(leave.getCreatedAt());
        dto.setUpdatedAt(leave.getUpdatedAt());

        // Safely map the user and approvedBy fields using the UserMapper's simple DTO method
        if (leave.getUser() != null) {
            dto.setUser(userMapper.toDto(leave.getUser()));
        }
        if (leave.getApprovedBy() != null) {
            dto.setApprovedBy(userMapper.toDto(leave.getApprovedBy()));
        }

        return dto;
    }
}