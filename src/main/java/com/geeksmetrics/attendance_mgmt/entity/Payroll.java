package com.geeksmetrics.attendance_mgmt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Getter @Setter
public class Payroll {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "user_id")
    private User user;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private Double totalHours;
    private Double overtimeHours;
    private Double grossPay;
    private LocalDate paymentDate;
}