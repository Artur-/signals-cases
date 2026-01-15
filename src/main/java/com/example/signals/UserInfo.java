package com.example.signals;

/**
 * Information about an active user session.
 */
public record UserInfo(String username, String sessionId,
        long sessionStartTime) {

    // Convenience constructor
    public UserInfo(String username, String sessionId) {
        this(username, sessionId, System.currentTimeMillis());
    }

    // Generate composite key for tracking
    public String getCompositeKey() {
        return username + ":" + sessionId;
    }
}
