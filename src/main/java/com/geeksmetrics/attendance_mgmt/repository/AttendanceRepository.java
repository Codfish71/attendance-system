package com.geeksmetrics.attendance_mgmt.repository;

import com.geeksmetrics.attendance_mgmt.entity.Attendance;
import com.geeksmetrics.attendance_mgmt.entity.AttendanceStatus;
import com.geeksmetrics.attendance_mgmt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end);

    List<Attendance> findByStatus(AttendanceStatus status);

    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId AND a.checkOut IS NULL ORDER BY a.checkIn DESC")
    Optional<Attendance> findActiveAttendanceByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Attendance a WHERE a.status = :status AND a.user.id = :userId")
    List<Attendance> findByStatusAndUserId(@Param("status") AttendanceStatus status, @Param("userId") Long userId);

    @Query("SELECT a FROM Attendance a WHERE YEAR(a.checkIn) = :year AND MONTH(a.checkIn) = :month AND a.user.id = :userId AND a.status = 'APPROVED'")
    List<Attendance> findApprovedAttendanceByUserAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
}

