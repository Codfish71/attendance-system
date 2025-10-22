package com.geeksmetrics.attendance_mgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Data
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    @Column(nullable = false)
    private Double checkInLatitude;

    @Column(nullable = false)
    private Double checkInLongitude;

    private Double checkOutLatitude;
    private Double checkOutLongitude;

    private Double hoursWorked;
    private Double regularHours;
    private Double overtimeHours;
    private Double weekendOvertimeHours;
    private Double holidayOvertimeHours;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.PROCESSING;

    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}