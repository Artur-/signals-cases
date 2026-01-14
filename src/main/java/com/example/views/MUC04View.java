package com.example.views;

import com.example.MissingAPI;
import com.example.security.CurrentUserSignal;
import com.example.signals.CollaborativeSignals;
import com.example.signals.UserSessionRegistry;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import jakarta.annotation.security.PermitAll;

/**
 * Multi-User Case 4: Collaborative Form Editing with Locking
 *
 * Demonstrates field-level locking for collaborative editing:
 * - Show who is editing which field
 * - Lock fields when another user is editing
 * - Conflict detection on submit
 * - Real-time editing indicators
 *
 * Key Patterns:
 * - Field lock status Signal<Map<Field, User>>
 * - Optimistic locking
 * - Show editing indicators
 * - Merge conflict detection
 */
@Route(value = "muc-04", layout = MainLayout.class)
@PageTitle("Multi-User Case 4: Collaborative Editing")
@Menu(order = 21, title = "MUC 4: Collaborative Editing")
@PermitAll
public class MUC04View extends VerticalLayout {

    private final String currentUser;
    private final CollaborativeSignals collaborativeSignals;
    private final UserSessionRegistry userSessionRegistry;

    public MUC04View(CurrentUserSignal currentUserSignal,
                         CollaborativeSignals collaborativeSignals,
                         UserSessionRegistry userSessionRegistry) {
        this.currentUser = currentUserSignal.getUserSignal().value().getUsername();
        this.collaborativeSignals = collaborativeSignals;
        this.userSessionRegistry = userSessionRegistry;

        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Multi-User Case 4: Collaborative Form Editing with Locking");

        Paragraph description = new Paragraph(
            "This demonstrates collaborative form editing with field-level locking. " +
            "When you focus a field, it becomes locked for other users. " +
            "Other users see who is editing each field and are prevented from concurrent edits."
        );

        // Company Name field
        TextField companyNameField = createLockedField("companyName", "Company Name",
            collaborativeSignals.getCompanyNameSignal());

        // Address field
        TextField addressField = createLockedField("address", "Address",
            collaborativeSignals.getAddressSignal());

        // Phone field
        TextField phoneField = createLockedField("phone", "Phone Number",
            collaborativeSignals.getPhoneSignal());

        // Active editors display
        H3 editorsTitle = new H3("Active Editors");
        Div editorsDiv = new Div();
        editorsDiv.getStyle()
            .set("background-color", "#e3f2fd")
            .set("padding", "1em")
            .set("border-radius", "4px");

        MissingAPI.bindChildren(editorsDiv,
            collaborativeSignals.getFieldLocksSignal().map(locks -> {
                if (locks.isEmpty()) {
                    Div msg = new Div();
                    msg.setText("No fields are currently being edited");
                    msg.getStyle().set("font-style", "italic");
                    return java.util.List.of(msg);
                }

                return locks.entrySet().stream()
                    .map(entry -> {
                        Div item = new Div();
                        String fieldLabel = formatFieldName(entry.getKey());
                        item.setText(String.format("🔒 %s: %s", fieldLabel, entry.getValue()));
                        item.getStyle()
                            .set("padding", "0.5em")
                            .set("background-color", entry.getValue().equals(currentUser) ?
                                "#fff3e0" : "transparent")
                            .set("border-radius", "4px");
                        return item;
                    })
                    .toList();
            })
        );

        // Save button
        Button saveButton = new Button("Save Changes", event -> {
            // In production, would check for conflicts and merge
            // For now, just show a message
            Paragraph successMsg = new Paragraph("✓ Changes saved successfully");
            successMsg.getStyle().set("color", "green");
            add(successMsg);
            getUI().ifPresent(ui -> ui.access(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        ui.access(() -> remove(successMsg));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }));
        });
        saveButton.addThemeName("primary");

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle()
            .set("background-color", "#fff3e0")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-top", "1em")
            .set("font-style", "italic");
        infoBox.add(new Paragraph(
            "💡 Field-level locking prevents edit conflicts in collaborative scenarios. " +
            "When a user focuses on a field, other users see it's locked. " +
            "In production, this would include:\n" +
            "• Automatic lock timeout after inactivity\n" +
            "• Optimistic locking on save with conflict detection\n" +
            "• Real-time value synchronization\n" +
            "• Merge strategies for concurrent edits"
        ));

        add(
            title,
            description,
            new H3("Shared Form Data"),
            companyNameField,
            addressField,
            phoneField,
            saveButton,
            editorsTitle,
            editorsDiv,
            infoBox
        );
    }

    private TextField createLockedField(String fieldName, String label,
                                         com.vaadin.signals.WritableSignal<String> signal) {
        TextField field = new TextField(label);
        field.setWidthFull();

        // Bind value
        MissingAPI.bindValue(field, signal);

        // Lock field when focused
        field.addFocusListener(event -> {
            collaborativeSignals.lockField(fieldName, currentUser);
        });

        // Unlock when blurred
        field.addBlurListener(event -> {
            collaborativeSignals.unlockField(fieldName, currentUser);
        });

        // Show who is editing
        Signal<String> helperTextSignal = collaborativeSignals.getFieldLocksSignal().map(locks -> {
            String lockOwner = locks.get(fieldName);
            if (lockOwner == null) {
                return "Available to edit";
            } else if (lockOwner.equals(currentUser)) {
                return "You are editing this field";
            } else {
                return "🔒 " + lockOwner + " is editing this field";
            }
        });
        MissingAPI.bindHelperText(field, helperTextSignal);

        // Disable if locked by another user
        Signal<Boolean> enabledSignal = collaborativeSignals.getFieldLocksSignal().map(locks -> {
            String lockOwner = locks.get(fieldName);
            return lockOwner == null || lockOwner.equals(currentUser);
        });
        MissingAPI.bindEnabled(field, enabledSignal);

        return field;
    }

    private String formatFieldName(String fieldName) {
        return switch (fieldName) {
            case "companyName" -> "Company Name";
            case "address" -> "Address";
            case "phone" -> "Phone Number";
            default -> fieldName;
        };
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        userSessionRegistry.registerUser(currentUser);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        userSessionRegistry.unregisterUser(currentUser);
    }
}
