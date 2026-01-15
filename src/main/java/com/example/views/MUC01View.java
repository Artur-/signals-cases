package com.example.views;

import jakarta.annotation.security.PermitAll;

import com.example.MissingAPI;
import com.example.security.CurrentUserSignal;
import com.example.signals.CollaborativeSignals;
import com.example.signals.SessionIdHelper;
import com.example.signals.UserSessionRegistry;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Multi-User Case 1: Shared Chat/Message List
 *
 * Demonstrates a collaborative chat-like interface with: - Append-only shared
 * message list signal - Multiple users adding messages - Server-side signal
 * source with Push updates - Each user can only append, not modify history
 *
 * Key Patterns: - Shared Signal<List<Message>> across sessions - Append-only
 * operations - Server-side signal coordination - Push-based real-time updates
 *
 * Note: This is a simulation using local state. In production: - Signal would
 * be server-side (e.g., Spring @Component with @VaadinSessionScope) - Updates
 * would use Vaadin Push to broadcast to all clients - Message persistence would
 * be in database
 */
@Route(value = "muc-01", layout = MainLayout.class)
@PageTitle("MUC 1: Shared Chat")
@Menu(order = 50, title = "MUC 1: Shared Chat")
@PermitAll
public class MUC01View extends VerticalLayout {

    private final String currentUser;
    private final CollaborativeSignals collaborativeSignals;
    private final UserSessionRegistry userSessionRegistry;
    private String sessionId;

    public MUC01View(CurrentUserSignal currentUserSignal,
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
        setHeight("100%");

        H2 title = new H2("Multi-User Case 1: Shared Chat/Message List");

        Paragraph description = new Paragraph(
                "This use case demonstrates a collaborative chat with an append-only shared message list. "
                        + "In production, this would use server-side signals with Push to broadcast messages to all users. "
                        + "Try opening this page in multiple browser windows (different users) to see simulated collaboration.");

        // Active users display
        ActiveUsersDisplay activeUsersDisplay = new ActiveUsersDisplay(
                userSessionRegistry, "muc-01");

        // Message display area
        Div messagesContainer = new Div();
        messagesContainer.setWidthFull();
        messagesContainer.getStyle().set("background-color", "#f5f5f5")
                .set("border", "1px solid #e0e0e0").set("border-radius", "4px")
                .set("padding", "1em").set("min-height", "200px")
                .set("overflow-y", "auto").set("max-height", "400px");

        // Bind message list to UI
        MissingAPI.bindChildren(messagesContainer, collaborativeSignals
                .getMessagesSignal()
                .map(messageSignals -> messageSignals.stream().map(
                        msgSignal -> createMessageComponent(msgSignal.value()))
                        .toList()));

        // Message input
        H3 inputTitle = new H3("Send Message");
        TextArea messageInput = new TextArea();
        messageInput.setPlaceholder("Type your message...");
        messageInput.setWidthFull();
        messageInput.setMaxLength(500);

        HorizontalLayout sendLayout = new HorizontalLayout();
        sendLayout.setWidthFull();

        Button sendButton = new Button("Send Message", event -> {
            String text = messageInput.getValue();
            if (text != null && !text.trim().isEmpty() && sessionId != null) {
                // Look up current display name
                String displayName = getCurrentDisplayName();
                collaborativeSignals.appendMessage(
                        new CollaborativeSignals.Message(displayName,
                                text.trim()));
                messageInput.clear();
            }
        });
        sendButton.addThemeName("primary");

        Button clearButton = new Button("Clear All Messages", event -> {
            collaborativeSignals.clearMessages();
        });
        clearButton.addThemeName("error");
        clearButton.addThemeName("small");

        sendLayout.add(sendButton, clearButton);

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");

        Div messageCount = new Div();
        messageCount.bindText(collaborativeSignals.getMessagesSignal()
                .map(messages -> "💬 Total messages: " + messages.size()));

        infoBox.add(new Paragraph("💡 In production implementation:\n"
                + "• Signal is application-scoped Spring component (injected)\n"
                + "• Vaadin Push would broadcast updates to all connected clients\n"
                + "• Messages would be persisted to database\n"
                + "• Authorization would prevent editing others' messages\n"
                + "• Signal API handles all synchronization automatically"),
                messageCount);

        add(title, description, activeUsersDisplay, new H3("Messages"),
                messagesContainer, inputTitle, messageInput, sendLayout, infoBox);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.sessionId = SessionIdHelper.getCurrentSessionId();
    }

    private Div createMessageComponent(CollaborativeSignals.Message message) {
        Div messageDiv = new Div();
        messageDiv.getStyle().set("background-color", "#ffffff")
                .set("border-left", "3px solid var(--lumo-primary-color)")
                .set("padding", "0.75em").set("margin-bottom", "0.5em")
                .set("border-radius", "4px");

        Div header = new Div();
        header.getStyle().set("display", "flex")
                .set("justify-content", "space-between")
                .set("margin-bottom", "0.5em");

        Div author = new Div();
        author.setText("👤 " + message.author());
        author.getStyle().set("font-weight", "bold").set("color",
                "var(--lumo-primary-color)");

        Div timestamp = new Div();
        timestamp.setText(message.getFormattedTimestamp());
        timestamp.getStyle().set("font-size", "0.85em").set("color",
                "var(--lumo-secondary-text-color)");

        header.add(author, timestamp);

        Div text = new Div();
        text.setText(message.text());
        text.getStyle().set("color", "var(--lumo-body-text-color)");

        messageDiv.add(header, text);
        return messageDiv;
    }

    private String getCurrentDisplayName() {
        if (sessionId == null) {
            return currentUser;
        }

        var users = userSessionRegistry.getActiveUsersSignal().value();
        var displayNames = userSessionRegistry.getDisplayNamesSignal().value();

        // Find matching session key for current user
        String sessionKey = currentUser + ":" + sessionId;
        for (int i = 0; i < users.size() && i < displayNames.size(); i++) {
            if (sessionKey.equals(users.get(i).value().getCompositeKey())) {
                return displayNames.get(i);
            }
        }

        // Fallback to username
        return currentUser;
    }
}
