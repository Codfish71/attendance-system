package com.geeksmetrics.attendance_mgmt.service;

import com.geeksmetrics.attendance_mgmt.dto.PayrollDto;
import com.geeksmetrics.attendance_mgmt.dto.UserDto;
import com.geeksmetrics.attendance_mgmt.entity.*;
import com.geeksmetrics.attendance_mgmt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class PayrollService {
    private final PayrollRepository payrollRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final CompanySettingsRepository settingsRepository;

    public PayrollService(PayrollRepository payrollRepository, AttendanceRepository attendanceRepository, LeaveRepository leaveRepository, UserRepository userRepository, CompanySettingsRepository settingsRepository) {
        this.payrollRepository = payrollRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
    }

    @Transactional
    public Payroll generatePayroll(Long userId, int month, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if payroll already exists
        if (payrollRepository.findByUserAndMonthAndYear(user, month, year).isPresent()) {
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

        return payrollRepository.save(payroll);
    }

    private long calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        long workingDays = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.FRIDAY && dayOfWeek != DayOfWeek.SATURDAY) {
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
    public Payroll processPayroll(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        payroll.setStatus(PayrollStatus.PROCESSED);
        return payrollRepository.save(payroll);
    }

    @Transactional
    public Payroll markAsPaid(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        payroll.setStatus(PayrollStatus.PAID);
        return payrollRepository.save(payroll);
    }

    // Replace the old getPayrollsByMonth with this new one
    @Transactional(readOnly = true)
    public List<PayrollDto> getPayrollsByMonth(int month, int year) {
        // 1. Fetch entities efficiently using the new method
        List<Payroll> payrolls = payrollRepository.findByMonthAndYearWithUser(month, year);

        // 2. Map the list of entities to a list of DTOs
        return payrolls.stream()
                .map(this::toPayrollDto)
                .collect(Collectors.toList());
    }

    public Payroll getUserPayroll(Long userId, int month, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return payrollRepository.findByUserAndMonthAndYear(user, month, year)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
    }

    private PayrollDto toPayrollDto(Payroll payroll) {
        UserDto userDto = new UserDto();
        userDto.setId(payroll.getUser().getId());
        userDto.setFirstName(payroll.getUser().getFirstName());
        userDto.setLastName(payroll.getUser().getLastName());
        userDto.setEmail(payroll.getUser().getEmail());

        PayrollDto payrollDto = new PayrollDto();
        payrollDto.setId(payroll.getId());
        payrollDto.setMonth(payroll.getMonth());
        payrollDto.setYear(payroll.getYear());
        payrollDto.setRegularHours(payroll.getRegularHours());
        payrollDto.setOvertimeHours(payroll.getOvertimeHours());
        payrollDto.setWeekendOvertimeHours(payroll.getWeekendOvertimeHours());
        payrollDto.setHolidayOvertimeHours(payroll.getHolidayOvertimeHours());
        payrollDto.setRegularPay(payroll.getRegularPay());
        payrollDto.setOvertimePay(payroll.getOvertimePay());
        payrollDto.setWeekendOvertimePay(payroll.getWeekendOvertimePay());
        payrollDto.setHolidayOvertimePay(payroll.getHolidayOvertimePay());
        payrollDto.setTotalPay(payroll.getTotalPay());
        payrollDto.setPaymentDate(payroll.getPaymentDate());
        payrollDto.setStatus(payroll.getStatus());
        payrollDto.setUser(userDto); // Set the UserDto

        return payrollDto;
    }
}
