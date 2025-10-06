package com.geeksmetrics.attendance_mgmt.repository;


import com.geeksmetrics.attendance_mgmt.entity.Payroll;
import com.geeksmetrics.attendance_mgmt.entity.PayrollStatus;
import com.geeksmetrics.attendance_mgmt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByUserAndMonthAndYear(User user, Integer month, Integer year);

    List<Payroll> findByMonthAndYear(Integer month, Integer year);

    List<Payroll> findByStatus(PayrollStatus status);
}
