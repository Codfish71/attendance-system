package com.geeksmetrics.attendance_mgmt.repository;

import com.geeksmetrics.attendance_mgmt.entity.CompanyConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyConfigRepository extends JpaRepository<CompanyConfig, Integer> {
}
