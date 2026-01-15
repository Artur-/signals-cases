package com.example.signals;

/**
 * Information about an active user session.
 */
public record UserInfo(String username, String sessionId, String currentView,
        String nickname, long sessionStartTime) {

    // Constructor with current view and nickname
    public UserInfo(String username, String sessionId, String currentView,
            String nickname) {
        this(username, sessionId, currentView, nickname,
                System.currentTimeMillis());
    }

    // Constructor without view or nickname (defaults to null)
    public UserInfo(String username, String sessionId) {
        this(username, sessionId, null, null, System.currentTimeMillis());
    }

    // Generate composite key for tracking
    public String getCompositeKey() {
        return username + ":" + sessionId;
    }

    // Helper to create updated instance with new view
    public UserInfo withCurrentView(String newView) {
        return new UserInfo(username, sessionId, newView, nickname,
                sessionStartTime);
    }

    // Helper to create updated instance with new nickname
    public UserInfo withNickname(String newNickname) {
        return new UserInfo(username, sessionId, currentView, newNickname,
                sessionStartTime);
    }
}
