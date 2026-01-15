package com.example.signals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.vaadin.signals.ListSignal;
import com.vaadin.signals.MapSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Application-scoped signals for collaborative use cases. These signals are
 * shared across all user sessions.
 */
@Component
public class CollaborativeSignals {

    // ========== Use Case 18: Shared Chat ===========

    public record Message(String username, String author, String text,
            LocalDateTime timestamp) {
        public Message(String username, String author, String text) {
            this(username, author, text, LocalDateTime.now());
        }

        public String getFormattedTimestamp() {
            return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    private final ListSignal<Message> messagesSignal = new ListSignal<>(
            Message.class);

    public ListSignal<Message> getMessagesSignal() {
        return messagesSignal;
    }

    public void appendMessage(Message message) {
        messagesSignal.insertLast(message);
    }

    public void clearMessages() {
        messagesSignal.clear();
    }

    // ========== Use Case 19: Cursor Positions ===========

    public record CursorPosition(int x, int y) {
        // Default constructor for Jackson deserialization
        public CursorPosition() {
            this(0, 0);
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    // MapSignal where key is "username:sessionId" and value is CursorPosition
    private final MapSignal<CursorPosition> sessionCursorsSignal = new MapSignal<>(
            CursorPosition.class);

    public MapSignal<CursorPosition> getSessionCursorsSignal() {
        return sessionCursorsSignal;
    }

    public WritableSignal<CursorPosition> getCursorSignalForUser(
            String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        return sessionCursorsSignal
                .putIfAbsent(sessionKey, new CursorPosition(0, 0)).signal();
    }

    public void unregisterCursor(String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        sessionCursorsSignal.remove(sessionKey);
    }

    // ========== Use Case 20: Click Game ===========

    // MapSignal where key is "username:sessionId" and value is score (Integer)
    private final MapSignal<Integer> leaderboardSignal = new MapSignal<>(
            Integer.class);

    private final WritableSignal<Boolean> buttonVisibleSignal = new ValueSignal<>(
            false);
    private final WritableSignal<Integer> buttonLeftSignal = new ValueSignal<>(
            0);
    private final WritableSignal<Integer> buttonTopSignal = new ValueSignal<>(
            0);
    private final WritableSignal<Integer> clicksRemainingSignal = new ValueSignal<>(
            0);
    private final WritableSignal<Integer> roundNumberSignal = new ValueSignal<>(
            0);

    public MapSignal<Integer> getLeaderboardSignal() {
        return leaderboardSignal;
    }

    public WritableSignal<Boolean> getButtonVisibleSignal() {
        return buttonVisibleSignal;
    }

    public WritableSignal<Integer> getButtonLeftSignal() {
        return buttonLeftSignal;
    }

    public WritableSignal<Integer> getButtonTopSignal() {
        return buttonTopSignal;
    }

    public WritableSignal<Integer> getClicksRemainingSignal() {
        return clicksRemainingSignal;
    }

    public WritableSignal<Integer> getRoundNumberSignal() {
        return roundNumberSignal;
    }

    public void initializePlayerScore(String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        leaderboardSignal.putIfAbsent(sessionKey, 0);
    }

    public synchronized boolean awardPoint(String username, String sessionId) {
        if (!buttonVisibleSignal.value()
                || clicksRemainingSignal.value() <= 0) {
            return false; // Round already finished
        }

        // Award the point
        String sessionKey = username + ":" + sessionId;
        ValueSignal<Integer> scoreSignal = leaderboardSignal.value()
                .get(sessionKey);
        if (scoreSignal != null) {
            scoreSignal.value(scoreSignal.value() + 1);
        } else {
            // Initialize if not present
            leaderboardSignal.put(sessionKey, 1);
        }

        // Decrement clicks remaining
        int remaining = clicksRemainingSignal.value() - 1;
        clicksRemainingSignal.value(remaining);

        // Hide button temporarily (will be repositioned by view)
        buttonVisibleSignal.value(false);

        // Return true if more clicks remain in this round
        return remaining > 0;
    }

    public void startNewRound(int left, int top) {
        buttonLeftSignal.value(left);
        buttonTopSignal.value(top);
        clicksRemainingSignal.value(5); // 5 clicks per round
        roundNumberSignal.value(roundNumberSignal.value() + 1);
        buttonVisibleSignal.value(true);
    }

    public void repositionButton(int left, int top) {
        buttonLeftSignal.value(left);
        buttonTopSignal.value(top);
        buttonVisibleSignal.value(true);
    }

    public void resetLeaderboard() {
        leaderboardSignal.clear();
    }

    public void unregisterScore(String username, String sessionId) {
        String sessionKey = username + ":" + sessionId;
        leaderboardSignal.remove(sessionKey);
    }

    // ========== Use Case 21: Form Locking ===========

    public record FieldLock(String username, String sessionId) {
    }

    private final WritableSignal<String> companyNameSignal = new ValueSignal<>(
            "");
    private final WritableSignal<String> addressSignal = new ValueSignal<>("");
    private final WritableSignal<String> phoneSignal = new ValueSignal<>("");
    // MapSignal where key is fieldName and value is FieldLock
    private final MapSignal<FieldLock> fieldLocksSignal = new MapSignal<>(
            FieldLock.class);

    public WritableSignal<String> getCompanyNameSignal() {
        return companyNameSignal;
    }

    public WritableSignal<String> getAddressSignal() {
        return addressSignal;
    }

    public WritableSignal<String> getPhoneSignal() {
        return phoneSignal;
    }

    public MapSignal<FieldLock> getFieldLocksSignal() {
        return fieldLocksSignal;
    }

    public void lockField(String fieldName, String username, String sessionId) {
        fieldLocksSignal.put(fieldName, new FieldLock(username, sessionId));
    }

    public void unlockField(String fieldName, String username,
            String sessionId) {
        ValueSignal<FieldLock> lockSignal = fieldLocksSignal.value()
                .get(fieldName);
        if (lockSignal != null) {
            FieldLock lock = lockSignal.value();
            if (lock != null && username.equals(lock.username())
                    && sessionId.equals(lock.sessionId())) {
                fieldLocksSignal.remove(fieldName);
            }
        }
    }

    public boolean isFieldLockedByOther(String fieldName, String username,
            String sessionId) {
        ValueSignal<FieldLock> lockSignal = fieldLocksSignal.value()
                .get(fieldName);
        if (lockSignal == null) {
            return false;
        }
        FieldLock lockOwner = lockSignal.value();
        return lockOwner != null && !(username.equals(lockOwner.username())
                && sessionId.equals(lockOwner.sessionId()));
    }

    // ========== MUC 6: Shared Task List ===========

    public record Task(String id, String title, boolean completed,
            java.time.LocalDate dueDate) {
    }

    private final ListSignal<Task> tasksSignal = new ListSignal<>(Task.class);

    public ListSignal<Task> getTasksSignal() {
        return tasksSignal;
    }

    public void initializeSampleTasks() {
        if (tasksSignal.value().isEmpty()) {
            tasksSignal.insertLast(new Task("task-1", "Review pull requests",
                    false, java.time.LocalDate.now()));
            tasksSignal.insertLast(new Task("task-2", "Update documentation",
                    true, java.time.LocalDate.now().plusDays(1)));
            tasksSignal.insertLast(new Task("task-3", "Fix bug #123", false,
                    java.time.LocalDate.now().plusDays(2)));
            tasksSignal.insertLast(new Task("task-4", "Prepare demo", false,
                    java.time.LocalDate.now().plusWeeks(1)));
        }
    }
}
