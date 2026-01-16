package com.example.model;

import java.time.LocalDate;
import java.util.UUID;

public record Task(String id, String title, String description, TaskStatus status, boolean completed,
        LocalDate dueDate) {

    public enum TaskStatus {
        TODO, IN_PROGRESS, DONE
    }

    public static Task create(String title, String description) {
        return new Task(UUID.randomUUID().toString(), title, description, TaskStatus.TODO, false,
                LocalDate.now().plusDays(7));
    }

    public Task withStatus(TaskStatus newStatus) {
        return new Task(id, title, description, newStatus, completed, dueDate);
    }

    public Task withCompleted(boolean newCompleted) {
        return new Task(id, title, description, status, newCompleted, dueDate);
    }

    public Task withTitle(String newTitle) {
        return new Task(id, newTitle, description, status, completed, dueDate);
    }

    public Task withDescription(String newDescription) {
        return new Task(id, title, newDescription, status, completed, dueDate);
    }

    public Task withDueDate(LocalDate newDueDate) {
        return new Task(id, title, description, status, completed, newDueDate);
    }
}
