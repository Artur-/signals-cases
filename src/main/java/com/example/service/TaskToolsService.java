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

        boolean removed = context.removeTask(taskId);
        logger.info("✅ Task removal result: {}", removed ? "success" : "not found");
        return removed ? "Task removed successfully" : "Task not found with ID: " + taskId;
    }

    @Tool(description = "Update a task's title and/or description by its ID")
    public String updateTask(String taskId, String title, String description, ToolContext toolContext) {
        logger.info("🔧 Tool called: updateTask(taskId={}, title={}, description={})", taskId, title, description);

        TaskContext context = (TaskContext) toolContext.getContext().get("taskContext");
        if (context == null) {
            return "Error: Task context not available";
        }

        boolean updated = context.updateTask(taskId, title, description);
        logger.info("✅ Task update result: {}", updated ? "success" : "not found");
        return updated ? "Task updated successfully" : "Task not found with ID: " + taskId;
    }

    @Tool(description = "Mark a task as completed or not completed by its ID")
    public String markComplete(String taskId, boolean completed, ToolContext toolContext) {
        logger.info("🔧 Tool called: markComplete(taskId={}, completed={})", taskId, completed);

        TaskContext context = (TaskContext) toolContext.getContext().get("taskContext");
        if (context == null) {
            return "Error: Task context not available";
        }

        boolean updated = context.markComplete(taskId, completed);
        logger.info("✅ Task completion update result: {}", updated ? "success" : "not found");
        return updated ? "Task marked as " + (completed ? "completed" : "not completed")
                : "Task not found with ID: " + taskId;
    }

    @Tool(description = "Change a task's status (TODO, IN_PROGRESS, or DONE) by its ID")
    public String changeStatus(String taskId, String status, ToolContext toolContext) {
        logger.info("🔧 Tool called: changeStatus(taskId={}, status={})", taskId, status);

        TaskContext context = (TaskContext) toolContext.getContext().get("taskContext");
        if (context == null) {
            return "Error: Task context not available";
        }

        try {
            Task.TaskStatus taskStatus = Task.TaskStatus.valueOf(status.toUpperCase());
            boolean updated = context.changeStatus(taskId, taskStatus);
            logger.info("✅ Task status change result: {}", updated ? "success" : "not found");
            return updated ? "Task status changed to " + taskStatus : "Task not found with ID: " + taskId;
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
                .map(task -> String.format("- [%s] %s (Status: %s, Completed: %s, Due: %s)\n  Description: %s",
                        task.id(), task.title(), task.status(), task.completed(), task.dueDate(),
                        task.description()))
                .reduce((a, b) -> a + "\n\n" + b).orElse("");

        logger.info("✅ Listed {} tasks", tasks.size());
        return result;
    }
}
