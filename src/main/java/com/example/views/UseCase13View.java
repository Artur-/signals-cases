package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.MissingAPI;
import com.example.signals.SessionIdHelper;
import com.example.signals.UserInfo;
import com.example.signals.UserSessionRegistry;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.signals.Signal;

/**
 * Use Case 13: Real-Time Active Users Tracking
 *
 * Demonstrates application-scoped signals tracking all logged-in users with
 * automatic real-time updates when users join/leave or change views.
 *
 * Key Patterns:
 * - Application-scoped ListSignal tracking all active users
 * - Automatic reactivity without polling or manual refresh
 * - Multi-user real-time synchronization
 * - Computed signals for derived values
 * - Reactive list rendering with bindChildren
 */
@Route(value = "use-case-13", layout = MainLayout.class)
@PageTitle("Use Case 13: Real-Time Active Users")
@Menu(order = 13, title = "UC 13: Real-Time Active Users")
@PermitAll
public class UseCase13View extends VerticalLayout {

    private final UserSessionRegistry userSessionRegistry;
    private final Map<String, String> routeToTitleMap;

    public UseCase13View(UserSessionRegistry userSessionRegistry) {
        this.userSessionRegistry = userSessionRegistry;

        // Cache route-to-title mappings from MenuConfiguration
        this.routeToTitleMap = new HashMap<>();
        MenuConfiguration.getMenuEntries().forEach(entry -> {
            // Store both with and without leading slash for flexible lookup
            String path = entry.path();
            String title = entry.title();
            routeToTitleMap.put(path, title);
            if (path.startsWith("/")) {
                routeToTitleMap.put(path.substring(1), title);
            } else {
                routeToTitleMap.put("/" + path, title);
            }
        });

        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 13: Real-Time Active Users Tracking");

        Paragraph description = new Paragraph(
                "This use case demonstrates application-scoped signals tracking all logged-in users "
                        + "with automatic real-time updates when users join/leave or change views. "
                        + "Features include tab visibility tracking (Page Visibility API) and last interaction time. "
                        + "The ListSignal reactively propagates changes to all sessions without polling. "
                        + "Try opening multiple browser tabs with different users to see live synchronization!");

        // Active users counter
        Signal<Integer> userCountSignal = Signal.computed(() ->
                userSessionRegistry.getActiveUsersSignal().value().size()
        );

        Div counterBox = new Div();
        counterBox.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("padding", "1em")
                .set("border-radius", "8px")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("margin", "1em 0");

        H3 counterTitle = new H3();
        counterTitle.bindText(userCountSignal.map(count ->
                "👥 Currently Online: " + count
        ));
        counterTitle.getStyle().set("margin", "0");

        counterBox.add(counterTitle);

        // User list container
        H3 userListTitle = new H3("Active Users");
        userListTitle.getStyle().set("margin-top", "1.5em").set("margin-bottom", "0.5em");

        Div userListContainer = new Div();
        userListContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "1em");

        // Get current session ID for highlighting
        String currentSessionId = SessionIdHelper.getCurrentSessionId();

        // Reactively bind children to active users list
        MissingAPI.bindChildren(userListContainer, Signal.computed(() -> {
            List<com.vaadin.signals.ValueSignal<UserInfo>> userSignals =
                    userSessionRegistry.getActiveUsersSignal().value();
            List<Card> userCards = new ArrayList<>();

            for (var userSignal : userSignals) {
                UserInfo user = userSignal.value();
                Card userCard = createUserCard(user, currentSessionId);
                userCards.add(userCard);
            }

            return userCards;
        }));

        // Educational info box
        Div infoBox = new Div();
        infoBox.getStyle()
                .set("background", "#e0f7fa")
                .set("padding", "1em")
                .set("border-radius", "8px")
                .set("border-left", "4px solid #00BCD4")
                .set("margin-top", "2em");

        H3 infoTitle = new H3("💡 How This Works");
        infoTitle.getStyle().set("margin-top", "0");

        VerticalLayout infoContent = new VerticalLayout();
        infoContent.setSpacing(false);
        infoContent.setPadding(false);

        infoContent.add(
                new Paragraph("• Application-scoped ListSignal tracks all logged-in users globally"),
                new Paragraph("• Automatically updates when users join/leave without polling"),
                new Paragraph("• Tab visibility tracked using Page Visibility API (🟢 = active, ⚫ = inactive)"),
                new Paragraph("• Last interaction time shows when user last sent a server event"),
                new Paragraph("• Each user session is tracked with unique ID (vaadinSessionId:uiId)"),
                new Paragraph("• Try opening multiple browser tabs with different users and switching tabs!")
        );

        infoBox.add(infoTitle, infoContent);

