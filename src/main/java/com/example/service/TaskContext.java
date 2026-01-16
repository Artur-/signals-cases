package com.example.service;

import java.time.LocalDate;
import java.util.List;

import com.example.model.Task;

/**
 * Interface for tool functions to access view-local task state.
 * The view provides an implementation that modifies the ListSignal.
 */
public interface TaskContext {

    List<Task> getAllTasks();

    void addTask(Task task);

    void removeTask(String taskId);

    void updateTask(String taskId, String title, String description);

    void changeStatus(String taskId, Task.TaskStatus status);

    void updateDueDate(String taskId, LocalDate dueDate);
}
