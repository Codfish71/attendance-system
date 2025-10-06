package com.geeksmetrics.attendance_mgmt.repository;

import com.geeksmetrics.attendance_mgmt.entity.Leave;
import com.geeksmetrics.attendance_mgmt.entity.LeaveStatus;
import com.geeksmetrics.attendance_mgmt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByUserAndStatus(User user, LeaveStatus status);

    List<Leave> findByStatus(LeaveStatus status);

    @Query("SELECT l FROM Leave l WHERE l.user.id = :userId AND l.status = 'APPROVED' AND ((l.startDate <= :endDate) AND (l.endDate >= :startDate))")
    List<Leave> findApprovedLeavesByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}