package com.example.signals;

import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Application-scoped registry of currently logged-in users.
 * Provides a reactive signal containing the list of active users.
 */
@Component
public class UserSessionRegistry {

    public record UserInfo(String username, long sessionStartTime) {
        public UserInfo(String username) {
            this(username, System.currentTimeMillis());
        }
    }

    private final WritableSignal<List<UserInfo>> activeUsersSignal =
        new ValueSignal<>(new CopyOnWriteArrayList<>());

    /**
     * Get the signal containing the list of active users.
     */
    public WritableSignal<List<UserInfo>> getActiveUsersSignal() {
        return activeUsersSignal;
    }

    /**
     * Register a user as active.
     */
    public void registerUser(String username) {
        List<UserInfo> users = new ArrayList<>(activeUsersSignal.value());

        // Check if user is already registered
        boolean exists = users.stream()
            .anyMatch(u -> u.username().equals(username));

        if (!exists) {
            users.add(new UserInfo(username));
            activeUsersSignal.value(users);
        }
    }

    /**
     * Unregister a user (e.g., on logout or session timeout).
     */
    public void unregisterUser(String username) {
        List<UserInfo> users = new ArrayList<>(activeUsersSignal.value());
        users.removeIf(u -> u.username().equals(username));
        activeUsersSignal.value(users);
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
            .anyMatch(u -> u.username().equals(username));
    }
}
