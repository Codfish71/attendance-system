package com.geeksmetrics.attendance_mgmt.service;

import com.geeksmetrics.attendance_mgmt.dto.PayrollDto;
import com.geeksmetrics.attendance_mgmt.dto.UserDto; // Keep if needed for other methods, but not directly for payroll mapping now
import com.geeksmetrics.attendance_mgmt.entity.*;
import com.geeksmetrics.attendance_mgmt.mapper.PayrollMapper; // Import the new mapper
import com.geeksmetrics.attendance_mgmt.repository.*;
import lombok.RequiredArgsConstructor; // Good practice for constructor injection
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Use Lombok for constructor injection
public class PayrollService {
    private final PayrollRepository payrollRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final CompanySettingsRepository settingsRepository;
    private final PayrollMapper payrollMapper; // Inject the new mapper

    // Constructor can be removed if @RequiredArgsConstructor is used and fields are final
    // public PayrollService(PayrollRepository payrollRepository, AttendanceRepository attendanceRepository, LeaveRepository leaveRepository, UserRepository userRepository, CompanySettingsRepository settingsRepository, PayrollMapper payrollMapper) {
    //     this.payrollRepository = payrollRepository;
    //     this.attendanceRepository = attendanceRepository;
    //     this.leaveRepository = leaveRepository;
    //     this.userRepository = userRepository;
    //     this.settingsRepository = settingsRepository;
    //     this.payrollMapper = payrollMapper;
    // }

    @Transactional
    public PayrollDto generatePayroll(Long userId, int month, int year) { // Return PayrollDto
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if payroll already exists
        if (payrollRepository.findByUserAndMonthAndYearWithUser(user, month, year).isPresent()) {
            throw new RuntimeException("Payroll already exists for this period");
        }

        // Get approved attendances for the month
        List<Attendance> attendances = attendanceRepository
                .findApprovedAttendanceByUserAndMonth(userId, year, month);

        // Calculate totals
        double totalRegularHours = 0.0;
        double totalOvertimeHours = 0.0;
        double totalWeekendOvertimeHours = 0.0;
        double totalHolidayOvertimeHours = 0.0;

        for (Attendance attendance : attendances) {
            totalRegularHours += attendance.getRegularHours() != null ? attendance.getRegularHours() : 0.0;
            totalOvertimeHours += attendance.getOvertimeHours() != null ? attendance.getOvertimeHours() : 0.0;
            totalWeekendOvertimeHours += attendance.getWeekendOvertimeHours() != null ? attendance.getWeekendOvertimeHours() : 0.0;
            totalHolidayOvertimeHours += attendance.getHolidayOvertimeHours() != null ? attendance.getHolidayOvertimeHours() : 0.0;
        }

        // Add paid leave hours
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<Leave> paidLeaves = leaveRepository
                .findApprovedLeavesByUserAndDateRange(userId, startDate, endDate);

        CompanySettings settings = settingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Company settings not configured"));

        for (Leave leave : paidLeaves) {
            if (leave.getLeaveType() == LeaveType.PAID || leave.getLeaveType() == LeaveType.SICK) {
                long leaveDays = calculateWorkingDays(leave.getStartDate(), leave.getEndDate());
                totalRegularHours += leaveDays * settings.getStandardWorkHoursPerDay();
            }
        }

        // Calculate pay
        double regularPay = totalRegularHours * user.getHourlyRate();
        double overtimePay = totalOvertimeHours * user.getHourlyRate() * user.getRegularOvertimeMultiplier();
        double weekendOvertimePay = totalWeekendOvertimeHours * user.getHourlyRate() * user.getWeekendOvertimeMultiplier();
        double holidayOvertimePay = totalHolidayOvertimeHours * user.getHourlyRate() * user.getHolidayOvertimeMultiplier();

        double totalPay = regularPay + overtimePay + weekendOvertimePay + holidayOvertimePay;

        // Create payroll
        Payroll payroll = new Payroll();
        payroll.setUser(user);
        payroll.setMonth(month);
        payroll.setYear(year);
        payroll.setRegularHours(totalRegularHours);
        payroll.setOvertimeHours(totalOvertimeHours);
        payroll.setWeekendOvertimeHours(totalWeekendOvertimeHours);
        payroll.setHolidayOvertimeHours(totalHolidayOvertimeHours);
        payroll.setRegularPay(regularPay);
        payroll.setOvertimePay(overtimePay);
        payroll.setWeekendOvertimePay(weekendOvertimePay);
        payroll.setHolidayOvertimePay(holidayOvertimePay);
        payroll.setTotalPay(totalPay);
        payroll.setPaymentDate(calculatePaymentDate(year, month, settings.getPaymentDay()));
        payroll.setStatus(PayrollStatus.PENDING);

        Payroll savedPayroll = payrollRepository.save(payroll);
        return payrollMapper.toDto(savedPayroll); // Map to DTO before returning
    }

    private long calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        long workingDays = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.FRIDAY && dayOfWeek != DayOfWeek.SATURDAY) { // Assuming Friday and Saturday are non-working days
                workingDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return workingDays;
    }

    private LocalDate calculatePaymentDate(int year, int month, int paymentDay) {
        LocalDate paymentDate = LocalDate.of(year, month, paymentDay);
        DayOfWeek dayOfWeek = paymentDate.getDayOfWeek();

        // If payment day falls on Friday or Saturday, move to nearest Thursday
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            paymentDate = paymentDate.minusDays(1);
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            paymentDate = paymentDate.minusDays(2);
        }

        return paymentDate;
    }

    @Transactional
    public PayrollDto processPayroll(Long payrollId) { // Return PayrollDto
        Payroll payroll = payrollRepository.findByIdWithUser(payrollId) // Use new method to fetch user
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        payroll.setStatus(PayrollStatus.PROCESSED);
        Payroll updatedPayroll = payrollRepository.save(payroll);
        return payrollMapper.toDto(updatedPayroll); // Map to DTO
    }

    @Transactional
    public PayrollDto markAsPaid(Long payrollId) { // Return PayrollDto
        Payroll payroll = payrollRepository.findByIdWithUser(payrollId) // Use new method to fetch user
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        payroll.setStatus(PayrollStatus.PAID);
        Payroll updatedPayroll = payrollRepository.save(payroll);
        return payrollMapper.toDto(updatedPayroll); // Map to DTO
    }

    // This method is already updated to return List<PayrollDto>
    @Transactional(readOnly = true)
    public List<PayrollDto> getPayrollsByMonth(int month, int year) {
        List<Payroll> payrolls = payrollRepository.findByMonthAndYearWithUser(month, year);
        return payrolls.stream()
                .map(payrollMapper::toDto) // Use the injected mapper
                .collect(Collectors.toList());
    }

    public PayrollDto getUserPayroll(Long userId, int month, int year) { // Return PayrollDto
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // MODIFIED: Handle the Optional without throwing an exception
        Optional<Payroll> payrollOptional = payrollRepository.findByUserAndMonthAndYearWithUser(user, month, year);

        // If payroll is found, map it to DTO; otherwise, return null.
        return payrollOptional.map(payrollMapper::toDto).orElse(null);
    }

    // Remove the private toPayrollDto method as it's now in PayrollMapper
    // private PayrollDto toPayrollDto(Payroll payroll) { ... }
}
