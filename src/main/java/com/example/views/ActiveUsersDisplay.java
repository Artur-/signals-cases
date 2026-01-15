package com.example.views;

import com.example.MissingAPI;
import com.example.signals.UserSessionRegistry;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.signals.Signal;

/**
 * Reusable component for displaying active users/sessions.
 * Shows a count and comma-separated list of display names.
 */
public class ActiveUsersDisplay extends Div {

    /**
     * Creates an active users display showing all users with default styling and
     * "Active sessions" label.
     *
     * @param userSessionRegistry the registry to get user data from
     */
    public ActiveUsersDisplay(UserSessionRegistry userSessionRegistry) {
        this(userSessionRegistry, "Active sessions", null, false);
    }

    /**
     * Creates an active users display filtered to a specific view route.
     *
     * @param userSessionRegistry the registry to get user data from
     * @param viewRoute the view route to filter by (e.g., "muc-01")
     */
    public ActiveUsersDisplay(UserSessionRegistry userSessionRegistry,
            String viewRoute) {
        this(userSessionRegistry, "Active on this view", viewRoute, false);
    }

    /**
     * Creates an active users display with custom label text and optional accent
     * border.
     *
     * @param userSessionRegistry the registry to get user data from
     * @param labelText the text to show before the count (e.g., "Active users",
     *            "Active sessions")
     * @param showAccentBorder if true, shows a colored left border
     */
    public ActiveUsersDisplay(UserSessionRegistry userSessionRegistry,
            String labelText, boolean showAccentBorder) {
        this(userSessionRegistry, labelText, null, showAccentBorder);
    }

    /**
     * Creates an active users display with all customization options.
     *
     * @param userSessionRegistry the registry to get user data from
     * @param labelText the text to show before the count (e.g., "Active users",
     *            "Active sessions")
     * @param viewRoute the view route to filter by, or null for all users
     * @param showAccentBorder if true, shows a colored left border
     */
    public ActiveUsersDisplay(UserSessionRegistry userSessionRegistry,
            String labelText, String viewRoute, boolean showAccentBorder) {
        // Container styling
        getStyle().set("background-color", "#fff3e0").set("padding", "0.75em")
                .set("border-radius", "4px").set("margin-bottom", "1em")
                .set("display", "flex").set("flex-direction", "column")
                .set("gap", "0.5em");

        if (showAccentBorder) {
            getStyle().set("border-left",
                    "4px solid var(--lumo-warning-color)");
        }

        // Title with count
        Span title = new Span();
        title.getStyle().set("font-weight", "500");

        // Users container with avatars
        Div usersContainer = new Div();
        usersContainer.getStyle().set("display", "flex")
                .set("flex-wrap", "wrap").set("gap", "0.5em");

        // Determine which signal to use based on viewRoute
        Signal<java.util.List<String>> displayNamesSignal = viewRoute != null
                ? userSessionRegistry.getActiveUsersOnView(viewRoute)
                : userSessionRegistry.getDisplayNamesSignal();

        // Bind title with count
        title.bindText(displayNamesSignal
                .map(displayNames -> "👥 " + labelText + ": "
                        + displayNames.size()));

        // Bind user avatars and names
        MissingAPI.bindChildren(usersContainer, Signal.computed(() -> {
            var displayNames = displayNamesSignal.value();
            var users = userSessionRegistry.getActiveUsersSignal().value();

            // Filter users if viewRoute is specified
            java.util.List<com.example.signals.UserInfo> filteredUsers;
            if (viewRoute != null) {
                filteredUsers = users.stream()
                        .map(userSignal -> userSignal.value())
                        .filter(user -> viewRoute.equals(user.currentView()))
                        .toList();
            } else {
                filteredUsers = users.stream()
                        .map(userSignal -> userSignal.value()).toList();
            }

            return filteredUsers.stream().map(user -> {
                // Display name (use nickname if set, otherwise username)
                String displayName = user.nickname() != null
                        && !user.nickname().isEmpty() ? user.nickname()
                                : user.username();

                HorizontalLayout userItem = new HorizontalLayout();
                userItem.setSpacing(true);
                userItem.setAlignItems(
                        com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
                userItem.getStyle().set("padding", "0.25em 0.5em")
                        .set("background-color", "rgba(255, 255, 255, 0.7)")
                        .set("border-radius", "16px");

                // Avatar (default 40x40 size)
                Avatar avatar = new Avatar(displayName);
                avatar.setImage(MainLayout.getProfilePicturePath(user.username()));

                Span nameLabel = new Span(displayName);
                nameLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");

                userItem.add(avatar, nameLabel);
                return userItem;
            }).toList();
        }));

        add(title, usersContainer);
    }
}
