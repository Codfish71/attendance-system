package com.geeksmetrics.attendance_mgmt.service;

// ============= AttendanceService.java =============

import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.entity.AttendanceStatus;
import com.geeksmetrics.attendance_mgmt.entity.CompanySettings;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.repository.AttendanceRepository;
import com.geeksmetrics.attendance_mgmt.repository.CompanySettingsRepository;
import com.geeksmetrics.attendance_mgmt.repository.PublicHolidayRepository;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CompanySettingsRepository settingsRepository;
    private final PublicHolidayRepository holidayRepository;
    private final LocationService locationService;

    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository, CompanySettingsRepository settingsRepository, PublicHolidayRepository holidayRepository, LocationService locationService) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.holidayRepository = holidayRepository;
        this.locationService = locationService;
    }


    @Transactional
    public Attendance checkIn(Long userId, Double latitude, Double longitude) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already has an active attendance
        if (attendanceRepository.findActiveAttendanceByUserId(userId).isPresent()) {
            throw new RuntimeException("Already checked in. Please check out first.");
        }

        // Validate proximity
        CompanySettings settings = settingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Company settings not configured"));

        if (!locationService.isWithinProximity(latitude, longitude,
                settings.getOfficeLatitude(), settings.getOfficeLongitude(),
                settings.getProximityRadiusMeters())) {
            throw new RuntimeException("You are not within office proximity");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setCheckInLatitude(latitude);
        attendance.setCheckInLongitude(longitude);
        attendance.setStatus(AttendanceStatus.PENDING);

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance checkOut(Long userId, Double latitude, Double longitude) {
        Attendance attendance = attendanceRepository.findActiveAttendanceByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No active check-in found"));

        // Validate proximity
        CompanySettings settings = settingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Company settings not configured"));

        if (!locationService.isWithinProximity(latitude, longitude,
                settings.getOfficeLatitude(), settings.getOfficeLongitude(),
                settings.getProximityRadiusMeters())) {
            throw new RuntimeException("You are not within office proximity");
        }

        attendance.setCheckOut(LocalDateTime.now());
        attendance.setCheckOutLatitude(latitude);
        attendance.setCheckOutLongitude(longitude);

        // Calculate hours worked
        calculateHours(attendance, settings);

        return attendanceRepository.save(attendance);
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

    @Transactional
    public Attendance approveAttendance(Long attendanceId, Long approverId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        attendance.setStatus(AttendanceStatus.APPROVED);
        attendance.setApprovedBy(approver);
        attendance.setApprovedAt(LocalDateTime.now());

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance rejectAttendance(Long attendanceId, Long approverId, String reason) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        attendance.setStatus(AttendanceStatus.REJECTED);
        attendance.setApprovedBy(approver);
        attendance.setApprovedAt(LocalDateTime.now());
        attendance.setRejectionReason(reason);

        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getPendingAttendances() {
        return attendanceRepository.findByStatus(AttendanceStatus.PENDING);
    }

    public List<Attendance> getUserAttendances(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return attendanceRepository.findByUserAndCheckInBetween(user, start, end);
    }
}

