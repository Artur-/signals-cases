package com.example.signals;

import com.vaadin.signals.ListSignal;
import org.springframework.stereotype.Component;

/**
 * Application-scoped registry of currently logged-in users.
 * Provides a reactive signal containing the list of active users.
 */
@Component
public class UserSessionRegistry {

    private final ListSignal<UserInfo> activeUsersSignal = new ListSignal<>(UserInfo.class);

    /**
     * Get the signal containing the list of active users.
     */
    public ListSignal<UserInfo> getActiveUsersSignal() {
        return activeUsersSignal;
    }

    /**
     * Register a user as active.
     */
    public void registerUser(String username) {
        // Check if user is already registered
        boolean exists = activeUsersSignal.value().stream()
            .anyMatch(userSignal -> userSignal.value().username().equals(username));

        if (!exists) {
            activeUsersSignal.insertLast(new UserInfo(username));
        }
    }

    /**
     * Unregister a user (e.g., on logout or session timeout).
     */
    public void unregisterUser(String username) {
        activeUsersSignal.value().stream()
            .filter(userSignal -> userSignal.value().username().equals(username))
            .findFirst()
            .ifPresent(activeUsersSignal::remove);
    }

    /**
     * Get count of active users.
     */
    public int getActiveUserCount() {
        return activeUsersSignal.value().size();
    }

    /**
     * Check if a user is currently active.
     */
    public boolean isUserActive(String username) {
        return activeUsersSignal.value().stream()
            .anyMatch(userSignal -> userSignal.value().username().equals(username));
    }
}

