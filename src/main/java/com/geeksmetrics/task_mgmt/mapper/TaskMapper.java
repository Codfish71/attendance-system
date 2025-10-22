package com.geeksmetrics.task_mgmt.mapper;

import com.geeksmetrics.attendance_mgmt.mapper.UserMapper;
import com.geeksmetrics.task_mgmt.dto.TaskDto;
import com.geeksmetrics.task_mgmt.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final UserMapper userMapper;

    public TaskDto toDto(Task task) {
        if (task == null) {
            return null;
        }

        TaskDto dto = new TaskDto();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setTaskDateTime(task.getTaskDateTime());
        dto.setLocation(task.getLocation());
        dto.setLocationLatitude(task.getLocationLatitude());
        dto.setLocationLongitude(task.getLocationLongitude());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setStartedAt(task.getStartedAt());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCompletionNotes(task.getCompletionNotes());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        // Use the injected UserMapper to convert User entities to UserDto
        if (task.getAssignedBy() != null) {
            dto.setAssignedBy(userMapper.toDto(task.getAssignedBy()));
        }
        if (task.getAssignedTo() != null) {
            dto.setAssignedTo(userMapper.toDto(task.getAssignedTo()));
        }

        return dto;
    }
}
