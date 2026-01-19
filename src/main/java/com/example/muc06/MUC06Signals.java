package com.example.muc06;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import com.vaadin.signals.ListSignal;

/**
 * Application-scoped signals for MUC06: Shared Task List
 */
@Component
public class MUC06Signals {

    public record Task(String id, String title, boolean completed,
            java.time.LocalDate dueDate) {
    }

    private final ListSignal<Task> tasksSignal = new ListSignal<>(Task.class);

    public ListSignal<Task> getTasksSignal() {
        return tasksSignal;
    }

    @PostConstruct
    public void initializeSampleTasks() {
        if (tasksSignal.value().isEmpty()) {
            tasksSignal.insertLast(new Task("task-1", "Review pull requests",
                    false, java.time.LocalDate.now()));
            tasksSignal.insertLast(new Task("task-2", "Update documentation",
                    true, java.time.LocalDate.now().plusDays(1)));
            tasksSignal.insertLast(new Task("task-3", "Fix bug #123", false,
                    java.time.LocalDate.now().plusDays(2)));
            tasksSignal.insertLast(new Task("task-4", "Prepare demo", false,
                    java.time.LocalDate.now().plusWeeks(1)));
        }
    }
}
