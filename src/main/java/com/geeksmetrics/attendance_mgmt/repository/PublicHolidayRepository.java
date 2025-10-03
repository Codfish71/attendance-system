package com.geeksmetrics.attendance_mgmt.repository;

import com.geeksmetrics.attendance_mgmt.entity.PublicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {
    List<PublicHoliday> findByHolidayDateBetween(LocalDate start, LocalDate end);
}
