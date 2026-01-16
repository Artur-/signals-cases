package com.example.views;

import java.time.Instant;
import java.time.LocalDate;

import com.example.MissingAPI;
import com.example.model.Task;
import com.example.security.CurrentUserSignal;
import com.example.service.TaskContext;
import com.example.service.TaskLLMService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

public abstract class AbstractTaskChatView extends VerticalLayout {

    public record ChatMessageData(String role, String content, Instant timestamp) {
    }

    // Signals injected via constructor
    protected final ListSignal<Task> tasksSignal;
    protected final ListSignal<ChatMessageData> chatMessagesSignal;
    protected final String conversationId;
    protected final CurrentUserSignal currentUserSignal;

    // Computed signals for statistics
    private Signal<Integer> totalTasksSignal;
    private Signal<Integer> completedTasksSignal;
    private Signal<Integer> pendingTasksSignal;

    // Services
    protected final TaskLLMService taskLLMService;

    // UI Components
    private MessageList messageList;
    private MessageInput messageInput;

    // Signals for UI state
    private final WritableSignal<Boolean> messageInputEnabledSignal = new ValueSignal<>(true);

    // Constructor with signal injection
    protected AbstractTaskChatView(
            ListSignal<Task> tasksSignal,
            ListSignal<ChatMessageData> chatMessagesSignal,
            TaskLLMService taskLLMService,
            String conversationId,
            CurrentUserSignal currentUserSignal) {

        this.tasksSignal = tasksSignal;
        this.chatMessagesSignal = chatMessagesSignal;
        this.taskLLMService = taskLLMService;
        this.conversationId = conversationId;
        this.currentUserSignal = currentUserSignal;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Set up computed signals
        totalTasksSignal = tasksSignal.map(list -> list.size());
        completedTasksSignal = Signal.computed(
                () -> (int) tasksSignal.value().stream().filter(t -> t.value().isCompleted()).count());
        pendingTasksSignal = Signal.computed(() -> totalTasksSignal.value() - completedTasksSignal.value());

        // Build UI - Statistics on top, then AI and Grid side by side
        HorizontalLayout statsSection = buildStatisticsSection();
        HorizontalLayout mainPanel = buildMainPanel();

        add(statsSection);
        add(mainPanel);

        setFlexGrow(0, statsSection);  // Don't grow
        setFlexGrow(1, mainPanel);     // Take remaining space
    }

    private HorizontalLayout buildMainPanel() {
        HorizontalLayout mainPanel = new HorizontalLayout();
        mainPanel.setWidthFull();
        mainPanel.setHeightFull();
        mainPanel.setSpacing(true);
        mainPanel.setAlignItems(Alignment.STRETCH);
        mainPanel.getStyle().set("min-height", "0");

        // Left side: AI Chat
        VerticalLayout chatPanel = buildChatPanel();

        // Right side: Task Grid
        VerticalLayout taskPanel = buildTaskPanel();

        mainPanel.add(chatPanel, taskPanel);
        mainPanel.setFlexGrow(1, chatPanel);   // 50% of horizontal space
        mainPanel.setFlexGrow(1, taskPanel);   // 50% of horizontal space

        return mainPanel;
    }

    private VerticalLayout buildChatPanel() {
        VerticalLayout chatPanel = new VerticalLayout();
        chatPanel.setHeightFull();
        chatPanel.setPadding(false);
        chatPanel.setSpacing(true);
        chatPanel.getStyle().set("min-width", "0");

        H2 chatTitle = new H2("AI Task Assistant");
        chatTitle.getStyle().set("margin", "0");

        Paragraph chatDescription = new Paragraph(
                "Chat with the AI to manage your tasks. Try commands like 'Add a task to buy groceries' or 'List my tasks'.");

        VerticalLayout chatSection = buildChatSection();
        chatPanel.add(chatTitle, chatDescription, chatSection);
        chatPanel.setFlexGrow(1, chatSection);

        return chatPanel;
    }

    private VerticalLayout buildTaskPanel() {
        VerticalLayout taskPanel = new VerticalLayout();
        taskPanel.setHeightFull();
        taskPanel.setPadding(false);
        taskPanel.setSpacing(true);
        taskPanel.getStyle().set("min-width", "0");

        H2 title = new H2("Task Management");
        title.getStyle().set("margin", "0");

        VerticalLayout gridContainer = buildTaskGrid();

        Button addTaskButton = new Button("Add New Task", VaadinIcon.PLUS.create());
        addTaskButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTaskButton.addClickListener(e -> openAddTaskDialog());

        taskPanel.add(title, gridContainer, addTaskButton);
        taskPanel.setFlexGrow(1, gridContainer);

        return taskPanel;
    }

    private HorizontalLayout buildStatisticsSection() {
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);

