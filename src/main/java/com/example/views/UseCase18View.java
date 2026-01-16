package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.util.UUID;

import com.example.model.Task;
import com.example.service.TaskLLMService;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.ListSignal;

@Route(value = "use-case-18", layout = MainLayout.class)
@PageTitle("Use Case 18: LLM-Powered Task List")
@Menu(order = 18, title = "UC 18: LLM Task List")
@PermitAll
public class UseCase18View extends AbstractTaskChatView {

    public UseCase18View(TaskLLMService taskLLMService) {
        super(
            new ListSignal<>(Task.class),                    // View-local task signal
            new ListSignal<>(ChatMessageData.class),         // View-local chat signal
            taskLLMService,
            UUID.randomUUID().toString()                     // Per-instance conversation ID
        );

        // Initialize sample tasks for single-user view
        tasksSignal.insertLast(Task.create("Review pull requests",
            "Review and merge pending pull requests")
            .withDueDate(LocalDate.now().plusDays(2)));
        tasksSignal.insertLast(Task.create("Write unit tests",
            "Add unit tests for new features")
            .withStatus(Task.TaskStatus.IN_PROGRESS)
            .withDueDate(LocalDate.now().plusDays(5)));
        tasksSignal.insertLast(Task.create("Deploy to staging",
            "Deploy latest changes to staging environment")
            .withDueDate(LocalDate.now().plusDays(7)));
    }
}
