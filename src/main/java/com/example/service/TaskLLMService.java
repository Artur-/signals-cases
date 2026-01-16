package com.example.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class TaskLLMService {

    private final ChatClient chatClient;
    private final TaskToolsService taskToolsService;
    private final ChatMemory chatMemory;

    public TaskLLMService(ChatClient.Builder chatClientBuilder, TaskToolsService taskToolsService) {
        this.taskToolsService = taskToolsService;
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public Flux<String> streamMessage(String userMessage, TaskContext context, String conversationId) {
        String systemPrompt = buildSystemPrompt(context);

        // Stream response with tool calling support and conversation memory
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(taskToolsService)  // Register @Tool methods
                .toolContext(Map.of("taskContext", context))  // Pass view-local context
                .stream()
                .content();
    }

    private String buildSystemPrompt(TaskContext context) {
        // Include current task list in the prompt
        String taskList = context.getAllTasks().stream()
                .map(task -> String.format("- [%s] %s (Status: %s, Due: %s)", task.id(), task.title(),
                        task.status(), task.dueDate()))
                .reduce("", (a, b) -> a + "\n" + b);

        return """
                You are a helpful task management assistant. You can help users manage their tasks.

                Current tasks:
                %s

                Task status can be TODO, IN_PROGRESS, or DONE. DONE means the task is completed.
                When users ask to manage tasks, use the appropriate tools.
                Be conversational and confirm actions after executing them.
                """.formatted(taskList.isBlank() ? "No tasks yet." : taskList);
    }
}
