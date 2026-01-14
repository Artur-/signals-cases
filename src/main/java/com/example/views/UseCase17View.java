package com.example.views;

import com.example.MissingAPI;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Use Case 17: Form Auto-Save with Persistence
 *
 * Demonstrates form auto-save with localStorage persistence:
 * - Save draft every 30 seconds if form is dirty
 * - Signal showing last save time and status
 * - Load draft from localStorage on view initialization
 * - Clear draft after successful submit
 * - Visual indication of dirty/saved state
 *
 * Key Patterns:
 * - Form dirty state tracking
 * - Periodic auto-save with signals
 * - localStorage integration via JavaScript
 * - Draft restoration on page load
 */
@Route(value = "use-case-17", layout = MainLayout.class)
@PageTitle("Use Case 17: Form Auto-Save")
@Menu(order = 55, title = "UC 17: Form Auto-Save")
@PermitAll
public class UseCase17View extends VerticalLayout {

    public static class FormData {
        public String name = "";
        public String email = "";
        public String subject = "";
        public String message = "";

        public FormData() {}

        public FormData(String name, String email, String subject, String message) {
            this.name = name;
            this.email = email;
            this.subject = subject;
            this.message = message;
        }

        public boolean isEmpty() {
            return name.isEmpty() && email.isEmpty() && subject.isEmpty() && message.isEmpty();
        }
    }

    public enum SaveStatus {
        NO_DRAFT,
        DIRTY,
        SAVING,
        SAVED,
        LOADED
    }

    public static class DraftStatus {
        private final SaveStatus status;
        private final LocalDateTime timestamp;

        public DraftStatus(SaveStatus status, LocalDateTime timestamp) {
            this.status = status;
            this.timestamp = timestamp;
        }

        public SaveStatus getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }

