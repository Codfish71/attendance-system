package com.geeksmetrics.task_mgmt.repository;

import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.task_mgmt.entity.Task;
import com.geeksmetrics.task_mgmt.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(User user);

    List<Task> findByAssignedBy(User user);

    List<Task> findByStatus(TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.status = :status")
    List<Task> findByAssignedToIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignedBy.id = :userId")
    List<Task> findByAssignedById(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.taskDateTime BETWEEN :start AND :end")
    List<Task> findByAssignedToIdAndTaskDateTimeBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

