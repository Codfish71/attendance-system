package com.geeksmetrics.task_mgmt.dto;

import com.geeksmetrics.attendance_mgmt.dto.UserDto;
import com.geeksmetrics.task_mgmt.entity.TaskPriority;
import com.geeksmetrics.task_mgmt.entity.TaskStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private UserDto assignedBy;
    private UserDto assignedTo;
    private LocalDateTime taskDateTime;
    private String location;
    private Double locationLatitude;
    private Double locationLongitude;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String completionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