        public String getDisplayText() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            switch (status) {
                case NO_DRAFT: return "No draft saved";
                case DIRTY: return "Unsaved changes";
                case SAVING: return "Saving draft...";
                case SAVED: return "Draft saved at " + timestamp.format(formatter);
                case LOADED: return "Draft loaded from " + timestamp.format(formatter);
                default: return "";
            }
        }
    }

    private static final String DRAFT_KEY = "use-case-17-form-draft";
    private static final int AUTO_SAVE_INTERVAL_MS = 30000; // 30 seconds

    private final WritableSignal<String> nameSignal = new ValueSignal<>("");
    private final WritableSignal<String> emailSignal = new ValueSignal<>("");
    private final WritableSignal<String> subjectSignal = new ValueSignal<>("");
    private final WritableSignal<String> messageSignal = new ValueSignal<>("");

    private final WritableSignal<DraftStatus> draftStatusSignal =
        new ValueSignal<>(new DraftStatus(SaveStatus.NO_DRAFT, LocalDateTime.now()));

    private final WritableSignal<FormData> originalDataSignal = new ValueSignal<>(new FormData());

    private Timer autoSaveTimer;

    public UseCase17View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 17: Form Auto-Save with Persistence");

        Paragraph description = new Paragraph(
            "This use case demonstrates auto-save functionality for long forms. " +
            "As you type, the form detects changes and automatically saves a draft to browser localStorage every 30 seconds. " +
            "If you close the tab and come back, your draft will be restored. " +
            "The status indicator shows save state in real-time."
        );

        // Form
        H3 formTitle = new H3("Support Request Form");

        TextField nameField = new TextField("Name");
        nameField.setWidthFull();
        MissingAPI.bindValue(nameField, nameSignal);

        EmailField emailField = new EmailField("Email");
        emailField.setWidthFull();
        MissingAPI.bindValue(emailField, emailSignal);

        TextField subjectField = new TextField("Subject");
        subjectField.setWidthFull();
        MissingAPI.bindValue(subjectField, subjectSignal);

        TextArea messageArea = new TextArea("Message");
        messageArea.setWidthFull();
        messageArea.setHeight("200px");
        MissingAPI.bindValue(messageArea, messageSignal);

        // Status indicator
        Div statusBox = new Div();
        statusBox.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5em")
            .set("padding", "0.75em")
            .set("border-radius", "4px")
            .set("margin", "1em 0");

        Icon statusIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
        Paragraph statusText = new Paragraph();
        statusText.getStyle().set("margin", "0");

        MissingAPI.bindText(statusText, draftStatusSignal.map(DraftStatus::getDisplayText));

        // Bind status box style based on status
        Signal<String> statusBgColorSignal = draftStatusSignal.map(status -> {
            switch (status.getStatus()) {
                case NO_DRAFT: return "#f5f5f5";
                case DIRTY: return "#fff3e0";
                case SAVING: return "#e3f2fd";
                case SAVED: return "#e8f5e9";
                case LOADED: return "#e1f5fe";
                default: return "#f5f5f5";
            }
        });
        MissingAPI.bindStyle(statusBox, "background-color", statusBgColorSignal);

        Signal<String> statusIconSignal = draftStatusSignal.map(status -> {
            switch (status.getStatus()) {
                case NO_DRAFT: return "circle-thin";
                case DIRTY: return "exclamation-circle";
                case SAVING: return "spinner";
                case SAVED: return "check-circle";
                case LOADED: return "download";
                default: return "circle-thin";
            }
        });

        // Update icon dynamically
        com.vaadin.flow.component.ComponentEffect.bind(this, draftStatusSignal, (comp, status) -> {
            VaadinIcon icon;
            switch (status.getStatus()) {
                case NO_DRAFT: icon = VaadinIcon.CIRCLE_THIN; break;
                case DIRTY: icon = VaadinIcon.EXCLAMATION_CIRCLE; break;
                case SAVING: icon = VaadinIcon.SPINNER; break;
                case SAVED: icon = VaadinIcon.CHECK_CIRCLE; break;
                case LOADED: icon = VaadinIcon.DOWNLOAD; break;
                default: icon = VaadinIcon.CIRCLE_THIN;
            }
            statusIcon.getElement().setAttribute("icon", "vaadin:" + icon.name().toLowerCase().replace('_', '-'));
        });

        statusBox.add(statusIcon, statusText);

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button submitButton = new Button("Submit", event -> {
            // Simulate form submission
            Notification notification = Notification.show(
                "Form submitted successfully!",
                3000,
                Notification.Position.BOTTOM_END
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Clear draft and reset form
            clearDraft();
            resetForm();
        });
        submitButton.addThemeVariants();

        // Enable submit only if form is not empty
        Signal<Boolean> isFormEmptySignal = Signal.computed(() ->
            nameSignal.value().isEmpty() &&
            emailSignal.value().isEmpty() &&
            subjectSignal.value().isEmpty() &&
            messageSignal.value().isEmpty()
        );
        MissingAPI.bindEnabled(submitButton, isFormEmptySignal.map(empty -> !empty));

        Button saveNowButton = new Button("Save Draft Now", event -> {
            saveDraft();
            Notification.show("Draft saved!", 2000, Notification.Position.BOTTOM_END);
        });
        saveNowButton.addThemeName("tertiary");

        Button clearButton = new Button("Clear Draft", event -> {
            clearDraft();
            resetForm();
            Notification.show("Draft cleared", 2000, Notification.Position.BOTTOM_END);
        });
        clearButton.addThemeName("tertiary");
        clearButton.addThemeName("error");

        buttons.add(submitButton, saveNowButton, clearButton);

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle()
            .set("background-color", "#e0f7fa")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-top", "1em")
            .set("font-style", "italic");
        infoBox.add(new Paragraph(
            "💡 Auto-save is crucial for long forms where users might lose work due to browser crashes or accidental navigation. " +
            "This pattern uses localStorage for client-side persistence - in production, you'd typically save to a server endpoint. " +
            "The form tracks 'dirty' state by comparing current values with the original/last-saved values. " +
            "Try typing something, wait 30 seconds for auto-save, then refresh the page to see your draft restored. " +
            "Or click 'Save Draft Now' to save immediately."
        ));

        add(title, description, formTitle, nameField, emailField, subjectField, messageArea, statusBox, buttons, infoBox);

        // Subscribe to form fields to mark as dirty
        setupDirtyTracking();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Load draft from localStorage
        loadDraft();

        // Set up auto-save timer
        autoSaveTimer = new Timer();
        autoSaveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getUI().ifPresent(ui -> ui.access(() -> {
                    if (isFormDirty()) {
                        saveDraft();
                    }
                }));
            }
        }, AUTO_SAVE_INTERVAL_MS, AUTO_SAVE_INTERVAL_MS);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (autoSaveTimer != null) {
            autoSaveTimer.cancel();
        }
    }

    private void setupDirtyTracking() {
        // Mark form as dirty when any field changes
        com.vaadin.flow.component.ComponentEffect.effect(this, () -> {
            // Access all signals to track them
            nameSignal.value();
            emailSignal.value();
            subjectSignal.value();
            messageSignal.value();

            if (isFormDirty() && draftStatusSignal.value().getStatus() != SaveStatus.DIRTY) {
                draftStatusSignal.value(new DraftStatus(SaveStatus.DIRTY, LocalDateTime.now()));
            }
        });
    }

    private boolean isFormDirty() {
        FormData current = getCurrentFormData();
        FormData original = originalDataSignal.value();

        return !current.name.equals(original.name) ||
               !current.email.equals(original.email) ||
               !current.subject.equals(original.subject) ||
               !current.message.equals(original.message);
    }

    private FormData getCurrentFormData() {
        return new FormData(
            nameSignal.value(),
            emailSignal.value(),
            subjectSignal.value(),
            messageSignal.value()
        );
    }

    private void saveDraft() {
        draftStatusSignal.value(new DraftStatus(SaveStatus.SAVING, LocalDateTime.now()));

        FormData data = getCurrentFormData();
        if (data.isEmpty()) {
            clearDraft();
            return;
        }

        // Manually construct JSON (simple format)
        String json = String.format(
            "{\"name\":\"%s\",\"email\":\"%s\",\"subject\":\"%s\",\"message\":\"%s\"}",
            escapeJson(data.name),
            escapeJson(data.email),
            escapeJson(data.subject),
            escapeJson(data.message)
        );

        // Save to localStorage via JavaScript
        UI.getCurrent().getPage().executeJs(
            "localStorage.setItem($0, $1);",
            DRAFT_KEY,
            json
        ).then(result -> {
            LocalDateTime now = LocalDateTime.now();
            draftStatusSignal.value(new DraftStatus(SaveStatus.SAVED, now));
            originalDataSignal.value(data);
        });
    }

    private void loadDraft() {
        UI.getCurrent().getPage().executeJs(
            "return localStorage.getItem($0);",
            DRAFT_KEY
        ).then(String.class, json -> {
            if (json != null && !json.isEmpty() && !json.equals("null")) {
                try {
                    // Simple JSON parsing for our format
                    FormData data = parseFormDataJson(json);

                    // Restore form fields
                    nameSignal.value(data.name);
                    emailSignal.value(data.email);
                    subjectSignal.value(data.subject);
                    messageSignal.value(data.message);

                    originalDataSignal.value(data);
                    draftStatusSignal.value(new DraftStatus(SaveStatus.LOADED, LocalDateTime.now()));

                } catch (Exception e) {
                    // Invalid JSON, clear it
                    clearDraft();
                }
            }
        });
    }

    private void clearDraft() {
        UI.getCurrent().getPage().executeJs(
            "localStorage.removeItem($0);",
            DRAFT_KEY
        );
        draftStatusSignal.value(new DraftStatus(SaveStatus.NO_DRAFT, LocalDateTime.now()));
    }

    private void resetForm() {
        nameSignal.value("");
        emailSignal.value("");
        subjectSignal.value("");
        messageSignal.value("");
        originalDataSignal.value(new FormData());
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private FormData parseFormDataJson(String json) {
        // Simple JSON parser for our specific format
        // Format: {"name":"...","email":"...","subject":"...","message":"..."}
        FormData data = new FormData();

        // Extract name
        data.name = extractJsonValue(json, "name");
        data.email = extractJsonValue(json, "email");
        data.subject = extractJsonValue(json, "subject");
        data.message = extractJsonValue(json, "message");

        return data;
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int startIdx = json.indexOf(pattern);
        if (startIdx == -1) return "";

        startIdx += pattern.length();
        int endIdx = startIdx;

        // Find the closing quote, handling escaped quotes
        while (endIdx < json.length()) {
            if (json.charAt(endIdx) == '"' && (endIdx == 0 || json.charAt(endIdx - 1) != '\\')) {
                break;
            }
            endIdx++;
        }

        String value = json.substring(startIdx, endIdx);

        // Unescape
        return value.replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
    }
}
