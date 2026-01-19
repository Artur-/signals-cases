package com.example.preferences;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;

/**
 * Session-scoped user preference state.
 *
 * Requirements:
 * - Session scoped lifecycle per user session
 * - Holds a Signal with the selected background color
 * - Injectable into views/layouts
 */
@Component
@SessionScope
public class UserPreferences {

    public static final String DEFAULT_COLOR = "var(--lumo-base-color)"; // uses theme default

    private final WritableSignal<String> backgroundColorSignal;

    /**
     * No-args constructor to allow Spring to instantiate the bean
     * while providing a sensible default value.
     */
    public UserPreferences() {
        this.backgroundColorSignal = new ValueSignal<>(DEFAULT_COLOR);
    }

    /**
     * Optional constructor for tests or advanced wiring.
     */
    public UserPreferences(WritableSignal<String> backgroundColorSignal) {
        this.backgroundColorSignal = backgroundColorSignal != null
                ? backgroundColorSignal
                : new ValueSignal<>(DEFAULT_COLOR);
    }

    /**
     * Accessor used by views/layouts for binding.
     */
    public WritableSignal<String> backgroundColorSignal() {
        return backgroundColorSignal;
    }

}
