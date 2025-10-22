package com.geeksmetrics.task_mgmt.service;

import com.geeksmetrics.attendance_mgmt.entity.Role;
import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import com.geeksmetrics.task_mgmt.entity.Task;
import com.geeksmetrics.task_mgmt.entity.TaskStatus;
import com.geeksmetrics.task_mgmt.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public Task createTask(Long assignedById, Task task) {
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Assigned by user not found"));

        User assignedTo = userRepository.findById(task.getAssignedTo().getId())
                .orElseThrow(() -> new RuntimeException("Assigned to user not found"));

        // Validate hierarchy
        if (!canAssignTask(assignedBy, assignedTo)) {
            throw new RuntimeException("You cannot assign tasks to this user");
        }

        task.setAssignedBy(assignedBy);
        task.setAssignedTo(assignedTo);
        task.setStatus(TaskStatus.ASSIGNED);

        return taskRepository.save(task);
    }

    private boolean canAssignTask(User assignedBy, User assignedTo) {
        // Project Manager can assign to Site Lead
        if (assignedBy.getRoles().contains(Role.ROLE_PROJECT_MANAGER) &&
                assignedTo.getRoles().contains(Role.ROLE_SITE_LEAD)) {
            return true;
        }

        // Site Lead can assign to Coordinator
        if (assignedBy.getRoles().contains(Role.ROLE_SITE_LEAD) &&
                assignedTo.getRoles().contains(Role.ROLE_COORDINATOR)) {
            return true;
        }

        return false;
    }

    @Transactional
    public Task startTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getAssignedTo().getId().equals(userId)) {
            throw new RuntimeException("You are not assigned to this task");
        }

        if (task.getStatus() != TaskStatus.ASSIGNED) {
            throw new RuntimeException("Task cannot be started in current status");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Transactional
    public Task completeTask(Long taskId, Long userId, String completionNotes) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getAssignedTo().getId().equals(userId)) {
            throw new RuntimeException("You are not assigned to this task");
        }

        task.setStatus(TaskStatus.DONE);
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletionNotes(completionNotes);

        return taskRepository.save(task);
    }

    public List<Task> getMyTasks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return taskRepository.findByAssignedTo(user);
    }

    public List<Task> getAssignedTasks(Long userId) {
        return taskRepository.findByAssignedById(userId);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }
}