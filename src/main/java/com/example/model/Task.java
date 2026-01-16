package com.example.model;

import java.time.LocalDate;
import java.util.UUID;

public record Task(String id, String title, String description, TaskStatus status, LocalDate dueDate) {

    public enum TaskStatus {
        TODO, IN_PROGRESS, DONE
    }

    public static Task create(String title, String description) {
        return new Task(UUID.randomUUID().toString(), title, description, TaskStatus.TODO,
                LocalDate.now().plusDays(7));
    }

    public boolean isCompleted() {
        return status == TaskStatus.DONE;
    }

    public Task withStatus(TaskStatus newStatus) {
        return new Task(id, title, description, newStatus, dueDate);
    }

    public Task withTitle(String newTitle) {
        return new Task(id, newTitle, description, status, dueDate);
    }

    public Task withDescription(String newDescription) {
        return new Task(id, title, newDescription, status, dueDate);
    }

    public Task withDueDate(LocalDate newDueDate) {
        return new Task(id, title, description, status, newDueDate);
    }
}
