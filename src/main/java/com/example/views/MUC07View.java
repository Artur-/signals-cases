package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.util.UUID;

import com.example.security.CurrentUserSignal;
import com.example.service.TaskLLMService;
import com.example.signals.CollaborativeSignals;
import com.example.signals.UserSessionRegistry;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "muc-07", layout = MainLayout.class)
@PageTitle("MUC 7: Shared LLM Task List")
@Menu(order = 56, title = "MUC 7: Shared LLM Task List")
@PermitAll
public class MUC07View extends AbstractTaskChatView {

    public MUC07View(CurrentUserSignal currentUserSignal,
                     CollaborativeSignals collaborativeSignals,
                     UserSessionRegistry userSessionRegistry,
                     TaskLLMService taskLLMService) {

        // Call super with SHARED signals - must be first statement
        super(
            collaborativeSignals.getLlmTasksSignal(),           // Shared task signal
            collaborativeSignals.getLlmChatMessagesSignal(),    // Shared chat signal
            taskLLMService,
            getUserConversationId(currentUserSignal),           // Per-user conversation ID
            currentUserSignal                                   // Current user for avatar/name
        );

        // Add active users display
        ActiveUsersDisplay activeUsersDisplay = new ActiveUsersDisplay(
            userSessionRegistry, "Active on this view", "muc-07", true
        );
        addComponentAsFirst(activeUsersDisplay);  // Add before chat panel
    }

    private static String getUserConversationId(CurrentUserSignal currentUserSignal) {
        CurrentUserSignal.UserInfo userInfo = currentUserSignal.getUserSignal().value();
        if (userInfo == null || !userInfo.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated");
        }
        return userInfo.getUsername() + ":" + UUID.randomUUID();
    }
}
