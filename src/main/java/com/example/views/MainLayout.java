package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.util.stream.Collectors;

import com.example.security.CurrentUserSignal;
import com.example.signals.SessionIdHelper;
import com.example.signals.UserSessionRegistry;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.menu.MenuConfiguration;

@PageTitle("Signal API Use Cases")
@PermitAll
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final CurrentUserSignal currentUserSignal;
    private final UserSessionRegistry userSessionRegistry;
    private String currentUser;
    private String sessionId;
    private TextField nicknameField;

    public MainLayout(CurrentUserSignal currentUserSignal,
            UserSessionRegistry userSessionRegistry) {
        this.currentUserSignal = currentUserSignal;
        this.userSessionRegistry = userSessionRegistry;

        // Get current user info
        CurrentUserSignal.UserInfo userInfo = currentUserSignal.getUserSignal()
                .value();
        if (userInfo != null && userInfo.isAuthenticated()) {
            this.currentUser = userInfo.getUsername();
        }

        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Signal API Use Cases");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        // Active users display using signal
        Span activeUsersDisplay = new Span();
        activeUsersDisplay.getStyle().set("margin-left", "auto")
                .set("margin-right", "1em")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");
        activeUsersDisplay.bindText(
                userSessionRegistry.getDisplayNamesSignal().map(displayNames -> {
                    if (displayNames.isEmpty()) {
                        return "";
                    }
                    String usernames = String.join(", ", displayNames);
                    return "👥 " + displayNames.size() + " online: " + usernames;
                }));

        // Nickname setting UI
        nicknameField = new TextField();
        nicknameField.setPlaceholder("Set nickname...");
        nicknameField.setWidth("150px");
        nicknameField.setClearButtonVisible(true);
        nicknameField.getStyle().set("margin-right", "1em");

        // Save nickname on change
        nicknameField.addValueChangeListener(event -> {
            if (currentUser != null && sessionId != null) {
                String nickname = event.getValue();
                userSessionRegistry.setNickname(currentUser, sessionId, nickname);
            }
        });

        // Current user display using signal
        Span userDisplay = new Span();
        userDisplay.getStyle().set("margin-right", "1em")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");
        userDisplay.bindText(currentUserSignal.getUserSignal()
                .map(user -> user.isAuthenticated() ? "👤 " + user.getUsername()
                        : ""));

        addToNavbar(toggle, title, activeUsersDisplay, nicknameField,
                userDisplay);

        // Add auto-menu from @Menu annotations
        SideNav nav = new SideNav();
        MenuConfiguration.getMenuEntries().forEach(entry -> nav
                .addItem(new SideNavItem(entry.title(), entry.path())));
        addToDrawer(nav);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.sessionId = SessionIdHelper.getCurrentSessionId();

        if (currentUser != null && sessionId != null) {
            // Load current nickname if exists
            String currentNickname = userSessionRegistry.getNickname(currentUser,
                    sessionId);
            if (currentNickname != null) {
                nicknameField.setValue(currentNickname);
            }
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        if (currentUser != null && sessionId != null) {
            userSessionRegistry.unregisterUser(currentUser, sessionId);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Get sessionId if not already set (beforeEnter fires before onAttach)
        if (sessionId == null) {
            sessionId = SessionIdHelper.getCurrentSessionId();
        }

        // Register user with current route (or update route if already registered)
        if (currentUser != null && sessionId != null) {
            String viewRoute = event.getLocation().getPath();
            userSessionRegistry.registerUser(currentUser, sessionId, viewRoute);
        }
    }
}
