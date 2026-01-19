package com.example.muc07;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import com.example.model.Task;
import com.example.usecase18.ChatMessageData;
import com.vaadin.signals.ListSignal;

/**
 * Application-scoped signals for MUC07: LLM-Powered Shared Task List
 */
@Component
public class MUC07Signals {

    private final ListSignal<Task> llmTasksSignal = new ListSignal<>(
            Task.class);
    private final ListSignal<ChatMessageData> llmChatMessagesSignal = new ListSignal<>(
            ChatMessageData.class);

    public ListSignal<Task> getLlmTasksSignal() {
        return llmTasksSignal;
    }

    public ListSignal<ChatMessageData> getLlmChatMessagesSignal() {
        return llmChatMessagesSignal;
    }

    @PostConstruct
    public void initializeSampleLLMTasks() {
        if (llmTasksSignal.value().isEmpty()) {
            llmTasksSignal.insertLast(
                    Task.create("Review pull requests",
                                    "Review and merge pending pull requests")
                            .withDueDate(java.time.LocalDate.now().plusDays(2)));
            llmTasksSignal.insertLast(Task
                    .create("Write unit tests",
                            "Add unit tests for new features")
                    .withStatus(Task.TaskStatus.IN_PROGRESS)
                    .withDueDate(java.time.LocalDate.now().plusDays(5)));
            llmTasksSignal.insertLast(Task
                    .create("Deploy to staging",
                            "Deploy latest changes to staging environment")
                    .withDueDate(java.time.LocalDate.now().plusDays(7)));
        }
    }
}
