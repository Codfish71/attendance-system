package com.geeksmetrics.attendance_mgmt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;

    private Double clockInLatitude;
    private Double clockInLongitude;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.PENDING;

    public enum AttendanceStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
