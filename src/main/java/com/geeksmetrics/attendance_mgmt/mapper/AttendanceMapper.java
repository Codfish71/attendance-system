package com.geeksmetrics.attendance_mgmt.mapper;

import com.geeksmetrics.attendance_mgmt.dto.AttendanceDto;
import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceMapper {

    private final UserMapper userMapper;

    public AttendanceDto toDto(Attendance attendance) {
        if (attendance == null) {
            return null;
        }

        AttendanceDto dto = new AttendanceDto();
        dto.setId(attendance.getId());
        dto.setCheckIn(attendance.getCheckIn());
        dto.setCheckOut(attendance.getCheckOut());
        dto.setCheckInLatitude(attendance.getCheckInLatitude());
        dto.setCheckInLongitude(attendance.getCheckInLongitude());
        dto.setCheckOutLatitude(attendance.getCheckOutLatitude());
        dto.setCheckOutLongitude(attendance.getCheckOutLongitude());
        dto.setHoursWorked(attendance.getHoursWorked());
        dto.setRegularHours(attendance.getRegularHours());
        dto.setOvertimeHours(attendance.getOvertimeHours());
        dto.setWeekendOvertimeHours(attendance.getWeekendOvertimeHours());
        dto.setHolidayOvertimeHours(attendance.getHolidayOvertimeHours());
        dto.setStatus(attendance.getStatus());
        dto.setNotes(attendance.getNotes());
        dto.setRejectionReason(attendance.getRejectionReason());
        dto.setApprovedAt(attendance.getApprovedAt());
        dto.setCreatedAt(attendance.getCreatedAt());
        dto.setUpdatedAt(attendance.getUpdatedAt());

        // Safely map the user and approvedBy fields using the UserMapper
        if (attendance.getUser() != null) {
            dto.setUser(userMapper.toDto(attendance.getUser()));
        }
        if (attendance.getApprovedBy() != null) {
            dto.setApprovedBy(userMapper.toDto(attendance.getApprovedBy()));
        }

        return dto;
    }
}
