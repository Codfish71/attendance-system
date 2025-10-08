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
    private final AttendanceMapper attendanceMapper; // Inject the mapper

    @Transactional
    public AttendanceDto checkIn(Long userId, Double latitude, Double longitude) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (attendanceRepository.findActiveAttendanceByUserId(userId).isPresent()) {
            throw new RuntimeException("Already checked in. Please check out first.");
        }

        CompanySettings settings = getCompanySettings();
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

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(savedAttendance); // Map to DTO before returning
    }

    @Transactional
    public AttendanceDto checkOut(Long userId, Double latitude, Double longitude) {
        // Fetch the attendance record with its user to avoid extra queries later
        Attendance attendance = attendanceRepository.findActiveAttendanceByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No active check-in found"));

        CompanySettings settings = getCompanySettings();
        if (!locationService.isWithinProximity(latitude, longitude,
                settings.getOfficeLatitude(), settings.getOfficeLongitude(),
                settings.getProximityRadiusMeters())) {
            throw new RuntimeException("You are not within office proximity");
        }

        attendance.setCheckOut(LocalDateTime.now());
        attendance.setCheckOutLatitude(latitude);
        attendance.setCheckOutLongitude(longitude);

        calculateHours(attendance, settings);

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(savedAttendance); // Map to DTO
    }

    @Transactional
    public AttendanceDto approveAttendance(Long attendanceId, Long approverId) {
        Attendance attendance = attendanceRepository.findByIdWithUserAndApprover(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        attendance.setStatus(AttendanceStatus.APPROVED);
        attendance.setApprovedBy(approver);
        attendance.setApprovedAt(LocalDateTime.now());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(savedAttendance); // Map to DTO
    }

    @Transactional
    public AttendanceDto rejectAttendance(Long attendanceId, Long approverId, String reason) {
        Attendance attendance = attendanceRepository.findByIdWithUserAndApprover(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        attendance.setStatus(AttendanceStatus.REJECTED);
        attendance.setApprovedBy(approver);
        attendance.setApprovedAt(LocalDateTime.now());
        attendance.setRejectionReason(reason);

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toDto(savedAttendance); // Map to DTO
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto> getPendingAttendances() {
        // Use the new efficient method to fetch attendances with their users
        List<Attendance> attendances = attendanceRepository.findByStatusWithUser(AttendanceStatus.PENDING);
        return attendances.stream()
                .map(attendanceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto> getUserAttendances(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Use the new efficient method to fetch attendances with their users
        List<Attendance> attendances = attendanceRepository.findByUserAndCheckInBetweenWithUser(user, start, end);
        return attendances.stream()
                .map(attendanceMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- Private Helper Methods ---

    /**
     * A helper to centralize fetching company settings.
     * This is more efficient than calling findAll() every time.
     */
    private CompanySettings getCompanySettings() {
        return settingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Company settings not configured"));
    }

    private void calculateHours(Attendance attendance, CompanySettings settings) {
        Duration duration = Duration.between(attendance.getCheckIn(), attendance.getCheckOut());
        double hoursWorked = duration.toMinutes() / 60.0;
        attendance.setHoursWorked(hoursWorked);

        LocalDate workDate = attendance.getCheckIn().toLocalDate();
        DayOfWeek dayOfWeek = workDate.getDayOfWeek();
        boolean isHoliday = holidayRepository.existsByDate(workDate);
        // Assuming Friday and Saturday are weekends
        boolean isWeekend = dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY;

        double standardHours = settings.getStandardWorkHoursPerDay();

        // Reset all hour types
        attendance.setRegularHours(0.0);
        attendance.setOvertimeHours(0.0);
        attendance.setWeekendOvertimeHours(0.0);
        attendance.setHolidayOvertimeHours(0.0);

        if (isHoliday) {
            attendance.setHolidayOvertimeHours(hoursWorked);
        } else if (isWeekend) {
            attendance.setWeekendOvertimeHours(hoursWorked);
        } else {
            if (hoursWorked <= standardHours) {
                attendance.setRegularHours(hoursWorked);
            } else {
                attendance.setRegularHours(standardHours);
                attendance.setOvertimeHours(hoursWorked - standardHours);
            }
        }
    }
}