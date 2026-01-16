package com.example.service;

import java.util.List;

import com.example.model.Task;

/**
 * Interface for tool functions to access view-local task state.
 * The view provides an implementation that modifies the ListSignal.
 */
public interface TaskContext {

    List<Task> getAllTasks();

    void addTask(Task task);

    boolean removeTask(String taskId);

    boolean updateTask(String taskId, String title, String description);

    boolean markComplete(String taskId, boolean completed);

    boolean changeStatus(String taskId, Task.TaskStatus status);
}
