package com.example.signals;

import com.vaadin.signals.ListSignal;
import org.springframework.stereotype.Component;

/**
 * Application-scoped registry of currently logged-in users.
 * Provides a reactive signal containing the list of active users.
 */
@Component
public class UserSessionRegistry {

    private final ListSignal<UserInfo> activeUsersSignal = new ListSignal<>();

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
        boolean exists = activeUsersSignal.stream()
            .anyMatch(u -> u.username().equals(username));

        if (!exists) {
            activeUsersSignal.add(new UserInfo(username));
        }
    }

    /**
     * Unregister a user (e.g., on logout or session timeout).
     */
    public void unregisterUser(String username) {
        activeUsersSignal.removeIf(u -> u.username().equals(username));
    }

    /**
     * Get count of active users.
     */
    public int getActiveUserCount() {
        return activeUsersSignal.size();
    }

    /**
     * Check if a user is currently active.
     */
    public boolean isUserActive(String username) {
        return activeUsersSignal.stream()
            .anyMatch(u -> u.username().equals(username));
    }
}

