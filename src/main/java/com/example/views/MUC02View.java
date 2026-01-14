package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.util.Map;

import com.example.security.CurrentUserSignal;
import com.example.signals.CollaborativeSignals;
import com.example.signals.UserSessionRegistry;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.WritableSignal;

/**
 * Multi-User Case 2: Collaborative Cursor Positions
 *
 * Demonstrates showing all users' cursor positions in real-time: - Each user
 * updates their own cursor position signal - All users read all cursor signals
 * - Map<UserId, Signal<CursorPosition>> - Real-time updates
 *
 * Key Patterns: - Per-user writable signals shared via static Map - All users
 * observe all signals - Efficient multi-signal updates - Collaborative
 * awareness indicators
 */
@Route(value = "muc-02", layout = MainLayout.class)
@PageTitle("Multi-User Case 2: Collaborative Cursors")
@Menu(order = 51, title = "MUC 2: Collaborative Cursors")
@PermitAll
public class MUC02View extends VerticalLayout {

    private final String currentUser;
    private final WritableSignal<CollaborativeSignals.CursorPosition> myCursorSignal;
    private final CollaborativeSignals collaborativeSignals;
    private final UserSessionRegistry userSessionRegistry;

    public MUC02View(CurrentUserSignal currentUserSignal,
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

        // Register this user's cursor signal
        this.myCursorSignal = collaborativeSignals
                .getCursorSignalForUser(currentUser);

        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Multi-User Case 2: Collaborative Cursor Positions");

        Paragraph description = new Paragraph(
                "This use case demonstrates showing all users' cursor positions in a shared canvas area. "
                        + "Each user's cursor position is stored in their own signal, and all signals are read by all users. "
                        + "Try opening in multiple windows to see collaborative cursor awareness.");

        // Canvas area for cursor tracking
        Div canvas = new Div();
        canvas.setId("cursor-canvas");
        canvas.getStyle().set("position", "relative")
                .set("background-color", "#f5f5f5")
                .set("border", "2px solid #e0e0e0").set("border-radius", "4px")
                .set("height", "400px").set("margin", "1em 0")
                .set("cursor", "crosshair");

        // Add cursor indicators for all users
        Div cursorsContainer = new Div();
        cursorsContainer.getStyle().set("position", "relative");
        canvas.add(cursorsContainer);

        // Render cursor indicators for all users
        renderAllCursors(cursorsContainer);

        // Track mouse movement
        canvas.getElement().addEventListener("mousemove", event -> {
            // Get mouse position relative to canvas
            double clientX = event.getEventData().get("event.offsetX")
                    .asDouble();
            double clientY = event.getEventData().get("event.offsetY")
                    .asDouble();
            myCursorSignal.value(new CollaborativeSignals.CursorPosition(
                    (int) clientX, (int) clientY));
        }).addEventData("event.offsetX").addEventData("event.offsetY");

        // Current users list
        H3 usersTitle = new H3("Active Users");
        Div usersList = new Div();
        usersList.getStyle().set("background-color", "#e3f2fd")
                .set("padding", "1em").set("border-radius", "4px");

        // Display all active users
        for (Map.Entry<String, WritableSignal<CollaborativeSignals.CursorPosition>> entry : collaborativeSignals
                .getUserCursors().entrySet()) {
            String username = entry.getKey();
            WritableSignal<CollaborativeSignals.CursorPosition> signal = entry
                    .getValue();

            Div userItem = new Div();
            userItem.getStyle().set("display", "flex")
                    .set("justify-content", "space-between")
                    .set("margin-bottom", "0.5em");

            Div userLabel = new Div();
            userLabel.setText(
                    username + (username.equals(currentUser) ? " (you)" : ""));
            userLabel.getStyle().set("font-weight",
                    username.equals(currentUser) ? "bold" : "normal");

            Div positionLabel = new Div();
            positionLabel.bindText(
                    signal.map(CollaborativeSignals.CursorPosition::toString));
            positionLabel.getStyle().set("font-family", "monospace")
                    .set("color", "var(--lumo-secondary-text-color)");

            userItem.add(userLabel, positionLabel);
            usersList.add(userItem);
        }

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle().set("background-color", "#fff3e0")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "💡 Each user's cursor position is stored in a WritableSignal in a static shared Map. "
                        + "All users read all signals to display cursor indicators. This pattern enables "
                        + "real-time collaborative awareness without complex synchronization code. "
                        + "With Vaadin Push, updates propagate automatically to all connected clients."));

        add(title, description, canvas, usersTitle, usersList, infoBox);
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

    private void renderAllCursors(Div container) {
        for (Map.Entry<String, WritableSignal<CollaborativeSignals.CursorPosition>> entry : collaborativeSignals
                .getUserCursors().entrySet()) {
            String username = entry.getKey();
            if (username.equals(currentUser)) {
                continue; // Don't show own cursor
            }

            WritableSignal<CollaborativeSignals.CursorPosition> signal = entry
                    .getValue();

            Div cursorIndicator = new Div();
            cursorIndicator.getStyle().set("position", "absolute")
                    .set("width", "20px").set("height", "20px")
                    .set("background-color", "var(--lumo-primary-color)")
                    .set("border-radius", "50%")
                    .set("border", "2px solid white")
                    .set("pointer-events", "none")
                    .set("transform", "translate(-50%, -50%)")
                    .set("z-index", "1000");

            // Bind position
            cursorIndicator.getStyle().bind("left",
                    signal.map(pos -> pos.x() + "px"));
            cursorIndicator.getStyle().bind("top",
                    signal.map(pos -> pos.y() + "px"));

            // Label
            Div label = new Div();
            label.setText(username);
            label.getStyle().set("position", "absolute").set("top", "25px")
                    .set("left", "0").set("white-space", "nowrap")
                    .set("background-color", "rgba(0, 0, 0, 0.7)")
                    .set("color", "white").set("padding", "2px 6px")
                    .set("border-radius", "3px").set("font-size", "0.75em");

            cursorIndicator.add(label);
            container.add(cursorIndicator);
        }
    }
}