        add(title, description, counterBox, userListTitle, userListContainer, infoBox);
    }

    private Card createUserCard(UserInfo userInfo, String currentSessionId) {
        Card card = new Card();

        boolean isCurrentSession = userInfo.sessionId().equals(currentSessionId);
        boolean isTabActive = userInfo.isTabActive();

        // Highlight current user's session
        if (isCurrentSession) {
            card.getStyle()
                    .set("border", "2px solid var(--lumo-primary-color)")
                    .set("background", "var(--lumo-primary-color-10pct)");
        }

        // Dim inactive tabs
        if (!isTabActive) {
            card.getStyle()
                    .set("opacity", "0.6")
                    .set("filter", "grayscale(30%)");
        }

        // Header: Tab Status + Avatar + Username/Nickname + Role Badge
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);

        // Tab activity indicator
        Span tabIndicator = new Span(isTabActive ? "🟢" : "⚫");
        tabIndicator.getElement().setAttribute("title",
                isTabActive ? "Tab is active" : "Tab is inactive");
        tabIndicator.getStyle().set("flex-shrink", "0");

        // Avatar (using first letter of username)
        Span avatar = new Span(userInfo.username().substring(0, 1).toUpperCase());
        avatar.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "40px")
                .set("height", "40px")
                .set("border-radius", "50%")
                .set("background", getColorForUser(userInfo.username()))
                .set("color", "white")
                .set("font-weight", "bold")
                .set("flex-shrink", "0");

        // Display name with nickname if available
        String displayName = userInfo.nickname() != null && !userInfo.nickname().isEmpty()
                ? userInfo.username() + " (" + userInfo.nickname() + ")"
                : userInfo.username();

        // Add session number if multiple sessions for same user
        long sessionCount = userSessionRegistry.getActiveUsersSignal().value().stream()
                .filter(us -> us.value().username().equals(userInfo.username()))
                .count();

        if (sessionCount > 1) {
            // Find this session's number
            long sessionNumber = 0;
            for (var us : userSessionRegistry.getActiveUsersSignal().value()) {
                UserInfo u = us.value();
                if (u.username().equals(userInfo.username())) {
                    sessionNumber++;
                    if (u.sessionId().equals(userInfo.sessionId())) {
                        break;
                    }
                }
            }
            displayName = userInfo.username() + " #" + sessionNumber;
            if (userInfo.nickname() != null && !userInfo.nickname().isEmpty()) {
                displayName += " (" + userInfo.nickname() + ")";
            }
        }

        if (isCurrentSession) {
            displayName += " (YOU)";
        }

        Span nameSpan = new Span(displayName);
        nameSpan.getStyle().set("font-weight", "bold").set("flex-grow", "1");

        // Role badge
        Span roleBadge = createRoleBadge(userInfo.username());

        header.add(tabIndicator, avatar, nameSpan, roleBadge);
        card.setHeader(header);

        // Content: Current view and session duration
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(false);

        // Current view
        HorizontalLayout viewRow = new HorizontalLayout();
        viewRow.setAlignItems(FlexComponent.Alignment.CENTER);
        viewRow.setSpacing(false);
        viewRow.getStyle().set("gap", "0.5em");

        Span viewIcon = new Span("📍");
        Span viewText = new Span("Viewing: " + formatViewName(userInfo.currentView()));
        viewText.getStyle().set("font-size", "var(--lumo-font-size-s)");

        viewRow.add(viewIcon, viewText);

        // Session duration
        HorizontalLayout durationRow = new HorizontalLayout();
        durationRow.setAlignItems(FlexComponent.Alignment.CENTER);
        durationRow.setSpacing(false);
        durationRow.getStyle().set("gap", "0.5em");

        Span durationIcon = new Span("🕐");
        long sessionDuration = System.currentTimeMillis() - userInfo.sessionStartTime();
        String durationText = formatDuration(sessionDuration);
        Span durationTextSpan = new Span("Online for " + durationText);
        durationTextSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        durationRow.add(durationIcon, durationTextSpan);

        // Last interaction time
        HorizontalLayout interactionRow = new HorizontalLayout();
        interactionRow.setAlignItems(FlexComponent.Alignment.CENTER);
        interactionRow.setSpacing(false);
        interactionRow.getStyle().set("gap", "0.5em");

        Span interactionIcon = new Span("⚡");
        long timeSinceInteraction = System.currentTimeMillis() - userInfo.lastInteractionTime();
        String interactionText = timeSinceInteraction < 5000
                ? "Just now"
                : "Last active " + formatDuration(timeSinceInteraction) + " ago";
        Span interactionTextSpan = new Span(interactionText);
        interactionTextSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        interactionRow.add(interactionIcon, interactionTextSpan);

        content.add(viewRow, durationRow, interactionRow);
        card.add(content);

        return card;
    }

    private String getColorForUser(String username) {
        // Deterministic color based on username
        int hash = username.hashCode();
        String[] colors = {"#4CAF50", "#2196F3", "#FF9800", "#9C27B0", "#F44336", "#00BCD4"};
        return colors[Math.abs(hash) % colors.length];
    }

    private Span createRoleBadge(String username) {
        // Get role based on username (matches SecurityConfiguration)
        String role = getRoleForUser(username);
        String badgeColor = switch (role) {
            case "SUPER_ADMIN" -> "#9C27B0";
            case "ADMIN" -> "#F44336";
            case "EDITOR" -> "#FF9800";
            case "VIEWER" -> "#2196F3";
            default -> "#757575";
        };

        String badgeLabel = switch (role) {
            case "SUPER_ADMIN" -> "S";
            case "ADMIN" -> "A";
            case "EDITOR" -> "E";
            case "VIEWER" -> "V";
            default -> "?";
        };

        Span badge = new Span("[" + badgeLabel + "]");
        badge.getStyle()
                .set("padding", "2px 6px")
                .set("border-radius", "4px")
                .set("background", badgeColor)
                .set("color", "white")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "bold")
                .set("flex-shrink", "0");

        return badge;
    }

    private String getRoleForUser(String username) {
        // Map username to role based on SecurityConfiguration
        return switch (username) {
            case "superadmin" -> "SUPER_ADMIN";
            case "admin" -> "ADMIN";
            case "editor" -> "EDITOR";
            case "viewer" -> "VIEWER";
            default -> "VIEWER"; // Default role
        };
    }

    private String formatViewName(String viewRoute) {
        if (viewRoute == null || viewRoute.isEmpty()) {
            return "Unknown";
        }

        // Use cached route-to-title map
        return routeToTitleMap.getOrDefault(viewRoute, viewRoute);
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        }
    }
}
