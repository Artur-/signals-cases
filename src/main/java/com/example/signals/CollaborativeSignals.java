package com.example.signals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.vaadin.signals.ListSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Application-scoped signals for collaborative use cases. These signals are
 * shared across all user sessions.
 */
@Component
public class CollaborativeSignals {

    // ========== Use Case 18: Shared Chat ===========

    public record Message(String author, String text, LocalDateTime timestamp) {
        public Message(String author, String text) {
            this(author, text, LocalDateTime.now());
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
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    private final Map<String, WritableSignal<CursorPosition>> userCursors = new ConcurrentHashMap<>();

    public Map<String, WritableSignal<CursorPosition>> getUserCursors() {
        return userCursors;
    }

    public WritableSignal<CursorPosition> getCursorSignalForUser(
            String username) {
        return userCursors.computeIfAbsent(username,
                k -> new ValueSignal<>(new CursorPosition(0, 0)));
    }

    // ========== Use Case 20: Click Game ===========

    private final WritableSignal<Map<String, Integer>> leaderboardSignal = new ValueSignal<>(
            new ConcurrentHashMap<>());

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

    public WritableSignal<Map<String, Integer>> getLeaderboardSignal() {
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

    public void initializePlayerScore(String username) {
        Map<String, Integer> scores = new ConcurrentHashMap<>(
                leaderboardSignal.value());
        scores.putIfAbsent(username, 0);
        leaderboardSignal.value(scores);
    }

    public synchronized boolean awardPoint(String username) {
        if (!buttonVisibleSignal.value()
                || clicksRemainingSignal.value() <= 0) {
            return false; // Round already finished
        }

        // Award the point
        Map<String, Integer> scores = new ConcurrentHashMap<>(
                leaderboardSignal.value());
        scores.merge(username, 1, Integer::sum);
        leaderboardSignal.value(scores);

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
        leaderboardSignal.value(new ConcurrentHashMap<>());
    }

    // ========== Use Case 21: Form Locking ===========

    private final WritableSignal<String> companyNameSignal = new ValueSignal<>(
            "");
    private final WritableSignal<String> addressSignal = new ValueSignal<>("");
    private final WritableSignal<String> phoneSignal = new ValueSignal<>("");
    private final WritableSignal<Map<String, String>> fieldLocksSignal = new ValueSignal<>(
            new ConcurrentHashMap<>());

    public WritableSignal<String> getCompanyNameSignal() {
        return companyNameSignal;
    }

    public WritableSignal<String> getAddressSignal() {
        return addressSignal;
    }

    public WritableSignal<String> getPhoneSignal() {
        return phoneSignal;
    }

    public WritableSignal<Map<String, String>> getFieldLocksSignal() {
        return fieldLocksSignal;
    }

    public void lockField(String fieldName, String username) {
        Map<String, String> locks = new ConcurrentHashMap<>(
                fieldLocksSignal.value());
        locks.put(fieldName, username);
        fieldLocksSignal.value(locks);
    }

    public void unlockField(String fieldName, String username) {
        Map<String, String> locks = new ConcurrentHashMap<>(
                fieldLocksSignal.value());
        if (username.equals(locks.get(fieldName))) {
            locks.remove(fieldName);
            fieldLocksSignal.value(locks);
        }
    }

    public boolean isFieldLockedByOther(String fieldName, String username) {
        String lockOwner = fieldLocksSignal.value().get(fieldName);
        return lockOwner != null && !lockOwner.equals(username);
    }
}
