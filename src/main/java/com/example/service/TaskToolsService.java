package com.example.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import com.example.model.Task;

/**
 * Service containing @Tool annotated methods for LLM to manage tasks.
 * Uses Spring AI 2.0's ToolContext to access view-local state.
 */
@Service
public class TaskToolsService {

    private static final Logger logger = LoggerFactory.getLogger(TaskToolsService.class);

    @Tool(description = "Add a new task with title, description, and optional due date (format: YYYY-MM-DD)")
    public String addTask(String title, String description, String dueDate, ToolContext toolContext) {
        logger.info("🔧 Tool called: addTask(title={}, description={}, dueDate={})", title, description, dueDate);

        TaskContext context = (TaskContext) toolContext.getContext().get("taskContext");
        if (context == null) {
            return "Error: Task context not available";
        }

        Task newTask = Task.create(title, description);
        if (dueDate != null && !dueDate.isBlank()) {
            try {
                newTask = newTask.withDueDate(LocalDate.parse(dueDate));
            } catch (Exception e) {
                return "Error parsing due date. Please use YYYY-MM-DD format.";
            }
        }

        context.addTask(newTask);
        logger.info("✅ Task added successfully: {}", newTask.title());
        return "Task added: " + newTask.title();
    }

    @Tool(description = "Remove a task by its ID")
    public String removeTask(String taskId, ToolContext toolContext) {
        logger.info("🔧 Tool called: removeTask(taskId={})", taskId);

        TaskContext context = (TaskContext) toolContext.getContext().get("taskContext");
        if (context == null) {
            return "Error: Task context not available";
        }

        context.removeTask(taskId);
        logger.info("✅ Task removed");
        return "Task removed successfully";
    }

    @Tool(description = "Update a task's title and/or description by its ID")
    public String updateTask(String taskId, String title, String description, ToolContext toolContext) {
        logger.info("🔧 Tool called: updateTask(taskId={}, title={}, description={})", taskId, title, description);

        TaskContext context = (TaskContext) toolContext.getContext().get("taskContext");
        if (context == null) {
            return "Error: Task context not available";
        }

        context.updateTask(taskId, title, description);
        logger.info("✅ Task updated");
        return "Task updated successfully";
    }

    @Tool(description = "Change a task's status by its ID. Use DONE to mark as completed, TODO to reopen a task, or IN_PROGRESS for tasks being worked on.")
    public String changeStatus(String taskId, String status, ToolContext toolContext) {
        logger.info("🔧 Tool called: changeStatus(taskId={}, status={})", taskId, status);

        TaskContext context = (TaskContext) toolContext.getContext().get("taskContext");
        if (context == null) {
            return "Error: Task context not available";
        }

        try {
            Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
            context.changeStatus(taskId, taskStatus);
            logger.info("✅ Task status changed to {}", taskStatus);
            return "Task status changed to " + taskStatus;
        } catch (IllegalArgumentException e) {
            return "Invalid status. Please use TODO, IN_PROGRESS, or DONE.";
        }
    }

    @Tool(description = "List all current tasks with their details")
    public String listTasks(ToolContext toolContext) {
        logger.info("🔧 Tool called: listTasks()");

        TaskContext context = (TaskContext) toolContext.getContext().get("taskContext");
        if (context == null) {
            return "Error: Task context not available";
        }

        var tasks = context.getAllTasks();
        if (tasks.isEmpty()) {
            return "No tasks found.";
        }

        String result = tasks.stream()
                .map(task -> String.format("- [%s] %s (Status: %s, Due: %s)\n  Description: %s",
                        task.id(), task.title(), task.status(), task.dueDate(), task.description()))
                .reduce((a, b) -> a + "\n\n" + b).orElse("");

        logger.info("✅ Listed {} tasks", tasks.size());
        return result;
    }
}
