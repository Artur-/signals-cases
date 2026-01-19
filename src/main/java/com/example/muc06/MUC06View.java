package com.example.muc06;

import com.example.views.ActiveUsersDisplay;
import com.example.views.MainLayout;

import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.util.UUID;

import com.example.MissingAPI;
import com.example.security.CurrentUserSignal;
import com.example.signals.CollaborativeSignals;
import com.example.signals.SessionIdHelper;
import com.example.signals.UserSessionRegistry;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;

/**
 * Multi-User Case 6: Shared Task List with Inline Editing
 *
 * Demonstrates collaborative task list editing with: - Shared task list signal
 * across all users - Inline editing with immediate updates - Real-time
 * synchronization of changes - Multiple users editing different tasks
 * simultaneously
 *
 * Key Patterns: - Shared ListSignal<Task> across sessions - Inline field
 * editing without save buttons - Real-time reactive statistics - Server-side
 * signal coordination - Push-based real-time updates
 */
@Route(value = "muc-06", layout = MainLayout.class)
@PageTitle("MUC 6: Shared Task List")
@Menu(order = 55, title = "MUC 6: Shared Task List")
@PermitAll
public class MUC06View extends VerticalLayout {

    private final String currentUser;
    private final CollaborativeSignals collaborativeSignals;
    private final UserSessionRegistry userSessionRegistry;

    public MUC06View(CurrentUserSignal currentUserSignal,
            CollaborativeSignals collaborativeSignals,
            UserSessionRegistry userSessionRegistry) {
        CurrentUserSignal.UserInfo userInfo = currentUserSignal.getUserSignal()
                .value();
        if (userInfo == null || !userInfo.isAuthenticated()) {
            throw new IllegalStateException(
                    "User must be authenticated to access this view");
        }
        this.currentUser = userInfo.getUsername();
        this.collaborativeSignals = collaborativeSignals;
        this.userSessionRegistry = userSessionRegistry;

        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Multi-User Case 6: Shared Task List");

        Paragraph description = new Paragraph(
                "This use case demonstrates collaborative task list editing. "
                        + "The task list is shared across all users - any changes made by one user "
                        + "are immediately visible to all other users viewing this page. "
                        + "Try opening this page in multiple browser windows (different users) to see real-time collaboration. "
                        + "All edits are instant with no save button required.");

        // Initialize sample tasks if list is empty
        collaborativeSignals.initializeSampleTasks();

        ListSignal<CollaborativeSignals.Task> tasksSignal = collaborativeSignals
                .getTasksSignal();

        // Computed signals for statistics
        Signal<Integer> totalSignal = tasksSignal.map(list -> list.size());
        Signal<Integer> completedSignal = Signal.computed(() -> (int) tasksSignal
                .value().stream().filter(t -> t.value().completed()).count());
        Signal<Integer> pendingSignal = Signal
                .computed(() -> totalSignal.value() - completedSignal.value());

        // User count display
        ActiveUsersDisplay userCountBox = new ActiveUsersDisplay(
                userSessionRegistry, "Active on this view", "muc-06", true);

        // Statistics panel
        Div statsBox = new Div();
        statsBox.getStyle().set("background-color", "#f5f5f5")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-bottom", "1em").set("display", "flex")
                .set("gap", "2em");

        Span totalLabel = new Span();
        totalLabel.bindText(totalSignal.map(n -> "Total: " + n));
        totalLabel.getStyle().set("font-weight", "500");

        Span completedLabel = new Span();
        completedLabel.bindText(completedSignal.map(n -> "Completed: " + n));
        completedLabel.getStyle().set("color", "var(--lumo-success-color)")
                .set("font-weight", "500");

        Span pendingLabel = new Span();
        pendingLabel.bindText(pendingSignal.map(n -> "Pending: " + n));
        pendingLabel.getStyle().set("color", "var(--lumo-primary-color)")
                .set("font-weight", "500");

        statsBox.add(totalLabel, completedLabel, pendingLabel);

        // Tasks section
        H3 tasksTitle = new H3("Shared Tasks");

        Div tasksContainer = new Div();
        tasksContainer.getStyle().set("display", "flex")
                .set("flex-direction", "column").set("gap", "0.5em")
                .set("margin-bottom", "1em");

        MissingAPI.bindChildren(tasksContainer,
                tasksSignal
                        .map(taskSignals -> taskSignals.stream()
                                .map(taskSignal -> createTaskRow(taskSignal,
                                        tasksSignal))
                                .toList()));

        // Add task button
        Button addButton = new Button("Add Task", event -> {
            String id = "task-" + UUID.randomUUID().toString();
            CollaborativeSignals.Task newTask = new CollaborativeSignals.Task(
                    id, "", false, LocalDate.now());
            tasksSignal.insertLast(newTask);
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "💡 This demonstrates real-time collaborative editing with shared signals. "
                        + "In production:\n"
                        + "• Signal is application-scoped Spring component (injected)\n"
                        + "• Vaadin Push broadcasts all changes to connected clients\n"
                        + "• Tasks would be persisted to database\n"
                        + "• Conflict resolution could be added for simultaneous edits\n"
                        + "• Field locking could prevent editing conflicts (see MUC 4)\n"
                        + "• All synchronization is automatic via the Signal API"));

        add(title, description, userCountBox, statsBox, tasksTitle,
                tasksContainer, addButton, infoBox);
    }

    private HorizontalLayout createTaskRow(
            ValueSignal<CollaborativeSignals.Task> taskSignal,
            ListSignal<CollaborativeSignals.Task> tasksSignal) {
        CollaborativeSignals.Task task = taskSignal.value();

        // Checkbox for completed status
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(task.completed());
        checkbox.setAriaLabel("Task completed");
        checkbox.addValueChangeListener(e -> {
            CollaborativeSignals.Task current = taskSignal.value();
            taskSignal.value(new CollaborativeSignals.Task(current.id(),
                    current.title(), e.getValue(), current.dueDate()));
        });

        // TextField for title
        TextField titleField = new TextField();
        titleField.setValue(task.title() != null ? task.title() : "");
        titleField.setPlaceholder("Task title...");
        titleField.setWidth("400px");
        titleField.addValueChangeListener(e -> {
            CollaborativeSignals.Task current = taskSignal.value();
            taskSignal.value(new CollaborativeSignals.Task(current.id(),
                    e.getValue(), current.completed(), current.dueDate()));
        });

        // Add strikethrough styling for completed tasks
        if (task.completed()) {
            titleField.getStyle().set("text-decoration", "line-through");
        } else {
            titleField.getStyle().remove("text-decoration");
        }

        // DatePicker for due date
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(task.dueDate());
        datePicker.setPlaceholder("Due date");
        datePicker.setWidth("180px");
        datePicker.addValueChangeListener(e -> {
            CollaborativeSignals.Task current = taskSignal.value();
            taskSignal.value(new CollaborativeSignals.Task(current.id(),
                    current.title(), current.completed(), e.getValue()));
        });

        // Delete button
        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        deleteButton.setAriaLabel("Delete task");
        deleteButton.addClickListener(e -> tasksSignal.remove(taskSignal));

        // Layout with styling
        HorizontalLayout row = new HorizontalLayout(checkbox, titleField,
                datePicker, deleteButton);
        row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        row.setWidthFull();
        row.setPadding(true);
        row.getStyle().set("background-color", "#ffffff")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "4px");

        return row;
    }
}
