package com.example.preferences;

import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.local.ValueSignal;

/**
 * Session-scoped user preference state.
 *
 * Requirements:
 * - Session scoped lifecycle per user session
 * - Holds a Signal with the selected background color
 * - Holds a Signal with the selected locale for i18n
 * - Injectable into views/layouts
 */
@Component
@SessionScope
public class UserPreferences {

    public static final String DEFAULT_COLOR = "var(--lumo-base-color)"; // uses theme default

    private final WritableSignal<String> backgroundColorSignal;
    private final WritableSignal<Locale> localeSignal;

    /**
     * No-args constructor to allow Spring to instantiate the bean
     * while providing a sensible default value.
     */
    public UserPreferences() {
        this.backgroundColorSignal = new ValueSignal<>(DEFAULT_COLOR);
        this.localeSignal = new ValueSignal<>(Locale.ENGLISH);
    }

    /**
     * Optional constructor for tests or advanced wiring.
     */
    public UserPreferences(WritableSignal<String> backgroundColorSignal,
                           WritableSignal<Locale> localeSignal) {
        this.backgroundColorSignal = backgroundColorSignal != null
                ? backgroundColorSignal
                : new ValueSignal<>(DEFAULT_COLOR);
        this.localeSignal = localeSignal != null
                ? localeSignal
                : new ValueSignal<>(Locale.ENGLISH);
    }

    /**
     * Accessor used by views/layouts for binding.
     */
    public WritableSignal<String> backgroundColorSignal() {
        return backgroundColorSignal;
    }

    /**
     * Accessor for locale signal used for i18n.
     */
    public WritableSignal<Locale> localeSignal() {
        return localeSignal;
    }

}
