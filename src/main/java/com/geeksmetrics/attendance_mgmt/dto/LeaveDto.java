package com.geeksmetrics.attendance_mgmt.dto;

import com.geeksmetrics.attendance_mgmt.entity.LeaveStatus;
import com.geeksmetrics.attendance_mgmt.entity.LeaveType;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveDto {
    private Long id;
    private UserDto user;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private String reason;
    private LeaveStatus status;
    private String rejectionReason;
    private UserDto approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}