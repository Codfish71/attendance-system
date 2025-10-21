package com.geeksmetrics.attendance_mgmt.mapper;


import com.geeksmetrics.attendance_mgmt.dto.PayrollDto;
import com.geeksmetrics.attendance_mgmt.dto.UserDto;
import com.geeksmetrics.attendance_mgmt.entity.Payroll;
import com.geeksmetrics.attendance_mgmt.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {

    private final UserMapper userMapper; // Inject UserMapper

    public PayrollMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public PayrollDto toDto(Payroll payroll) {
        if (payroll == null) {
            return null;
        }

        PayrollDto dto = new PayrollDto();
        dto.setId(payroll.getId());
        dto.setMonth(payroll.getMonth());
        dto.setYear(payroll.getYear());
        dto.setRegularHours(payroll.getRegularHours());
        dto.setOvertimeHours(payroll.getOvertimeHours());
        dto.setWeekendOvertimeHours(payroll.getWeekendOvertimeHours());
        dto.setHolidayOvertimeHours(payroll.getHolidayOvertimeHours());
        dto.setRegularPay(payroll.getRegularPay());
        dto.setOvertimePay(payroll.getOvertimePay());
        dto.setWeekendOvertimePay(payroll.getWeekendOvertimePay());
        dto.setHolidayOvertimePay(payroll.getHolidayOvertimePay());
        dto.setTotalPay(payroll.getTotalPay());
        dto.setPaymentDate(payroll.getPaymentDate());
        dto.setStatus(payroll.getStatus());

        // Map the associated user to its DTO using UserMapper
        if (payroll.getUser() != null) {
            dto.setUser(userMapper.toDto(payroll.getUser()));
        }

        return dto;
    }
}
