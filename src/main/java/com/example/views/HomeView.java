package com.example.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Signal API Use Cases - Home")
@Menu(order = 0, title = "Home")
@PermitAll
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        // Header
        H1 header = new H1("Vaadin Signal API Use Cases");

        // Introduction
        Paragraph intro = new Paragraph(
            "This application demonstrates 17 comprehensive use cases for the proposed Vaadin Signal API. " +
            "Each use case validates different aspects of the reactive programming model, from basic " +
            "one-way bindings to complex form validation and dynamic component rendering."
        );

        // Signal API Overview
        H2 overviewTitle = new H2("What are Signals?");
        Paragraph overview = new Paragraph(
            "Signals are reactive state holders that automatically notify dependent computations and UI bindings " +
            "when their values change. The framework manages the dependency graph and lifecycle automatically, " +
            "eliminating manual listener management and cleanup code."
        );

        // Purpose
        H2 purposeTitle = new H2("Purpose of These Use Cases");
        Paragraph purpose = new Paragraph(
            "These use cases serve to validate the signal API design, identify potential gaps, provide practical " +
            "examples for developers, and guide implementation priorities for the Vaadin team."
        );

        // Statistics
        H2 statsTitle = new H2("Collection Overview");
        VerticalLayout stats = new VerticalLayout();
        stats.setSpacing(false);
        stats.setPadding(false);
        stats.add(
            createStat("17", "Total use cases covering the full API surface"),
            createStat("6", "Categories from basic bindings to advanced features"),
            createStat("100%", "Coverage of proposed signal API features")
        );

        // Categories
        H2 categoriesTitle = new H2("Use Case Categories");
        VerticalLayout categories = new VerticalLayout();
        categories.setSpacing(false);
        categories.setPadding(false);
        categories.add(
            createCategory("Category 1: Basic Signal Bindings", "Use Cases 1-3",
                "One-way and two-way bindings, component state synchronization"),
            createCategory("Category 2: Computed Signals & Derivations", "Use Cases 4-5",
                "Multi-signal computations and transformation pipelines"),
            createCategory("Category 3: Conditional Rendering", "Use Cases 6-8",
                "Dynamic subforms, progressive disclosure, permission-based UI"),
            createCategory("Category 4: List & Collection Rendering", "Use Cases 9-10",
                "Filtered data grids and dynamic lists with real-time updates"),
            createCategory("Category 5: Cross-Component Interactions", "Use Cases 11-13",
                "Cascading dropdowns, shopping carts, master-detail patterns"),
            createCategory("Category 6: Complex Forms & Advanced", "Use Cases 14-17",
                "Multi-step wizards, Binder integration, ComponentToggle, Grid providers")
        );

        // Documentation Link
        H2 docsTitle = new H2("Documentation");
        Paragraph docs = new Paragraph();
        docs.add(new Span("For detailed documentation, see "));
        docs.add(new Anchor("https://github.com/vaadin/platform/issues/8366",
            "vaadin/platform#8366 - Signal API Proposal"));
        docs.add(new Span(" and the "));
        docs.add(new Anchor("https://github.com/Artur-/signals-cases/blob/main/signal-use-cases.md",
            "signal-use-cases.md"));
        docs.add(new Span(" file in this project."));

        // Getting Started
        H2 gettingStartedTitle = new H2("Getting Started");
        Paragraph gettingStarted = new Paragraph(
            "Use the navigation menu on the left to explore each use case. Each view demonstrates a specific " +
            "pattern or feature of the signal API. The code shows how the proposed API would be used in " +
            "real-world scenarios."
        );

        add(
            header,
            intro,
            new Hr(),
            overviewTitle,
            overview,
            purposeTitle,
            purpose,
            new Hr(),
            statsTitle,
            stats,
            new Hr(),
            categoriesTitle,
            categories,
            new Hr(),
            docsTitle,
            docs,
            new Hr(),
            gettingStartedTitle,
            gettingStarted
        );
    }

    private Span createStat(String number, String description) {
        Span stat = new Span();
        Span num = new Span(number);
        num.getStyle()
            .set("font-size", "var(--lumo-font-size-xxl)")
            .set("font-weight", "bold")
            .set("color", "var(--lumo-primary-color)")
            .set("margin-right", "var(--lumo-space-s)");
        Span desc = new Span(description);
        desc.getStyle().set("color", "var(--lumo-secondary-text-color)");
        stat.add(num, desc);
        stat.getStyle().set("display", "flex").set("align-items", "center");
        return stat;
    }

    private VerticalLayout createCategory(String title, String range, String description) {
        VerticalLayout category = new VerticalLayout();
        category.setSpacing(false);
        category.setPadding(false);
        category.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "600");

        Span rangeSpan = new Span(range);
        rangeSpan.getStyle()
            .set("font-size", "var(--lumo-font-size-s)")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin-left", "var(--lumo-space-s)");

        Paragraph descPara = new Paragraph(description);
        descPara.getStyle()
            .set("margin-top", "var(--lumo-space-xs)")
            .set("margin-bottom", "0")
            .set("color", "var(--lumo-secondary-text-color)");

        category.add(
            new Span(titleSpan, rangeSpan),
            descPara
        );

        return category;
    }
}
