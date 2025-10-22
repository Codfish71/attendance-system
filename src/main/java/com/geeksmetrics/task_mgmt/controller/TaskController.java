package com.geeksmetrics.task_mgmt.controller;

import com.geeksmetrics.attendance_mgmt.dto.UserPrincipal;
import com.geeksmetrics.task_mgmt.dto.TaskDto;
import com.geeksmetrics.task_mgmt.entity.Task;
import com.geeksmetrics.task_mgmt.mapper.TaskMapper;
import com.geeksmetrics.task_mgmt.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'SITE_LEAD')")
    public ResponseEntity<TaskDto> createTask(@RequestBody Task task, Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        TaskDto createdTask = taskService.createTask(userId, task);
        return ResponseEntity.ok(createdTask);
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'SITE_LEAD', 'PROJECT_MANAGER')")
    public ResponseEntity<List<TaskDto>> getMyTasks(Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        List<Task> tasks = taskService.getMyTasks(userId);
        List<TaskDto> taskDtos = tasks.stream()
                .map(taskMapper::toDto)
                .toList();
        return ResponseEntity.ok(taskDtos);
    }

    @GetMapping("/assigned-by-me")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'SITE_LEAD')")
    public ResponseEntity<List<TaskDto>> getAssignedTasks(Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        List<Task> tasks = taskService.getAssignedTasks(userId);
        List<TaskDto> taskDtos = tasks.stream()
                .map(taskMapper::toDto)
                .toList();
        return ResponseEntity.ok(taskDtos);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'SITE_LEAD')")
    public ResponseEntity<TaskDto> startTask(@PathVariable Long id, Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        TaskDto task = taskService.startTask(id, userId);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'SITE_LEAD')")
    public ResponseEntity<TaskDto> completeTask(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
        String completionNotes = body.get("completionNotes");
        TaskDto task = taskService.completeTask(id, userId, completionNotes);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }
}