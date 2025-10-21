package com.geeksmetrics.attendance_mgmt.dto;

import com.geeksmetrics.attendance_mgmt.entity.AttendanceStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceDto {
    private Long id;
    private UserDto user; // Use UserDto for the employee
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Double checkInLatitude;
    private Double checkInLongitude;
    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private Double hoursWorked;
    private Double regularHours;
    private Double overtimeHours;
    private Double weekendOvertimeHours;
    private Double holidayOvertimeHours;
    private AttendanceStatus status;
    private String notes;
    private String rejectionReason;
    private UserDto approvedBy; // Use UserDto for the approver
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
