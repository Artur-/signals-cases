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

    // Computed signal for display names with session numbers
    private final Signal<java.util.List<String>> displayNamesSignal = Signal
            .computed(() -> {
                var userSignals = activeUsersSignal.value();

                // Count sessions per username
                java.util.Map<String, Integer> sessionCounts = new java.util.HashMap<>();
                java.util.Map<String, Integer> currentSessionNumber = new java.util.HashMap<>();

                for (var userSignal : userSignals) {
                    String username = userSignal.value().username();
                    sessionCounts.merge(username, 1, Integer::sum);
                }

                // Generate display names with session numbers
                java.util.List<String> displayNames = new java.util.ArrayList<>();
                for (var userSignal : userSignals) {
                    String username = userSignal.value().username();
                    int sessionNum = currentSessionNumber.merge(username, 1,
                            (a, b) -> a + 1);

                    String displayName;
                    if (sessionCounts.get(username) > 1) {
                        // Multiple sessions - show number
                        displayName = username + " #" + sessionNum;
                    } else {
                        // Single session - no number needed
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

}
