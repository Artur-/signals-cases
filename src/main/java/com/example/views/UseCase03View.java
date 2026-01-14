package com.example.views;

import com.example.MissingAPI;
import com.example.security.SecurityService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;
import jakarta.annotation.security.PermitAll;

import java.util.Set;
import java.util.function.Function;

@Route(value = "use-case-08", layout = MainLayout.class)
@PageTitle("Use Case 3: Permission-Based Component Visibility")
@Menu(order = 3, title = "UC 3: Permission-Based UI")
@PermitAll
public class UseCase03View extends VerticalLayout {

    enum Permission { VIEW_DASHBOARD, EDIT_CONTENT, DELETE_CONTENT, MANAGE_USERS, VIEW_LOGS, SYSTEM_SETTINGS }

    private final SecurityService securityService;

    public UseCase03View(SecurityService securityService) {
        this.securityService = securityService;

        // Get current user info from Spring Security
        String username = securityService.getUsername();
        Set<String> roles = securityService.getRoles();

        // Create signal for user permissions based on actual Spring Security roles
        WritableSignal<Set<Permission>> permissionsSignal = new ValueSignal<Set<Permission>>(
            getPermissionsForRoles(roles)
        );

        // User info display
        Div userInfoBox = new Div();
        userInfoBox.getStyle()
            .set("background-color", "#e8f5e9")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-bottom", "1em");

        H3 userInfoTitle = new H3("Current User Information");
        userInfoTitle.getStyle().set("margin-top", "0");

        Paragraph userInfo = new Paragraph();
        userInfo.getStyle()
            .set("font-family", "monospace")
            .set("white-space", "pre-line");
        userInfo.setText(String.format(
            "Username: %s\nRoles: %s\nPermissions: %d granted",
            username,
            String.join(", ", roles),
            permissionsSignal.value().size()
        ));

        Button logoutButton = new Button("Logout", event -> securityService.logout());
        logoutButton.addThemeName("error");
        logoutButton.addThemeName("small");

        userInfoBox.add(userInfoTitle, userInfo, logoutButton);

        // Helper function to create permission-based visibility signal
        Function<Permission, Signal<Boolean>> hasPermission = (Permission permission) ->
            permissionsSignal.map(perms -> perms.contains(permission));

        // Dashboard section
        Div dashboardSection = new Div();
        dashboardSection.add(new H3("Dashboard"), new Div("Dashboard content here..."));
        dashboardSection.bindVisible(hasPermission.apply(Permission.VIEW_DASHBOARD));

        // Content editing buttons
        HorizontalLayout editButtons = new HorizontalLayout();
        Button editButton = new Button("Edit Content");
        editButton.bindVisible(hasPermission.apply(Permission.EDIT_CONTENT));

        Button deleteButton = new Button("Delete Content");
        deleteButton.bindVisible(hasPermission.apply(Permission.DELETE_CONTENT));
        deleteButton.addThemeName("error");

        editButtons.add(editButton, deleteButton);

        // User management section
        Div userManagementSection = new Div();
        userManagementSection.add(
            new H3("User Management"),
            new Button("Add User"),
            new Button("Remove User"),
            new Button("Edit Permissions")
        );
        userManagementSection.bindVisible(hasPermission.apply(Permission.MANAGE_USERS));

        // System logs section
        Div logsSection = new Div();
        logsSection.add(new H3("System Logs"), new Div("Log entries..."));
        logsSection.bindVisible(hasPermission.apply(Permission.VIEW_LOGS));

        // System settings section
        Div settingsSection = new Div();
        settingsSection.add(
            new H3("System Settings"),
            new Button("Configure System"),
            new Button("Backup Database"),
            new Button("Manage Integrations")
        );
        settingsSection.bindVisible(hasPermission.apply(Permission.SYSTEM_SETTINGS));

        // Status indicator showing current permissions
        Div permissionsDisplay = new Div();
        permissionsDisplay.getStyle()
            .set("background-color", "#fff3e0")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-top", "1em");

        Span permissionsTitle = new Span("Visible Sections Based On Your Permissions:");
        permissionsTitle.getStyle()
            .set("font-weight", "bold")
            .set("display", "block")
            .set("margin-bottom", "0.5em");

        Span permissionsList = new Span();
        MissingAPI.bindText(permissionsList, permissionsSignal.map(perms -> {
            if (perms == null || perms.isEmpty()) {
                return "None";
            }
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object p : perms) {
                if (!first) sb.append(", ");
                sb.append(p.toString());
                first = false;
            }
            return sb.toString();
        }));

        permissionsDisplay.add(permissionsTitle, permissionsList);

        // Info about impersonation
        Div impersonationHint = new Div();
        impersonationHint.getStyle()
            .set("background-color", "#e3f2fd")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-top", "1em")
            .set("font-style", "italic");
        impersonationHint.setText(
            "💡 Tip: Use Vaadin Copilot's impersonation feature to test different user roles without logging out"
        );

        add(userInfoBox, permissionsDisplay, impersonationHint,
            dashboardSection, editButtons, userManagementSection, logsSection, settingsSection);
    }

    private Set<Permission> getPermissionsForRoles(Set<String> roles) {
        // Map Spring Security roles to application permissions
        Set<Permission> permissions = new java.util.HashSet<>();

        if (roles.contains("VIEWER")) {
            permissions.add(Permission.VIEW_DASHBOARD);
        }
        if (roles.contains("EDITOR")) {
            permissions.add(Permission.VIEW_DASHBOARD);
            permissions.add(Permission.EDIT_CONTENT);
        }
        if (roles.contains("ADMIN")) {
            permissions.add(Permission.VIEW_DASHBOARD);
            permissions.add(Permission.EDIT_CONTENT);
            permissions.add(Permission.DELETE_CONTENT);
            permissions.add(Permission.MANAGE_USERS);
        }
        if (roles.contains("SUPER_ADMIN")) {
            permissions.addAll(Set.of(Permission.values()));
        }

        return permissions;
    }
}
