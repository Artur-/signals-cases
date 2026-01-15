package com.example.signals;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

/**
 * Helper class for generating globally unique session identifiers.
 * Combines VaadinSession ID (HTTP session) with UI ID to ensure uniqueness
 * across all browser tabs and sessions.
 */
public class SessionIdHelper {

    /**
     * Get the current globally unique session ID.
     * Format: {vaadinSessionId}:{uiId}
     *
     * @return unique session identifier for the current UI instance
     */
    public static String getCurrentSessionId() {
        VaadinSession session = VaadinSession.getCurrent();
        UI ui = UI.getCurrent();

        if (session == null || ui == null) {
            throw new IllegalStateException(
                    "Cannot get session ID outside of Vaadin request context");
        }

        String vaadinSessionId = session.getSession().getId();
        int uiId = ui.getUIId();

        return vaadinSessionId + ":" + uiId;
    }
}
