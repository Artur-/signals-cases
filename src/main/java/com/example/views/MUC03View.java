package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.util.Random;

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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Multi-User Case 3: Competitive Button Click Game
 *
 * Demonstrates race condition handling and conflict resolution: - Button
 * appears at random position - Fastest clicker gets the point - All users see
 * shared leaderboard - Server-authoritative scoring (only one can win)
 *
 * Key Patterns: - Optimistic UI updates - Server-side signal coordination -
 * Atomic operations on shared signals - Conflict resolution strategy
 */
@Route(value = "muc-03", layout = MainLayout.class)
@PageTitle("Multi-User Case 3: Click Game")
@Menu(order = 52, title = "MUC 3: Click Race Game")
@PermitAll
public class MUC03View extends VerticalLayout {

    private final String currentUser;
    private final CollaborativeSignals collaborativeSignals;
    private final UserSessionRegistry userSessionRegistry;
    private final Random random = new Random();

    public MUC03View(CurrentUserSignal currentUserSignal,
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

        // Initialize user score if not present
        collaborativeSignals.initializePlayerScore(currentUser);

        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Multi-User Case 3: Competitive Button Click Game");

        Paragraph description = new Paragraph(
                "This demonstrates race condition handling in a multi-user game. "
                        + "Click START to begin a round. Each round has 5 targets that appear one after another at random locations with random delays. "
                        + "The fastest user to click each target gets a point. Race to get the most points!");

        // Round status
        Div roundStatus = new Div();
        roundStatus.getStyle().set("font-size", "1.2em")
                .set("font-weight", "bold").set("margin-bottom", "0.5em");
        roundStatus.bindText(collaborativeSignals.getRoundNumberSignal()
                .map(round -> round == 0 ? "Click START to begin"
                        : "Round " + round));

        Div clicksStatus = new Div();
        clicksStatus.getStyle().set("font-size", "1em").set("color",
                "var(--lumo-secondary-text-color)");
        clicksStatus.bindText(collaborativeSignals.getClicksRemainingSignal()
                .map(clicks -> clicks > 0 ? "Targets remaining: " + clicks
                        : ""));

        // Game area
        Div gameArea = new Div();
        gameArea.setWidthFull();
        gameArea.getStyle().set("position", "relative")
                .set("background-color", "#f5f5f5")
                .set("border", "2px solid #e0e0e0").set("border-radius", "4px")
                .set("height", "300px").set("margin", "1em 0");

        // Target button
        Button targetButton = new Button("CLICK ME!", event -> {
            handleButtonClick();
        });
        targetButton.addThemeName("primary");
        targetButton.addThemeName("large");
        targetButton.getStyle().set("position", "absolute").set("z-index",
                "10");

        // Bind button text to show clicks remaining
        targetButton.bindText(collaborativeSignals.getClicksRemainingSignal()
                .map(clicks -> "CLICK ME! (" + clicks + ")"));

        targetButton.bindVisible(collaborativeSignals.getButtonVisibleSignal());
        targetButton.getStyle().bind("left", collaborativeSignals
                .getButtonLeftSignal().map(left -> left + "px"));
        targetButton.getStyle().bind("top", collaborativeSignals
                .getButtonTopSignal().map(top -> top + "px"));

        gameArea.add(targetButton);

        // Controls
        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);

        Button startButton = new Button("START ROUND", event -> {
            startNewRound();
        });
        startButton.addThemeName("success");

        Button resetButton = new Button("Reset Scores", event -> {
            collaborativeSignals.resetLeaderboard();
        });
        resetButton.addThemeName("error");
        resetButton.addThemeName("small");

        controls.add(startButton, resetButton);

        // Leaderboard
        H3 leaderboardTitle = new H3("Leaderboard");
        Div leaderboardDiv = new Div();
        leaderboardDiv.getStyle().set("background-color", "#e3f2fd")
                .set("padding", "1em").set("border-radius", "4px");

        // Bind leaderboard display
        MissingAPI.bindChildren(leaderboardDiv,
                collaborativeSignals.getLeaderboardSignal().map(scores -> {
                    return scores.entrySet().stream().sorted(
                            (e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                            .map(entry -> {
                                Div item = new Div();
                                item.setText(String.format("%s: %d points",
                                        entry.getKey(), entry.getValue()));
                                item.getStyle().set("padding", "0.5em")
                                        .set("background-color",
                                                entry.getKey().equals(
                                                        currentUser) ? "#fff3e0"
                                                                : "transparent")
                                        .set("border-radius", "4px")
                                        .set("font-weight",
                                                entry.getKey().equals(
                                                        currentUser) ? "bold"
                                                                : "normal");
                                return item;
                            }).toList();
                }));

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle().set("background-color", "#fff3e0")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "💡 This demonstrates atomic operations and conflict resolution in multi-user scenarios. "
                        + "Each round has 5 targets that appear with random delays (500-2000ms) at random positions. "
                        + "When multiple users try to click simultaneously, only the first click is counted (atomic operation). "
                        + "The leaderboard is a shared signal that updates for all users in real-time. "
                        + "Race against other players to get the most points!"));

        add(title, description, roundStatus, clicksStatus, gameArea, controls,
                leaderboardTitle, leaderboardDiv, infoBox);
    }

    private void startNewRound() {
        // Position button randomly and start a new round
        int left = random.nextInt(400);
        int top = random.nextInt(200);

        collaborativeSignals.startNewRound(left, top);
    }

    private void handleButtonClick() {
        // Atomic operation: Only first click counts (handled by
        // CollaborativeSignals)
        boolean moreClicksRemain = collaborativeSignals.awardPoint(currentUser);

        // If there are more clicks remaining, reposition button after random
        // delay
        if (moreClicksRemain) {
            new Thread(() -> {
                try {
                    // Random delay between 500ms and 2000ms
                    int delay = 500 + random.nextInt(1500);
                    Thread.sleep(delay);

                    // Reposition button at random location
                    int left = random.nextInt(400);
                    int top = random.nextInt(200);

                    getUI().ifPresent(ui -> ui.access(() -> collaborativeSignals
                            .repositionButton(left, top)));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
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
