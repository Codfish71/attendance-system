package com.geeksmetrics.attendance_mgmt.repository;

import com.geeksmetrics.attendance_mgmt.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status);
}
