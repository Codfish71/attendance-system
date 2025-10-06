package com.geeksmetrics.attendance_mgmt.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "company_settings")
@Data
public class CompanySettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double officeLatitude;

    @Column(nullable = false)
    private Double officeLongitude;

    @Column(nullable = false)
    private Double proximityRadiusMeters = 100.0;

    @Column(nullable = false)
    private Double standardWorkHoursPerDay = 8.0;

    @Column(nullable = false)
    private Integer paymentDay = 26;
}
