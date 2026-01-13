package com.example.views;

import com.example.MissingAPI;
import com.example.security.CurrentUserSignal;
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
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;
import jakarta.annotation.security.PermitAll;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Use Case 20: Competitive Button Click Game
 *
 * Demonstrates race condition handling and conflict resolution:
 * - Button appears at random position
 * - Fastest clicker gets the point
 * - All users see shared leaderboard
 * - Server-authoritative scoring (only one can win)
 *
 * Key Patterns:
 * - Optimistic UI updates
 * - Server-side signal coordination
 * - Atomic operations on shared signals
 * - Conflict resolution strategy
 */
@Route(value = "use-case-20", layout = MainLayout.class)
@PageTitle("Use Case 20: Click Game")
@Menu(order = 52, title = "UC 20: Click Race Game")
@PermitAll
public class UseCase20View extends VerticalLayout {

    // Shared game state (static = shared across all users)
    private static final WritableSignal<Map<String, Integer>> leaderboardSignal =
        new ValueSignal<>(new ConcurrentHashMap<>());

    private static final WritableSignal<Boolean> buttonVisibleSignal = new ValueSignal<>(false);
    private static final WritableSignal<Integer> buttonLeftSignal = new ValueSignal<>(0);
    private static final WritableSignal<Integer> buttonTopSignal = new ValueSignal<>(0);
    private static final AtomicInteger roundNumber = new AtomicInteger(0);

    private final String currentUser;
    private final Random random = new Random();

    public UseCase20View(CurrentUserSignal currentUserSignal) {
        this.currentUser = currentUserSignal.getUserSignal().value().getUsername();

        // Initialize user score if not present
        Map<String, Integer> leaderboard = leaderboardSignal.value();
        leaderboard.putIfAbsent(currentUser, 0);
        leaderboardSignal.value(leaderboard);

        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 20: Competitive Button Click Game");

        Paragraph description = new Paragraph(
            "This demonstrates race condition handling in a multi-user game. " +
            "Click the START button to spawn a target button at a random location. " +
            "The fastest user to click it gets a point. Only one user can score per round."
        );

        // Game area
        Div gameArea = new Div();
        gameArea.getStyle()
            .set("position", "relative")
            .set("background-color", "#f5f5f5")
            .set("border", "2px solid #e0e0e0")
            .set("border-radius", "4px")
            .set("height", "300px")
            .set("margin", "1em 0");

        // Target button
        Button targetButton = new Button("CLICK ME!", event -> {
            handleButtonClick();
        });
        targetButton.addThemeName("primary");
        targetButton.addThemeName("large");
        targetButton.getStyle()
            .set("position", "absolute")
            .set("z-index", "10");

        MissingAPI.bindVisible(targetButton, buttonVisibleSignal);
        MissingAPI.bindStyle(targetButton, "left", buttonLeftSignal.map(left -> left + "px"));
        MissingAPI.bindStyle(targetButton, "top", buttonTopSignal.map(top -> top + "px"));

        gameArea.add(targetButton);

        // Controls
        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);

        Button startButton = new Button("START ROUND", event -> {
            startNewRound();
        });
        startButton.addThemeName("success");

        Button resetButton = new Button("Reset Scores", event -> {
            leaderboardSignal.value(new ConcurrentHashMap<>());
        });
        resetButton.addThemeName("error");
        resetButton.addThemeName("small");

        controls.add(startButton, resetButton);

        // Leaderboard
        H3 leaderboardTitle = new H3("Leaderboard");
        Div leaderboardDiv = new Div();
        leaderboardDiv.getStyle()
            .set("background-color", "#e3f2fd")
            .set("padding", "1em")
            .set("border-radius", "4px");

        // Bind leaderboard display
        MissingAPI.bindChildren(leaderboardDiv,
            leaderboardSignal.map(scores -> {
                return scores.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .map(entry -> {
                        Div item = new Div();
                        item.setText(String.format("%s: %d points", entry.getKey(), entry.getValue()));
                        item.getStyle()
                            .set("padding", "0.5em")
                            .set("background-color", entry.getKey().equals(currentUser) ?
                                "#fff3e0" : "transparent")
                            .set("border-radius", "4px")
                            .set("font-weight", entry.getKey().equals(currentUser) ?
                                "bold" : "normal");
                        return item;
                    })
                    .toList();
            })
        );

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle()
            .set("background-color", "#fff3e0")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-top", "1em")
            .set("font-style", "italic");
        infoBox.add(new Paragraph(
            "💡 This demonstrates atomic operations and conflict resolution in multi-user scenarios. " +
            "When multiple users try to click simultaneously, only the first click is counted (atomic operation). " +
            "The leaderboard is a shared signal that updates for all users in real-time."
        ));

        add(title, description, gameArea, controls, leaderboardTitle, leaderboardDiv, infoBox);
    }

    private void startNewRound() {
        // Position button randomly
        int left = random.nextInt(400);
        int top = random.nextInt(200);

        buttonLeftSignal.value(left);
        buttonTopSignal.value(top);
        buttonVisibleSignal.value(true);

        roundNumber.incrementAndGet();
    }

    private synchronized void handleButtonClick() {
        // Atomic operation: Only first click counts
        if (!buttonVisibleSignal.value()) {
            return; // Already clicked by someone else
        }

        buttonVisibleSignal.value(false);

        // Award point
        Map<String, Integer> scores = new ConcurrentHashMap<>(leaderboardSignal.value());
        scores.merge(currentUser, 1, Integer::sum);
        leaderboardSignal.value(scores);
    }
}
