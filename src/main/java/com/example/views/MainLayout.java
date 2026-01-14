package com.example.views;

import com.example.MissingAPI;
import com.example.security.CurrentUserSignal;
import com.example.signals.UserInfo;
import com.example.signals.UserSessionRegistry;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.menu.MenuConfiguration;
import jakarta.annotation.security.PermitAll;

import java.util.stream.Collectors;

@PageTitle("Signal API Use Cases")
@PermitAll
public class MainLayout extends AppLayout {

    public MainLayout(CurrentUserSignal currentUserSignal,
                      UserSessionRegistry userSessionRegistry) {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Signal API Use Cases");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        // Active users display using signal
        Span activeUsersDisplay = new Span();
        activeUsersDisplay.getStyle()
                .set("margin-left", "auto")
                .set("margin-right", "1em")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");
        MissingAPI.bindText(activeUsersDisplay,
            userSessionRegistry.getActiveUsersSignal().map(userSignals -> {
                if (userSignals.isEmpty()) {
                    return "";
                }
                String usernames = userSignals.stream()
                    .map(userSignal -> userSignal.value().username())
                    .collect(Collectors.joining(", "));
                return "👥 " + userSignals.size() + " online: " + usernames;
            })
        );

        // Current user display using signal
        Span userDisplay = new Span();
        userDisplay.getStyle()
                .set("margin-right", "1em")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");
        MissingAPI.bindText(userDisplay,
            currentUserSignal.getUserSignal().map(user ->
                user.isAuthenticated() ? "👤 " + user.getUsername() : ""
            )
        );

        addToNavbar(toggle, title, activeUsersDisplay, userDisplay);

        // Add auto-menu from @Menu annotations
        SideNav nav = new SideNav();
        MenuConfiguration.getMenuEntries().forEach(entry ->
            nav.addItem(new SideNavItem(entry.title(), entry.path()))
        );
        addToDrawer(nav);
    }
}
