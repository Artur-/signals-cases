package com.example.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.menu.MenuConfiguration;
import jakarta.annotation.security.PermitAll;

@PageTitle("Signal API Use Cases")
@PermitAll
public class MainLayout extends AppLayout {

    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Signal API Use Cases");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        addToNavbar(toggle, title);

        // Add auto-menu from @Menu annotations
        SideNav nav = new SideNav();
        MenuConfiguration.getMenuEntries().forEach(entry ->
            nav.addItem(new SideNavItem(entry.title(), entry.path()))
        );
        addToDrawer(nav);
    }
}
