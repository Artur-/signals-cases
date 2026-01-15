package com.example.signals;

import org.springframework.stereotype.Component;

import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;

/**
 * Application-scoped registry of currently logged-in users. Provides a reactive
 * signal containing the list of active users.
 */
@Component
public class UserSessionRegistry {

    private final ListSignal<UserInfo> activeUsersSignal = new ListSignal<>(
            UserInfo.class);

    // Computed signal for display names with session numbers and nicknames
    private final Signal<java.util.List<String>> displayNamesSignal = Signal
            .computed(() -> {
                var userSignals = activeUsersSignal.value();

                // Count sessions per username for auto-naming
                java.util.Map<String, Integer> sessionCounts = new java.util.HashMap<>();
                for (var userSignal : userSignals) {
                    String username = userSignal.value().username();
                    sessionCounts.merge(username, 1, Integer::sum);
                }

                // Generate display names
                java.util.Map<String, Integer> currentSessionNumber = new java.util.HashMap<>();
                java.util.List<String> displayNames = new java.util.ArrayList<>();

                for (var userSignal : userSignals) {
                    UserInfo user = userSignal.value();

                    // Check for nickname first
                    if (user.nickname() != null && !user.nickname().isEmpty()) {
                        displayNames.add(user.nickname());
                        continue;
                    }

                    // Fall back to auto-generated name
                    String username = user.username();
                    int sessionNum = currentSessionNumber.merge(username, 1,
                            (a, b) -> a + 1);

                    String displayName;
                    if (sessionCounts.get(username) > 1) {
                        displayName = username + " #" + sessionNum;
                    } else {
                        displayName = username;
                    }
                    displayNames.add(displayName);
                }

                return displayNames;
            });

    /**
     * Get the signal containing the list of active users.
     */
    public ListSignal<UserInfo> getActiveUsersSignal() {
        return activeUsersSignal;
    }

    /**
     * Get the computed signal containing formatted display names with session
     * numbers.
     */
    public Signal<java.util.List<String>> getDisplayNamesSignal() {
        return displayNamesSignal;
    }

    /**
     * Register a user as active with a specific session ID.
     */
    public void registerUser(String username, String sessionId) {
        if (username == null || sessionId == null) {
            throw new IllegalArgumentException(
                    "Username and sessionId cannot be null");
        }

        // Check using composite key
        String compositeKey = username + ":" + sessionId;
        boolean exists = activeUsersSignal.value().stream().anyMatch(
                userSignal -> compositeKey.equals(
                        userSignal.value().getCompositeKey()));

        if (!exists) {
            activeUsersSignal.insertLast(new UserInfo(username, sessionId));
        }
    }

    /**
     * Unregister a user (e.g., on logout or session timeout).
     */
    public void unregisterUser(String username, String sessionId) {
        if (username == null || sessionId == null) {
            throw new IllegalArgumentException(
                    "Username and sessionId cannot be null");
        }

        String compositeKey = username + ":" + sessionId;
        activeUsersSignal.value().stream()
                .filter(userSignal -> compositeKey.equals(
                        userSignal.value().getCompositeKey()))
                .findFirst().ifPresent(activeUsersSignal::remove);
    }

    /**
     * Get count of active users.
     */
    public int getActiveUserCount() {
        return activeUsersSignal.value().size();
    }

    /**
     * Check if a user is currently active (any session).
     */
    public boolean isUserActive(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        return activeUsersSignal.value().stream().anyMatch(
                userSignal -> username.equals(userSignal.value().username()));
    }

    /**
     * Check if a specific session is currently active.
     */
    public boolean isSessionActive(String username, String sessionId) {
        if (username == null || sessionId == null) {
            throw new IllegalArgumentException(
                    "Username and sessionId cannot be null");
        }

        String compositeKey = username + ":" + sessionId;
        return activeUsersSignal.value().stream().anyMatch(
                userSignal -> compositeKey.equals(
                        userSignal.value().getCompositeKey()));
    }

    /**
     * Set a custom nickname for a user session.
     */
    public void setNickname(String username, String sessionId, String nickname) {
        String compositeKey = username + ":" + sessionId;
        String trimmedNickname = (nickname == null || nickname.trim().isEmpty())
                ? null
                : nickname.trim();

        activeUsersSignal.value().stream()
                .filter(userSignal -> compositeKey.equals(
                        userSignal.value().getCompositeKey()))
                .findFirst().ifPresent(userSignal -> {
                    UserInfo oldInfo = userSignal.value();
                    userSignal.value(oldInfo.withNickname(trimmedNickname));
                });
    }

    /**
     * Get the nickname for a user session, or null if not set.
     */
    public String getNickname(String username, String sessionId) {
        String compositeKey = username + ":" + sessionId;
        return activeUsersSignal.value().stream()
                .filter(userSignal -> compositeKey.equals(
                        userSignal.value().getCompositeKey()))
                .findFirst().map(userSignal -> userSignal.value().nickname())
                .orElse(null);
    }

    /**
     * Update the current view for a user session.
     */
    public void updateUserView(String username, String sessionId,
            String viewRoute) {
        String compositeKey = username + ":" + sessionId;

        // Find the user and update their view
        activeUsersSignal.value().stream()
                .filter(userSignal -> compositeKey.equals(
                        userSignal.value().getCompositeKey()))
                .findFirst().ifPresent(userSignal -> {
                    UserInfo oldInfo = userSignal.value();
                    userSignal.value(oldInfo.withCurrentView(viewRoute));
                });
    }

    /**
     * Get a reactive signal of display names for users on a specific view.
     */
    public Signal<java.util.List<String>> getActiveUsersOnView(
            String viewRoute) {
        return Signal.computed(() -> {
            var allUsers = activeUsersSignal.value();
            var allDisplayNames = displayNamesSignal.value();

            java.util.List<String> result = new java.util.ArrayList<>();
            for (int i = 0; i < allUsers.size(); i++) {
                UserInfo user = allUsers.get(i).value();
                if (viewRoute.equals(user.currentView())) {
                    result.add(allDisplayNames.get(i));
                }
            }
            return result;
        });
    }

}
