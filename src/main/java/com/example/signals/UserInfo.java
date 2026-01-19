package com.example.signals;

/**
 * Information about an active user session.
 */
public record UserInfo(String username, String sessionId, String currentView,
        String nickname, long sessionStartTime, boolean isTabActive,
        long lastInteractionTime) {

    // Constructor with current view and nickname (legacy compatibility)
    public UserInfo(String username, String sessionId, String currentView,
            String nickname) {
        this(username, sessionId, currentView, nickname,
                System.currentTimeMillis(), true, System.currentTimeMillis());
    }

    // Constructor without view or nickname (defaults to null)
    public UserInfo(String username, String sessionId) {
        this(username, sessionId, null, null, System.currentTimeMillis(), true,
                System.currentTimeMillis());
    }

    // Generate composite key for tracking
    public String getCompositeKey() {
        return username + ":" + sessionId;
    }

    // Helper to create updated instance with new view
    public UserInfo withCurrentView(String newView) {
        return new UserInfo(username, sessionId, newView, nickname,
                sessionStartTime, isTabActive, lastInteractionTime);
    }

    // Helper to create updated instance with new nickname
    public UserInfo withNickname(String newNickname) {
        return new UserInfo(username, sessionId, currentView, newNickname,
                sessionStartTime, isTabActive, lastInteractionTime);
    }

    // Helper to create updated instance with new tab activity state
    public UserInfo withTabActive(boolean newTabActive) {
        return new UserInfo(username, sessionId, currentView, nickname,
                sessionStartTime, newTabActive, lastInteractionTime);
    }

    // Helper to create updated instance with new last interaction time
    public UserInfo withLastInteractionTime(long newLastInteractionTime) {
        return new UserInfo(username, sessionId, currentView, nickname,
                sessionStartTime, isTabActive, newLastInteractionTime);
    }
}
