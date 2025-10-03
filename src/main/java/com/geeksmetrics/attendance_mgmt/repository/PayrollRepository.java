package com.geeksmetrics.attendance_mgmt.repository;

import com.geeksmetrics.attendance_mgmt.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRepository extends JpaRepository<Payroll, Integer> {
}
