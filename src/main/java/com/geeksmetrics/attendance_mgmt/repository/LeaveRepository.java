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
import java.util.Optional;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    // --- Existing Methods ---
    List<Leave> findByStatus(LeaveStatus status);
    List<Leave> findByUserAndStatus(User user, LeaveStatus status);

    @Query("SELECT l FROM Leave l WHERE l.user.id = :userId AND l.status = 'APPROVED' AND ((l.startDate <= :endDate) AND (l.endDate >= :startDate))")
    List<Leave> findApprovedLeavesByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    // --- New, More Efficient Methods for DTO Mapping ---

    /**
     * Finds a leave record by its ID, eagerly fetching the associated user and approver.
     */
    @Query("SELECT l FROM Leave l JOIN FETCH l.user LEFT JOIN FETCH l.approvedBy WHERE l.id = :id")
    Optional<Leave> findByIdWithUserAndApprover(@Param("id") Long id);

    /**
     * Finds all leave records with a given status, eagerly fetching the associated user.
     */
    @Query("SELECT l FROM Leave l JOIN FETCH l.user WHERE l.status = :status")
    List<Leave> findByStatusWithUser(@Param("status") LeaveStatus status);

    /**
     * Finds all leave records for a user, eagerly fetching the user.
     */
    @Query("SELECT l FROM Leave l JOIN FETCH l.user WHERE l.user = :user")
    List<Leave> findByUserWithUser(@Param("user") User user);
}