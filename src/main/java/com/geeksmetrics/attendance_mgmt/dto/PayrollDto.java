package com.geeksmetrics.attendance_mgmt.dto;

import com.geeksmetrics.attendance_mgmt.entity.PayrollStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PayrollDto {
    private Long id;
    private UserDto user; // Notice this is now UserDto
    private Integer month;
    private Integer year;
    private Double regularHours;
    private Double overtimeHours;
    private Double weekendOvertimeHours;
    private Double holidayOvertimeHours;
    private Double regularPay;
    private Double overtimePay;
    private Double weekendOvertimePay;
    private Double holidayOvertimePay;
    private Double totalPay;
    private LocalDate paymentDate;
    private PayrollStatus status;
}
