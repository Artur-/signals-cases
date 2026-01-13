package com.example.views;

import com.example.MissingAPI;
import com.example.security.CurrentUserSignal;
import com.example.security.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import jakarta.annotation.security.PermitAll;

/**
 * Use Case 24: Application-Wide Current User Signal
 *
 * Demonstrates using a shared signal for current user information that can be:
 * - Used in multiple views and the main layout
 * - Reactive to authentication changes (login, logout, impersonation)
 * - Source of derived signals for role-based UI customization
 *
 * Key Patterns:
 * - Application-scoped signal (Spring @Component)
 * - Signal shared across multiple components
 * - Integration with Spring Security AuthenticationContext
 * - Reactive to security context changes
 */
@Route(value = "use-case-24", layout = MainLayout.class)
@PageTitle("Use Case 24: Current User Signal")
@Menu(order = 40, title = "UC 24: Current User Signal")
@PermitAll
public class UseCase24View extends VerticalLayout {

    public UseCase24View(CurrentUserSignal currentUserSignal, SecurityService securityService) {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 24: Application-Wide Current User Signal");

        Paragraph description = new Paragraph(
            "This use case demonstrates an application-scoped signal holding current user information. " +
            "The signal is shared across views and the main layout, and reacts to authentication changes " +
            "including login, logout, and impersonation (via Vaadin Copilot)."
        );

        // Current user info display
        Div userInfoBox = new Div();
        userInfoBox.getStyle()
            .set("background-color", "#e8f5e9")
            .set("padding", "1.5em")
            .set("border-radius", "8px")
            .set("margin", "1em 0");

        H3 userInfoTitle = new H3("Current User from Signal");
        userInfoTitle.getStyle().set("margin-top", "0");

        Span usernameDisplay = new Span();
        usernameDisplay.getStyle()
            .set("font-size", "1.2em")
            .set("font-weight", "bold")
            .set("display", "block")
            .set("margin-bottom", "0.5em");
        MissingAPI.bindText(usernameDisplay,
            currentUserSignal.getUserSignal().map(user ->
                user.isAuthenticated() ? "👤 " + user.getUsername() : "👤 Not logged in"
            )
        );

        Span rolesDisplay = new Span();
        rolesDisplay.getStyle()
            .set("display", "block")
            .set("color", "var(--lumo-secondary-text-color)");
        MissingAPI.bindText(rolesDisplay,
            currentUserSignal.getUserSignal().map(user -> {
                if (!user.isAuthenticated()) {
                    return "Anonymous user";
                }
                return "Roles: " + String.join(", ", user.getRoles());
            })
        );

        Button refreshButton = new Button("Refresh Signal", event -> {
            currentUserSignal.refresh();
        });
        refreshButton.addThemeName("small");

        userInfoBox.add(userInfoTitle, usernameDisplay, rolesDisplay, refreshButton);

        // Role-based content sections
        H3 roleBasedTitle = new H3("Role-Based UI Customization");

        // Viewer content
        Div viewerSection = new Div();
        viewerSection.getStyle()
            .set("padding", "1em")
            .set("background-color", "#e3f2fd")
            .set("border-radius", "4px")
            .set("margin-bottom", "0.5em");
        viewerSection.add(new Paragraph("📖 Viewer Content - You can view dashboards"));
        Signal<Boolean> hasViewerRole = currentUserSignal.getUserSignal()
            .map(user -> user.hasAnyRole("VIEWER", "EDITOR", "ADMIN", "SUPER_ADMIN"));
        viewerSection.bindVisible(hasViewerRole);

        // Editor content
        Div editorSection = new Div();
        editorSection.getStyle()
            .set("padding", "1em")
            .set("background-color", "#fff3e0")
            .set("border-radius", "4px")
            .set("margin-bottom", "0.5em");
        editorSection.add(new Paragraph("✏️ Editor Content - You can edit content"));
        Signal<Boolean> hasEditorRole = currentUserSignal.getUserSignal()
            .map(user -> user.hasAnyRole("EDITOR", "ADMIN", "SUPER_ADMIN"));
        editorSection.bindVisible(hasEditorRole);

        // Admin content
        Div adminSection = new Div();
        adminSection.getStyle()
            .set("padding", "1em")
            .set("background-color", "#fce4ec")
            .set("border-radius", "4px")
            .set("margin-bottom", "0.5em");
        adminSection.add(new Paragraph("⚙️ Admin Content - You can manage users and settings"));
        Signal<Boolean> hasAdminRole = currentUserSignal.getUserSignal()
            .map(user -> user.hasAnyRole("ADMIN", "SUPER_ADMIN"));
        adminSection.bindVisible(hasAdminRole);

        // Super Admin content
        Div superAdminSection = new Div();
        superAdminSection.getStyle()
            .set("padding", "1em")
            .set("background-color", "#f3e5f5")
            .set("border-radius", "4px")
            .set("margin-bottom", "0.5em");
        superAdminSection.add(new Paragraph("🔒 Super Admin Content - Full system access"));
        Signal<Boolean> hasSuperAdminRole = currentUserSignal.getUserSignal()
            .map(user -> user.hasRole("SUPER_ADMIN"));
        superAdminSection.bindVisible(hasSuperAdminRole);

        // Actions
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button logoutButton = new Button("Logout", event -> {
            securityService.logout();
            currentUserSignal.refresh();
        });
        logoutButton.addThemeName("error");

        actions.add(logoutButton);

        // Info about signal sharing
        Div infoBox = new Div();
        infoBox.getStyle()
            .set("background-color", "#e0f7fa")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-top", "1em")
            .set("font-style", "italic");
        infoBox.add(new Paragraph(
            "💡 This signal is application-scoped and can be injected into any view or the main layout. " +
            "Try using Vaadin Copilot's impersonation feature to switch roles and see the UI update reactively. " +
            "The MainLayout also uses this signal to display your username in the header."
        ));

        add(
            title,
            description,
            userInfoBox,
            roleBasedTitle,
            viewerSection,
            editorSection,
            adminSection,
            superAdminSection,
            actions,
            infoBox
        );
    }
}
