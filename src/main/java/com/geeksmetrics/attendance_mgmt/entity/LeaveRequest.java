package com.geeksmetrics.attendance_mgmt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LeaveType type;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;

    public enum LeaveType {
        PAID,
        UNPAID
    }

    public enum LeaveStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
