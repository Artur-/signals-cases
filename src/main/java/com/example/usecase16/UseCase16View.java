package com.example.usecase16;

import com.example.views.MainLayout;

import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.MissingAPI;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.local.ValueSignal;

/**
 * Use Case 16: Search with URL State (Router Integration)
 *
 * Demonstrates deep linking and URL state management: - Query parameters as
 * signals (two-way binding) - Update URL when signal changes - Load signal from
 * URL on navigation - Back button support (browser history) - Shareable URLs
 * with search state
 *
 * Key Patterns: - Router integration with signals - Query parameter binding -
 * URL state synchronization - Deep linking support
 */
@Route(value = "use-case-16", layout = MainLayout.class)
@PageTitle("Use Case 16: URL State Integration")
@Menu(order = 16, title = "UC 16: URL State Integration")
@PermitAll
public class UseCase16View extends VerticalLayout
        implements BeforeEnterObserver {

    public static class Article {
        private final String id;
        private final String title;
        private final String category;
        private final String content;

        public Article(String id, String title, String category,
                String content) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.content = content;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        public String getContent() {
            return content;
        }

        public boolean matches(String query, String category) {
            boolean categoryMatch = category.equals("All")
                    || this.category.equals(category);
            if (query.isEmpty()) {
                return categoryMatch;
            }
            String lowerQuery = query.toLowerCase();
            return categoryMatch && (title.toLowerCase().contains(lowerQuery)
                    || content.toLowerCase().contains(lowerQuery));
        }
    }

    private static final List<Article> ALL_ARTICLES = List.of(
            new Article("1", "Getting Started with Signals", "Tutorial",
                    "Learn the basics of reactive signals..."),
            new Article("2", "Advanced Signal Patterns", "Tutorial",
                    "Deep dive into computed signals and effects..."),
            new Article("3", "Building Reactive UIs", "Guide",
                    "Create dynamic user interfaces with signals..."),
            new Article("4", "Signal API Reference", "Documentation",
                    "Complete API documentation for signals..."),
            new Article("5", "Performance Optimization", "Guide",
                    "Best practices for signal performance..."),
            new Article("6", "Common Pitfalls", "Tutorial",
                    "Avoid these common mistakes when using signals..."),
            new Article("7", "Integration with Binder", "Documentation",
                    "How to use signals with Vaadin Binder..."),
            new Article("8", "Multi-User Signals", "Guide",
                    "Implementing collaborative features..."),
            new Article("9", "Testing Signal-Based Code", "Tutorial",
                    "Unit testing patterns for signals..."),
            new Article("10", "Migration Guide", "Documentation",
                    "Migrating from traditional state to signals..."));

    private final WritableSignal<String> searchQuerySignal = new ValueSignal<>(
            "");
    private final WritableSignal<String> categorySignal = new ValueSignal<>(
            "All");
    private final WritableSignal<List<Article>> filteredArticlesSignal = new ValueSignal<>(
            List.of());

    private boolean isInitializing = true;

    public UseCase16View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 16: Search with URL State Integration");

        Paragraph description = new Paragraph(
                "This use case demonstrates router integration where search filters are synchronized with URL query parameters. "
                        + "Try searching or changing the category - the URL updates automatically. "
                        + "Share the URL or use the back button to see state restoration in action. "
                        + "This enables deep linking: users can bookmark or share specific search results.");

        // Search controls
        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);
        controls.setWidthFull();

        TextField searchField = new TextField("Search");
        searchField.setPlaceholder("Search articles...");
        searchField.setWidth("300px");
        searchField.setClearButtonVisible(true);
        searchField.bindValue(searchQuerySignal);

        Select<String> categorySelect = new Select<>();
        categorySelect.setLabel("Category");
        categorySelect.setItems("All", "Tutorial", "Guide", "Documentation");
        categorySelect.setWidth("200px");
        categorySelect.bindValue(categorySignal);

        controls.add(searchField, categorySelect);

        // Current URL display
        Div urlBox = new Div();
        urlBox.getStyle().set("background-color", "#e3f2fd")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin", "1em 0");

        H3 urlTitle = new H3("Current URL");
        urlTitle.getStyle().set("margin-top", "0");

        // Update URL display based on signals
        Signal<String> currentUrlSignal = Signal.computed(() -> {
            String baseUrl = getBaseUrl();
            String query = searchQuerySignal.value();
            String category = categorySignal.value();

            StringBuilder url = new StringBuilder(baseUrl);
            if (!query.isEmpty() || !category.equals("All")) {
                url.append("?");
                if (!query.isEmpty()) {
                    url.append("q=").append(query);
                }
                if (!category.equals("All")) {
                    if (!query.isEmpty())
                        url.append("&");
                    url.append("category=").append(category);
                }
            }
            return url.toString();
        });

        Paragraph urlDisplay = new Paragraph(currentUrlSignal);
        urlDisplay.getStyle().set("font-family", "monospace")
                .set("word-break", "break-all")
                .set("background-color", "#ffffff").set("padding", "0.5em")
                .set("border-radius", "4px");

        urlBox.add(urlTitle, urlDisplay);

        // Shareable links section
        H3 shareTitle = new H3("Try These Shareable Links");

        HorizontalLayout shareLinks = new HorizontalLayout();
        shareLinks.setSpacing(true);

        Anchor link1 = new Anchor("use-case-16?q=signal", "Search: 'signal'");
        link1.getElement().setAttribute("router-link", "");

        Anchor link2 = new Anchor("use-case-16?category=Tutorial",
                "Category: Tutorial");
        link2.getElement().setAttribute("router-link", "");

        Anchor link3 = new Anchor("use-case-16?q=reactive&category=Guide",
                "Search: 'reactive' in Guide");
        link3.getElement().setAttribute("router-link", "");

        shareLinks.add(link1, link2, link3);

        // Results
        Signal<String> resultsTitleSignal = filteredArticlesSignal
                .map(articles -> articles.size() + " article"
                        + (articles.size() == 1 ? "" : "s") + " found");
        H3 resultsTitle = new H3(resultsTitleSignal);

        Div resultsContainer = new Div();
        resultsContainer.getStyle().set("display", "flex")
                .set("flex-direction", "column").set("gap", "0.5em")
                .set("margin-top", "1em");

        MissingAPI
                .bindComponentChildren(resultsContainer,
                        filteredArticlesSignal,
                        article -> {
                            Div card = new Div();
                            card.getStyle().set("background-color", "#ffffff")
                                    .set("border",
                                            "1px solid var(--lumo-contrast-20pct)")
                                    .set("border-radius", "4px")
                                    .set("padding", "1em");

                            Div headerDiv = new Div();
                            headerDiv.getStyle().set("display", "flex")
                                    .set("justify-content", "space-between")
                                    .set("align-items", "center")
                                    .set("margin-bottom", "0.5em");

                            Div titleDiv = new Div(article.getTitle());
                            titleDiv.getStyle().set("font-weight", "bold")
                                    .set("font-size", "1.1em");

                            Div categoryBadge = new Div(article.getCategory());
                            categoryBadge.getStyle()
                                    .set("background-color", "#e0e0e0")
                                    .set("padding", "0.25em 0.5em")
                                    .set("border-radius", "4px")
                                    .set("font-size", "0.85em");

                            headerDiv.add(titleDiv, categoryBadge);

                            Div contentDiv = new Div(article.getContent());
                            contentDiv.getStyle()
                                    .set("color",
                                            "var(--lumo-secondary-text-color)")
                                    .set("font-size", "0.9em");

                            card.add(headerDiv, contentDiv);
                            return card;
                        });

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "💡 Router integration with signals enables deep linking and shareable URLs. "
                        + "This pattern is essential for SEO, bookmarking, and sharing specific app states. "
                        + "The URL automatically updates as you interact with the UI, and the browser back button works naturally. "
                        + "In production, this would integrate with Vaadin Router's @QueryParameters or similar API. "
                        + "Try clicking the shareable links above to see the state restored from URL."));

        add(title, description, controls, urlBox, shareTitle, shareLinks,
                resultsTitle, resultsContainer, infoBox);

        // Subscribe to signals to update URL and filter results
        setupSignalSubscriptions();
    }

    private void setupSignalSubscriptions() {
        // When signals change, update URL (except during initialization)
        com.vaadin.flow.component.ComponentEffect.effect(this, () -> {
            // Access signals to track them
            searchQuerySignal.value();
            categorySignal.value();

            if (!isInitializing) {
                updateUrl();
            }
            updateFilteredResults();
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Extract query parameters from URL
        QueryParameters queryParameters = event.getLocation()
                .getQueryParameters();
        Map<String, List<String>> params = queryParameters.getParameters();

        isInitializing = true;

        // Load search query from URL
        if (params.containsKey("q")) {
            String query = params.get("q").get(0);
            searchQuerySignal.value(query);
        } else {
            searchQuerySignal.value("");
        }

        // Load category from URL
        if (params.containsKey("category")) {
            String category = params.get("category").get(0);
            categorySignal.value(category);
        } else {
            categorySignal.value("All");
        }

        isInitializing = false;

        // Initial filter
        updateFilteredResults();
    }

    private void updateUrl() {
        String query = searchQuerySignal.value();
        String category = categorySignal.value();

        // Build query parameters
        StringBuilder urlParams = new StringBuilder();
        if (!query.isEmpty()) {
            urlParams.append("q=").append(query);
        }
        if (!category.equals("All")) {
            if (urlParams.length() > 0)
                urlParams.append("&");
            urlParams.append("category=").append(category);
        }

        // Update browser URL without triggering navigation
        String url = "use-case-16"
                + (urlParams.length() > 0 ? "?" + urlParams : "");
        UI.getCurrent().getPage().getHistory().replaceState(null, url);
    }

    private void updateFilteredResults() {
        String query = searchQuerySignal.value();
        String category = categorySignal.value();

        List<Article> filtered = ALL_ARTICLES.stream()
                .filter(article -> article.matches(query, category))
                .collect(Collectors.toList());

        filteredArticlesSignal.value(filtered);
    }

    private String getBaseUrl() {
        VaadinServletRequest request = (VaadinServletRequest) com.vaadin.flow.server.VaadinRequest
                .getCurrent();
        if (request != null) {
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();

            StringBuilder url = new StringBuilder();
            url.append(scheme).append("://").append(serverName);
            if ((scheme.equals("http") && serverPort != 80)
                    || (scheme.equals("https") && serverPort != 443)) {
                url.append(":").append(serverPort);
            }
            url.append(contextPath).append("/use-case-16");
            return url.toString();
        }
        return "http://localhost:8080/use-case-16";
    }
}
