package com.geeksmetrics.attendance_mgmt.repository;

import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findTopByUserAndClockOutTimeIsNullOrderByClockInTimeDesc(User user);
    List<Attendance> findByStatus(Attendance.AttendanceStatus status);
    List<Attendance> findByUserAndClockInTimeBetween(User user, LocalDateTime start, LocalDateTime end);
}

