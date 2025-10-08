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
    Optional<Payroll> findByUserAndMonthAndYear(User user, Integer month, Integer year);

    // This is the original method, which we will no longer use for this endpoint
    List<Payroll> findByMonthAndYear(Integer month, Integer year);

    // Add this new, more efficient method
    @Query("SELECT p FROM Payroll p JOIN FETCH p.user WHERE p.month = :month AND p.year = :year")
    List<Payroll> findByMonthAndYearWithUser(@Param("month") Integer month, @Param("year") Integer year);

    List<Payroll> findByStatus(PayrollStatus status);
}