package com.geeksmetrics.attendance_mgmt.repository;

import com.geeksmetrics.attendance_mgmt.entity.Payroll;
import com.geeksmetrics.attendance_mgmt.entity.PayrollStatus;
import com.geeksmetrics.attendance_mgmt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    // Original method - might cause N+1 if user is lazy and accessed outside transaction
    // Optional<Payroll> findByUserAndMonthAndYear(User user, Integer month, Integer year);

    // New method for single payroll with user fetched
    @Query("SELECT p FROM Payroll p JOIN FETCH p.user WHERE p.user = :user AND p.month = :month AND p.year = :year")
    Optional<Payroll> findByUserAndMonthAndYearWithUser(@Param("user") User user, @Param("month") Integer month, @Param("year") Integer year);


    // This is the original method, which we will no longer use for this endpoint
    List<Payroll> findByMonthAndYear(Integer month, Integer year);

    // Add this new, more efficient method (already done)
    @Query("SELECT p FROM Payroll p JOIN FETCH p.user WHERE p.month = :month AND p.year = :year")
    List<Payroll> findByMonthAndYearWithUser(@Param("month") Integer month, @Param("year") Integer year);

    List<Payroll> findByStatus(PayrollStatus status);

    // Add a method to fetch a single payroll by ID with its user
    @Query("SELECT p FROM Payroll p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Payroll> findByIdWithUser(@Param("id") Long id);
}