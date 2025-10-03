package com.geeksmetrics.attendance_mgmt.service;

import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.entity.PublicHoliday;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.repository.AttendanceRepository;
import com.geeksmetrics.attendance_mgmt.repository.PublicHolidayRepository;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PayrollService {
    @Autowired private UserRepository userRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PublicHolidayRepository holidayRepository;

    @Value("${payroll.standard-daily-hours}") private double standardDailyHours;
    @Value("${payroll.hourly-rate}") private double standardHourlyRate;
    @Value("${payroll.overtime-rate-multiplier}") private double overtimeMultiplier;
    @Value("${payroll.weekend-rate-multiplier}") private double weekendMultiplier;
    @Value("${payroll.holiday-rate-multiplier}") private double holidayMultiplier;

    public double calculatePayForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));
        double hourlyRate = user.getHourlyRate() != null ? user.getHourlyRate() : standardHourlyRate;

        List<Attendance> records = attendanceRepository.findByUserAndClockInTimeBetween(
                user, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()
        ).stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.APPROVED).toList();

        Map<LocalDate, PublicHoliday> holidays = holidayRepository.findByHolidayDateBetween(startDate, endDate)
                .stream().collect(Collectors.toMap(PublicHoliday::getHolidayDate, ph -> ph));

        double totalPay = 0.0;

        for (Attendance record : records) {
            LocalDateTime clockIn = record.getClockInTime();
            LocalDateTime clockOut = record.getClockOutTime();
            if (clockIn == null || clockOut == null) continue;

            double hoursWorked = Duration.between(clockIn, clockOut).toMinutes() / 60.0;
            double standardHours = Math.min(hoursWorked, standardDailyHours);
            double overtimeHours = Math.max(0, hoursWorked - standardDailyHours);

            LocalDate workDate = clockIn.toLocalDate();
            DayOfWeek day = workDate.getDayOfWeek();

            // Base pay for standard hours
            totalPay += standardHours * hourlyRate;

            // Overtime Calculation
            if (overtimeHours > 0) {
                double currentMultiplier = overtimeMultiplier;
                if (holidays.containsKey(workDate)) {
                    currentMultiplier = holidayMultiplier;
                } else if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) { // Weekends in Kuwait
                    currentMultiplier = weekendMultiplier;
                }
                totalPay += overtimeHours * hourlyRate * currentMultiplier;
            }
        }
        // Add logic for paid leaves
        return totalPay;
    }
}
