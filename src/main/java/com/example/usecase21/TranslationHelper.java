package com.example.usecase21;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;

/**
 * Helper class that bridges ResourceBundle and Signals for reactive i18n.
 *
 * Each call to t(key) returns a computed signal that recomputes when the locale changes.
 * ResourceBundle.getBundle() has built-in caching so repeated calls are efficient.
 */
public class TranslationHelper {

    private static final String BUNDLE_NAME = "i18n.messages";

    private final WritableSignal<Locale> localeSignal;

    public TranslationHelper(WritableSignal<Locale> localeSignal) {
        this.localeSignal = localeSignal;
    }

    /**
     * Returns a computed signal that provides the translated string for the given key.
     * The signal automatically recomputes when the locale changes.
     *
     * @param key the translation key
     * @return a Signal containing the translated string, or "!key!" if not found
     */
    public Signal<String> t(String key) {
        return Signal.computed(() -> {
            Locale locale = localeSignal.value();
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
                if (bundle.containsKey(key)) {
                    return bundle.getString(key);
                }
            } catch (MissingResourceException e) {
                // Fall through to return missing key indicator
            }
            return "!" + key + "!";
        });
    }

    /**
     * Returns the current locale signal for direct access.
     */
    public WritableSignal<Locale> getLocaleSignal() {
        return localeSignal;
    }
}
