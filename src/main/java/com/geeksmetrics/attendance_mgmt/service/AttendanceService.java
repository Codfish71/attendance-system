package com.geeksmetrics.attendance_mgmt.service;



import com.geeksmetrics.attendance_mgmt.dto.ClockInRequest;
import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.repository.AttendanceRepository;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private UserRepository userRepository;

    @Value("${office.location.latitude}")
    private double officeLatitude;

    @Value("${office.location.longitude}")
    private double officeLongitude;

    @Value("${office.location.proximity-radius}")
    private double proximityRadius;

    @Transactional
    public Attendance clockIn(Long userId, ClockInRequest clockInRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Proximity Check
        double distance = LocationUtil.calculateDistance(
                clockInRequest.getLatitude(), clockInRequest.getLongitude(),
                officeLatitude, officeLongitude
        );

        if (distance > proximityRadius) {
            throw new IllegalStateException("You are not within the office proximity to clock in.");
        }

        // 2. Check for existing open clock-in
        attendanceRepository.findTopByUserAndClockOutTimeIsNullOrderByClockInTimeDesc(user)
                .ifPresent(a -> { throw new IllegalStateException("You must clock out from your previous session first."); });

        // 3. Create and save new attendance record
        Attendance newAttendance = new Attendance();
        newAttendance.setUser(user);
        newAttendance.setClockInTime(LocalDateTime.now());
        newAttendance.setClockInLatitude(clockInRequest.getLatitude());
        newAttendance.setClockInLongitude(clockInRequest.getLongitude());
        newAttendance.setStatus(Attendance.AttendanceStatus.PENDING);

        return attendanceRepository.save(newAttendance);
    }

    @Transactional
    public Attendance clockOut(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Attendance attendance = attendanceRepository.findTopByUserAndClockOutTimeIsNullOrderByClockInTimeDesc(user)
                .orElseThrow(() -> new IllegalStateException("No active clock-in session found."));

        attendance.setClockOutTime(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    // Methods for admin to approve/reject
    public Attendance approveAttendance(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId).orElseThrow(() -> new RuntimeException("Record not found"));
        attendance.setStatus(Attendance.AttendanceStatus.APPROVED);
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getPendingAttendance() {
        return attendanceRepository.findByStatus(Attendance.AttendanceStatus.PENDING);
    }
}
