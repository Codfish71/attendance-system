package com.geeksmetrics.attendance_mgmt.service;

import com.geeksmetrics.attendance_mgmt.dto.AttendanceDto;
import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.entity.AttendanceStatus;
import com.geeksmetrics.attendance_mgmt.entity.CompanySettings;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.mapper.AttendanceMapper;
import com.geeksmetrics.attendance_mgmt.repository.AttendanceRepository;
import com.geeksmetrics.attendance_mgmt.repository.CompanySettingsRepository;
import com.geeksmetrics.attendance_mgmt.repository.PublicHolidayRepository;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CompanySettingsRepository settingsRepository;
    private final PublicHolidayRepository holidayRepository;
    private final LocationService locationService;
    private final AttendanceMapper attendanceMapper;


    @Transactional
    public AttendanceDto checkIn(Long userId, Double latitude, Double longitude) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already has an active attendance
        if (attendanceRepository.findActiveAttendanceByUserId(userId).isPresent()) {
            throw new RuntimeException("Already checked in. Please check out first.");
        }

        // Validate proximity
        CompanySettings settings = settingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Company settings not configured"));

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setCheckInLatitude(latitude);
        attendance.setCheckInLongitude(longitude);
        attendance.setStatus(AttendanceStatus.PROCESSING);

        attendanceRepository.save(attendance);
        return attendanceMapper.toDto(attendance);

    }

    @Transactional
    public AttendanceDto checkOut(Long userId, Double latitude, Double longitude) {
        Attendance attendance = attendanceRepository.findActiveAttendanceByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No active check-in found"));

        // Validate proximity
        CompanySettings settings = settingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Company settings not configured"));


        attendance.setCheckOut(LocalDateTime.now());
        attendance.setCheckOutLatitude(latitude);
        attendance.setCheckOutLongitude(longitude);

        // Calculate hours worked
        calculateHours(attendance, settings);

        // Mark as COMPLETED instead of PENDING
        attendance.setStatus(AttendanceStatus.COMPLETED);

        attendanceRepository.save(attendance);
        return attendanceMapper.toDto(attendance);
    }

    // Remove approve/reject methods - not needed anymore

    public List<AttendanceDto> getUserAttendances(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Attendance> attendances = attendanceRepository.findByUserAndCheckInBetween(user, start, end);
        return attendances.stream()
                .map(attendanceMapper::toDto)
                .collect(Collectors.toList());
    }

    private void calculateHours(Attendance attendance, CompanySettings settings) {
        Duration duration = Duration.between(attendance.getCheckIn(), attendance.getCheckOut());
        double hoursWorked = duration.toMinutes() / 60.0;
        attendance.setHoursWorked(hoursWorked);

        LocalDate workDate = attendance.getCheckIn().toLocalDate();
        DayOfWeek dayOfWeek = workDate.getDayOfWeek();
        boolean isHoliday = holidayRepository.existsByDate(workDate);
        boolean isWeekend = dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY;

        double standardHours = settings.getStandardWorkHoursPerDay();

        if (isHoliday) {
            // All hours on holiday are holiday overtime
            attendance.setHolidayOvertimeHours(hoursWorked);
            attendance.setRegularHours(0.0);
            attendance.setOvertimeHours(0.0);
            attendance.setWeekendOvertimeHours(0.0);
        } else if (isWeekend) {
            // All hours on weekend are weekend overtime
            attendance.setWeekendOvertimeHours(hoursWorked);
            attendance.setRegularHours(0.0);
            attendance.setOvertimeHours(0.0);
            attendance.setHolidayOvertimeHours(0.0);
        } else {
            // Regular workday
            if (hoursWorked <= standardHours) {
                attendance.setRegularHours(hoursWorked);
                attendance.setOvertimeHours(0.0);
            } else {
                attendance.setRegularHours(standardHours);
                attendance.setOvertimeHours(hoursWorked - standardHours);
            }
            attendance.setWeekendOvertimeHours(0.0);
            attendance.setHolidayOvertimeHours(0.0);
        }
    }
}

