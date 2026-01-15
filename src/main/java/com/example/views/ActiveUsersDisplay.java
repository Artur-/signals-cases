package com.example.views;

import com.example.signals.UserSessionRegistry;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

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
                .set("border-radius", "4px").set("margin-bottom", "1em");

        if (showAccentBorder) {
            getStyle().set("border-left",
                    "4px solid var(--lumo-warning-color)");
        }

        // Label with reactive binding - filter by view if specified
        Span label = new Span();
        if (viewRoute != null) {
            // Show only users on this specific view
            label.bindText(
                    userSessionRegistry.getActiveUsersOnView(viewRoute)
                            .map(displayNames -> {
                                String usernames = String.join(", ",
                                        displayNames);
                                return "👥 " + labelText + ": "
                                        + displayNames.size() + " (" + usernames
                                        + ")";
                            }));
        } else {
            // Show all users across all views
            label.bindText(userSessionRegistry.getDisplayNamesSignal()
                    .map(displayNames -> {
                        String usernames = String.join(", ", displayNames);
                        return "👥 " + labelText + ": " + displayNames.size()
                                + " (" + usernames + ")";
                    }));
        }
        label.getStyle().set("font-weight", "500");

        add(label);
    }
}
