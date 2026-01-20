package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.util.stream.Collectors;

import com.example.security.CurrentUserSignal;
import com.example.signals.SessionIdHelper;
import com.example.signals.UserSessionRegistry;
import com.example.preferences.UserPreferences;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.menu.MenuConfiguration;

@PageTitle("Signal API Use Cases")
@PermitAll
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final CurrentUserSignal currentUserSignal;
    private final UserSessionRegistry userSessionRegistry;
    private final UserPreferences userPreferences;
    private String currentUser;
    private String sessionId;
    private TextField nicknameField;
    private Anchor sourceCodeLink;

    public MainLayout(CurrentUserSignal currentUserSignal,
            UserSessionRegistry userSessionRegistry,
            UserPreferences userPreferences) {
        this.currentUserSignal = currentUserSignal;
        this.userSessionRegistry = userSessionRegistry;
        this.userPreferences = userPreferences;

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

        // Active users display with avatars
        HorizontalLayout activeUsersDisplay = new HorizontalLayout();
        activeUsersDisplay.setSpacing(true);
        activeUsersDisplay.setAlignItems(
                com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        activeUsersDisplay.getStyle().set("margin-left", "auto")
                .set("margin-right", "1em");

        Span activeUsersLabel = new Span(userSessionRegistry.getDisplayNamesSignal()
                .map(displayNames -> displayNames.isEmpty() ? ""
                        : "👥 " + displayNames.size() + " online:"));
        activeUsersLabel.getStyle().set("color",
                "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        com.vaadin.flow.component.html.Div avatarsContainer = new com.vaadin.flow.component.html.Div();
        avatarsContainer.getStyle().set("display", "flex").set("gap", "0.25em");

        com.example.MissingAPI.bindChildren(avatarsContainer,
                com.vaadin.signals.Signal.computed(() -> {
                    var users = userSessionRegistry.getActiveUsersSignal()
                            .value();
                    var displayNames = userSessionRegistry.getDisplayNamesSignal()
                            .value();

                    java.util.List<Avatar> avatars = new java.util.ArrayList<>();
                    for (int i = 0; i < users.size(); i++) {
                        var user = users.get(i).value();
                        String displayName = i < displayNames.size()
                                ? displayNames.get(i)
                                : user.username();

                        Avatar avatar = new Avatar(displayName);
                        avatar.setImage(getProfilePicturePath(user.username()));
                        avatars.add(avatar);
                    }
                    return avatars;
                }));

        activeUsersDisplay.add(activeUsersLabel, avatarsContainer);

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

        // Current user display with avatar
        HorizontalLayout userDisplay = new HorizontalLayout();
        userDisplay.setSpacing(true);
        userDisplay.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        userDisplay.getStyle().set("margin-right", "1em");

        Avatar userAvatar = new Avatar();
        userAvatar.getElement().bindProperty("name",
                currentUserSignal.getUserSignal().map(user -> user.isAuthenticated()
                        ? user.getUsername()
                        : ""));
        userAvatar.getElement().bindProperty("img",
                currentUserSignal.getUserSignal().map(user -> user.isAuthenticated()
                        ? getProfilePicturePath(user.getUsername())
                        : ""));
        userAvatar.bindVisible(currentUserSignal.getUserSignal()
                .map(user -> user.isAuthenticated()));

        Span userName = new Span(currentUserSignal.getUserSignal()
                .map(user -> user.isAuthenticated() ? user.getUsername() : ""));
        userName.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        userDisplay.add(userAvatar, userName);

        // Logout button
        Button logoutButton = new Button("Logout", event -> {
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(
                    VaadinServletRequest.getCurrent().getHttpServletRequest(),
                    null, null);
            getUI().ifPresent(ui -> ui.getPage().setLocation("/login"));
        });
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        addToNavbar(toggle, title, activeUsersDisplay, nicknameField,
                userDisplay, logoutButton);

        // Fixed-position source code link overlay
        Div sourceCodeContainer = new Div();
        sourceCodeContainer.getStyle()
                .set("position", "fixed")
                .set("top", "calc(var(--vaadin-app-layout-navbar-offset-top) + 0.5em)")
                .set("right", "1em")
                .set("z-index", "100")
                .set("pointer-events", "auto");

        Icon codeIcon = VaadinIcon.CODE.create();
        codeIcon.setSize("16px");
        codeIcon.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-right", "0.5em");

        sourceCodeLink = new Anchor("", "View source");
        sourceCodeLink.setTarget("_blank");
        sourceCodeLink.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("background-color", "rgba(255, 255, 255, 0.95)")
                .set("padding", "0.5em 0.75em")
                .set("border-radius", "4px")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("text-decoration", "none")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("transition", "box-shadow 0.2s");

        sourceCodeLink.getElement().addEventListener("mouseenter", e -> {
            sourceCodeLink.getStyle().set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.15)");
        }).addEventData("event.preventDefault");

        sourceCodeLink.getElement().addEventListener("mouseleave", e -> {
            sourceCodeLink.getStyle().set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");
        }).addEventData("event.preventDefault");

        Span linkContent = new Span(codeIcon);
        linkContent.add("View source");
        sourceCodeLink.removeAll();
        sourceCodeLink.add(linkContent);

        sourceCodeContainer.add(sourceCodeLink);
        getElement().appendChild(sourceCodeContainer.getElement());

        // Add auto-menu from @Menu annotations
        SideNav nav = new SideNav();
        MenuConfiguration.getMenuEntries().forEach(entry -> nav
                .addItem(new SideNavItem(entry.title(), entry.path())));
        addToDrawer(nav);

        // Apply session-scoped background color reactively to the whole layout
        getStyle().bind("background-color", userPreferences.backgroundColorSignal());
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

        // Set up Page Visibility API listener to track tab activity
        getElement().executeJs(
                "const updateVisibility = () => {" +
                "  const isVisible = document.visibilityState === 'visible';" +
                "  $0.$server.onVisibilityChange(isVisible);" +
                "};" +
                "document.addEventListener('visibilitychange', updateVisibility);" +
                "updateVisibility();" // Report initial state
        , getElement());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        if (currentUser != null && sessionId != null) {
            userSessionRegistry.unregisterUser(currentUser, sessionId);
        }
    }

    /**
     * Called from JavaScript when the tab visibility changes.
     *
     * @param isVisible true if the tab is visible, false if hidden
     */
    @ClientCallable
    public void onVisibilityChange(boolean isVisible) {
        if (currentUser != null && sessionId != null) {
            userSessionRegistry.updateTabActivity(currentUser, sessionId, isVisible);
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

        // Update source code link for current view
        updateSourceCodeLink(event.getNavigationTarget());
    }

    private void updateSourceCodeLink(Class<?> viewClass) {
        if (sourceCodeLink == null || viewClass == null) {
            return;
        }

        String className = viewClass.getSimpleName();
        String packageName = viewClass.getPackageName();

        // Construct the correct path based on package structure
        String packagePath = packageName.replace(".", "/");
        String githubUrl = "https://github.com/vaadin/signals-cases/tree/main/src/main/java/"
                + packagePath + "/" + className + ".java";
        sourceCodeLink.setHref(githubUrl);

        // Hide link for views that don't have source in the views package
        boolean isViewClass = className.endsWith("View");
        sourceCodeLink.setVisible(isViewClass);
    }

    /**
     * Get the profile picture path for a username.
     * Images are stored in src/main/resources/META-INF/resources/profile-pictures/
     */
    public static String getProfilePicturePath(String username) {
        if (username == null) {
            return "";
        }
        // Map username to profile picture (images are in META-INF/resources/profile-pictures)
        return switch (username.toLowerCase()) {
            case "admin" -> "/profile-pictures/admin.png";
            case "editor" -> "/profile-pictures/editor.png";
            case "viewer" -> "/profile-pictures/viewer.png";
            case "superadmin" -> "/profile-pictures/superadmin.png";
            default -> ""; // No image for unknown users
        };
    }
}