        Div totalCard = createStatCard("Total", totalTasksSignal, "var(--lumo-primary-color)");
        Div completedCard = createStatCard("Completed", completedTasksSignal, "var(--lumo-success-color)");
        Div pendingCard = createStatCard("Pending", pendingTasksSignal, "var(--lumo-contrast-60pct)");

        statsLayout.add(totalCard, completedCard, pendingCard);
        return statsLayout;
    }

    private Div createStatCard(String label, Signal<Integer> valueSignal, String color) {
        Div card = new Div();
        card.getStyle().set("flex", "1").set("background-color", "#f5f5f5").set("padding", "1em")
                .set("border-radius", "8px").set("text-align", "center");

        Span valueLabel = new Span();
        valueLabel.bindText(valueSignal.map(String::valueOf));
        valueLabel.getStyle().set("font-size", "2em").set("font-weight", "bold").set("color", color)
                .set("display", "block");

        Span titleLabel = new Span(label);
        titleLabel.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.875em");

        card.add(valueLabel, titleLabel);
        return card;
    }

    private void openAddTaskDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Task");
        dialog.setWidth("500px");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        TextField titleField = new TextField("Title");
        titleField.setPlaceholder("Enter task title");
        titleField.setWidthFull();
        titleField.setAutofocus(true);

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setPlaceholder("Enter task description");
        descriptionField.setWidthFull();
        descriptionField.setMaxHeight("150px");

        DatePicker dueDatePicker = new DatePicker("Due Date");
        dueDatePicker.setValue(LocalDate.now().plusDays(7));
        dueDatePicker.setWidthFull();

        formLayout.add(titleField, descriptionField, dueDatePicker);

        Button saveButton = new Button("Add Task", e -> {
            String title = titleField.getValue();
            String description = descriptionField.getValue();
            LocalDate dueDate = dueDatePicker.getValue();

            if (title != null && !title.isBlank()) {
                Task newTask = Task.create(title, description).withDueDate(dueDate);
                tasksSignal.insertLast(newTask);
                dialog.close();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.add(formLayout);
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private VerticalLayout buildTaskGrid() {
        VerticalLayout gridContainer = new VerticalLayout();
        gridContainer.setWidthFull();
        gridContainer.setPadding(false);
        gridContainer.setSpacing(false);
        gridContainer.getStyle().set("min-height", "0");

        Grid<Task> grid = new Grid<>(Task.class, false);
        grid.addColumn(Task::title).setHeader("Title").setFlexGrow(2).setAutoWidth(true);
        grid.addColumn(Task::description).setHeader("Description").setFlexGrow(3);
        grid.addColumn(Task::status).setHeader("Status").setFlexGrow(1).setAutoWidth(true);
        grid.addColumn(Task::dueDate).setHeader("Due Date").setFlexGrow(1).setAutoWidth(true);

        grid.addComponentColumn(task -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editButton.addClickListener(e -> openEditDialog(task));

            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.addClickListener(e -> {
                tasksSignal.value().stream().filter(sig -> sig.value().equals(task)).findFirst()
                        .ifPresent(tasksSignal::remove);
            });

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setFlexGrow(0).setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        // Bind directly to ListSignal - it will register dependencies on all individual ValueSignals
        MissingAPI.bindItems(grid, tasksSignal);

        gridContainer.add(grid);
        gridContainer.setFlexGrow(1, grid);
        return gridContainer;
    }

    private void openEditDialog(Task task) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Task");
        dialog.setWidth("500px");

        FormLayout formLayout = new FormLayout();

        TextField titleField = new TextField("Title");
        titleField.setValue(task.title());
        titleField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setValue(task.description());
        descriptionField.setWidthFull();
        descriptionField.setMaxHeight("100px");

        ComboBox<Task.TaskStatus> statusCombo = new ComboBox<>("Status");
        statusCombo.setItems(Task.TaskStatus.values());
        statusCombo.setValue(task.status());
        statusCombo.setWidthFull();

        DatePicker dueDatePicker = new DatePicker("Due Date");
        dueDatePicker.setValue(task.dueDate());
        dueDatePicker.setWidthFull();

        formLayout.add(titleField, descriptionField, statusCombo, dueDatePicker);

        Button saveButton = new Button("Save", e -> {
            // Find the signal for this task and update it
            tasksSignal.value().stream().filter(sig -> sig.value().id().equals(task.id())).findFirst().ifPresent(sig -> {
                Task updatedTask = new Task(task.id(), titleField.getValue(), descriptionField.getValue(),
                        statusCombo.getValue(), dueDatePicker.getValue());
                sig.value(updatedTask);
            });
            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.add(formLayout);
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private VerticalLayout buildChatSection() {
        VerticalLayout chatContainer = new VerticalLayout();
        chatContainer.setWidthFull();
        chatContainer.setPadding(false);
        chatContainer.setSpacing(true);
        chatContainer.getStyle().set("min-height", "0");

        // Message list
        messageList = new MessageList();
        messageList.setWidthFull();
        messageList.setHeightFull();
        messageList.setMarkdown(true);

        // Reactively update message list using ComponentEffect
        com.vaadin.flow.component.ComponentEffect.bind(messageList, chatMessagesSignal,
                (msgList, msgSignals) -> {
                    if (msgSignals != null) {
                        CurrentUserSignal.UserInfo userInfo = currentUserSignal.getUserSignal().value();

                        var items = msgSignals.stream()
                                .map(msgSignal -> {
                                    ChatMessageData msg = msgSignal.value();
                                    MessageListItem item = new MessageListItem(
                                            msg.content().isBlank() ? "_typing..._" : msg.content(),
                                            msg.timestamp(),
                                            msg.role()
                                    );

                                    if (msg.role().equals("You") && userInfo != null && userInfo.isAuthenticated()) {
                                        item.setUserColorIndex(0);
                                        String username = userInfo.getUsername();
                                        if (username != null && !username.isBlank()) {
                                            item.setUserName(username);
                                            String userImage = MainLayout.getProfilePicturePath(username);
                                            if (userImage != null && !userImage.isBlank()) {
                                                item.setUserImage(userImage);
                                            }
                                        }
                                    } else {
                                        item.setUserColorIndex(1);
                                    }

                                    return item;
                                })
                                .toList();
                        msgList.setItems(items);
                    }
                });

        // Message input
        messageInput = new MessageInput();
        messageInput.bindEnabled(messageInputEnabledSignal);
        messageInput.addSubmitListener(this::onMessageSubmit);

        chatContainer.add(messageList, messageInput);
        chatContainer.setFlexGrow(1, messageList);

        return chatContainer;
    }

    private void onMessageSubmit(MessageInput.SubmitEvent event) {
        String userMessage = event.getValue();

        if (userMessage == null || userMessage.isBlank()) {
            return;
        }

        // Disable input while processing
        messageInputEnabledSignal.value(false);

        // Add user message
        chatMessagesSignal.insertLast(new ChatMessageData("You", userMessage, Instant.now()));

        // Create assistant message placeholder
        Instant assistantTimestamp = Instant.now();
        chatMessagesSignal.insertLast(new ChatMessageData("Assistant", "", assistantTimestamp));

        // Get reference to the last message signal (assistant message) for updates
        var assistantMessageSignal = chatMessagesSignal.value()
                .get(chatMessagesSignal.value().size() - 1);

        // Accumulate streaming content
        StringBuilder streamingContent = new StringBuilder();

        // Stream responses from LLM with consistent conversation ID for memory
        taskLLMService.streamMessage(userMessage, createTaskContext(), conversationId).subscribe(token -> {
            // Append token and update the message in the signal - must be done on UI thread
            getUI().ifPresent(ui -> ui.access(() -> {
                streamingContent.append(token);
                assistantMessageSignal.value(new ChatMessageData("Assistant", streamingContent.toString(), assistantTimestamp));
            }));
        }, error -> {
            // Update with error message - must be done on UI thread
            getUI().ifPresent(ui -> ui.access(() -> {
                streamingContent.append("\n\n❌ Error: ").append(error.getMessage());
                assistantMessageSignal.value(new ChatMessageData("Assistant", streamingContent.toString(), assistantTimestamp));
                messageInputEnabledSignal.value(true);
            }));
        }, () -> {
            // Streaming complete - re-enable input - must be done on UI thread
            getUI().ifPresent(ui -> ui.access(() -> {
                messageInputEnabledSignal.value(true);
            }));
        });
    }

    private void updateTaskField(String taskId, java.util.function.Function<Task, Task> updater) {
        tasksSignal.value().stream()
                .filter(sig -> sig.value().id().equals(taskId))
                .findFirst()
                .ifPresent(sig -> sig.value(updater.apply(sig.value())));
    }

    private TaskContext createTaskContext() {
        return new TaskContext() {
            @Override
            public java.util.List<Task> getAllTasks() {
                return tasksSignal.value().stream().map(ValueSignal::value).toList();
            }

            @Override
            public void addTask(Task task) {
                tasksSignal.insertLast(task);
            }

            @Override
            public void removeTask(String taskId) {
                tasksSignal.value().stream()
                        .filter(sig -> sig.value().id().equals(taskId))
                        .findFirst()
                        .ifPresent(tasksSignal::remove);
            }

            @Override
            public void updateTask(String taskId, String title, String description) {
                updateTaskField(taskId, task -> task.withTitle(title).withDescription(description));
            }

            @Override
            public void changeStatus(String taskId, Task.TaskStatus status) {
                updateTaskField(taskId, task -> task.withStatus(status));
            }
        };
    }
}
