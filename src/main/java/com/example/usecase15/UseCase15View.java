package com.example.usecase15;

import com.example.views.MainLayout;

import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.example.MissingAPI;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Use Case 15: Debounced Search
 *
 * Demonstrates search-as-you-type with debouncing: - TextField with immediate
 * updates to signal - Debounced signal (300ms delay) - Search only fires after
 * user stops typing - Cancel in-flight requests on new input - Loading
 * indicator during search - Highlight matching text in results
 *
 * Key Patterns: - Debouncing for performance - Async search with cancellation -
 * Loading states for search - Real-time search results
 */
@Route(value = "use-case-15", layout = MainLayout.class)
@PageTitle("Use Case 15: Debounced Search")
@Menu(order = 15, title = "UC 15: Debounced Search")
@PermitAll
public class UseCase15View extends VerticalLayout {

    public record Product(String id, String name, String category,
            double price) {
        public boolean matches(String query) {
            String lowerQuery = query.toLowerCase();
            return name.toLowerCase().contains(lowerQuery)
                    || category.toLowerCase().contains(lowerQuery);
        }
    }

    // Sample product database
    private static final List<Product> ALL_PRODUCTS = List.of(
            new Product("1", "Laptop Pro 15", "Electronics", 1299.99),
            new Product("2", "Wireless Mouse", "Electronics", 29.99),
            new Product("3", "Mechanical Keyboard", "Electronics", 89.99),
            new Product("4", "Office Chair", "Furniture", 249.99),
            new Product("5", "Standing Desk", "Furniture", 499.99),
            new Product("6", "Coffee Mug", "Kitchen", 12.99),
            new Product("7", "Water Bottle", "Kitchen", 19.99),
            new Product("8", "Notebook Set", "Stationery", 15.99),
            new Product("9", "Pen Collection", "Stationery", 24.99),
            new Product("10", "Desk Lamp", "Furniture", 39.99),
            new Product("11", "USB-C Hub", "Electronics", 49.99),
            new Product("12", "Headphones", "Electronics", 149.99),
            new Product("13", "Monitor 27\"", "Electronics", 399.99),
            new Product("14", "Webcam HD", "Electronics", 79.99),
            new Product("15", "Bookshelf", "Furniture", 129.99));

    private final WritableSignal<String> searchQuerySignal = new ValueSignal<>(
            "");
    private final WritableSignal<String> debouncedQuerySignal = new ValueSignal<>(
            "");
    private final WritableSignal<Boolean> isSearchingSignal = new ValueSignal<>(
            false);
    private final ListSignal<Product> searchResultsSignal = new ListSignal<>(
            Product.class);
    private final WritableSignal<Integer> searchCountSignal = new ValueSignal<>(
            0);

    private final AtomicReference<CompletableFuture<Void>> currentSearch = new AtomicReference<>();

    public UseCase15View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 15: Debounced Search");

        Paragraph description = new Paragraph(
                "This use case demonstrates search-as-you-type with debouncing. "
                        + "As you type in the search field, the actual search is delayed by 300ms. "
                        + "This prevents excessive server calls while typing and only searches after you pause. "
                        + "The search counter shows how many searches were actually performed.");

        // Search field
        TextField searchField = new TextField("Search Products");
        searchField.setPlaceholder("Type to search...");
        searchField.setWidth("400px");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(
                ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);

        searchField.bindValue(searchQuerySignal);
        searchField.addValueChangeListener(event -> {
            String query = event.getValue();
            debouncedQuerySignal.value(query);
            performSearch(query);
        });

        // Search stats
        Div statsBox = new Div();
        statsBox.getStyle().set("background-color", "#f5f5f5")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin", "1em 0").set("display", "flex").set("gap", "2em")
                .set("align-items", "center");

        Div instantQueryDiv = new Div();
        Span instantLabel = new Span("Instant value: ");
        instantLabel.getStyle().set("color",
                "var(--lumo-secondary-text-color)");
        Span instantValue = new Span(searchQuerySignal
                .map(q -> q.isEmpty() ? "(empty)" : "\"" + q + "\""));
        instantValue.getStyle().set("font-family", "monospace")
                .set("font-weight", "bold");
        instantQueryDiv.add(instantLabel, instantValue);

        Div debouncedQueryDiv = new Div();
        Span debouncedLabel = new Span("Debounced value (300ms): ");
        debouncedLabel.getStyle().set("color",
                "var(--lumo-secondary-text-color)");
        Span debouncedValue = new Span(debouncedQuerySignal
                .map(q -> q.isEmpty() ? "(empty)" : "\"" + q + "\""));
        debouncedValue.getStyle().set("font-family", "monospace")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");
        debouncedQueryDiv.add(debouncedLabel, debouncedValue);

        Div searchCountDiv = new Div();
        Span countLabel = new Span("Searches performed: ");
        countLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        Span countValue = new Span(searchCountSignal.map(String::valueOf));
        countValue.getStyle().set("font-weight", "bold").set("color",
                "var(--lumo-success-color)");
        searchCountDiv.add(countLabel, countValue);

        statsBox.add(instantQueryDiv, debouncedQueryDiv, searchCountDiv);

        // Search status
        Div statusBox = new Div();
        statusBox.getStyle().set("min-height", "30px").set("display", "flex")
                .set("align-items", "center").set("gap", "0.5em");

        Icon searchingIcon = new Icon(VaadinIcon.SPINNER);
        searchingIcon.getStyle().set("animation", "spin 1s linear infinite");
        searchingIcon.bindVisible(isSearchingSignal);

        Span statusText = new Span(isSearchingSignal
                .map(searching -> searching ? "Searching..." : ""));
        statusText.getStyle().set("color", "var(--lumo-primary-color)");

        statusBox.add(searchingIcon, statusText);

        // Results
        Signal<String> resultsTitleSignal = searchResultsSignal.map(results -> {
            if (results.isEmpty() && !debouncedQuerySignal.value().isEmpty()) {
                return "No results found";
            } else if (!results.isEmpty()) {
                return results.size() + " result"
                        + (results.size() == 1 ? "" : "s");
            }
            return "Type to search";
        });
        H3 resultsTitle = new H3(resultsTitleSignal);

        Div resultsContainer = new Div();
        resultsContainer.getStyle().set("display", "flex")
                .set("flex-direction", "column").set("gap", "0.5em")
                .set("margin-top", "1em");

        MissingAPI
                .bindComponentChildren(resultsContainer,
                        searchResultsSignal.map(prodSignals -> prodSignals
                                .stream().map(ValueSignal::value).toList()),
                        product -> {
                            Div card = new Div();
                            card.getStyle().set("background-color", "#ffffff")
                                    .set("border",
                                            "1px solid var(--lumo-contrast-20pct)")
                                    .set("border-radius", "4px")
                                    .set("padding", "1em")
                                    .set("display", "flex")
                                    .set("justify-content", "space-between")
                                    .set("align-items", "center");

                            Div leftSide = new Div();
                            Div nameDiv = new Div();
                            nameDiv.getStyle().set("font-weight", "bold");

                            // Highlight matching text
                            String query = debouncedQuerySignal.value();
                            nameDiv.getElement().setProperty("innerHTML",
                                    highlightMatch(product.name(), query));

                            Div categoryDiv = new Div(product.category());
                            categoryDiv.getStyle().set("font-size", "0.9em")
                                    .set("color",
                                            "var(--lumo-secondary-text-color)");

                            leftSide.add(nameDiv, categoryDiv);

                            Div priceDiv = new Div("$"
                                    + String.format("%.2f", product.price()));
                            priceDiv.getStyle().set("font-weight", "bold")
                                    .set("color", "var(--lumo-primary-color)");

                            card.add(leftSide, priceDiv);
                            return card;
                        });

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "💡 Debouncing is critical for performance in production applications. "
                        + "Without debouncing, typing 'laptop' would trigger 6 searches (one per character). "
                        + "With debouncing, it triggers just 1 search after you stop typing. "
                        + "The pattern also demonstrates cancelling in-flight requests when a new search starts. "
                        + "Watch the 'Searches performed' counter to see the debouncing in action."));

        // Add CSS for spinner animation
        getElement().executeJs("const style = document.createElement('style');"
                + "style.textContent = '@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }';"
                + "document.head.appendChild(style);");

        add(title, description, searchField, statsBox, statusBox, resultsTitle,
                resultsContainer, infoBox);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Cancel any in-flight search
        CompletableFuture<Void> search = currentSearch.get();
        if (search != null) {
            search.cancel(true);
        }
    }

    private void performSearch(String query) {
        // Cancel previous search if still running
        CompletableFuture<Void> previousSearch = currentSearch.get();
        if (previousSearch != null && !previousSearch.isDone()) {
            previousSearch.cancel(true);
        }

        if (query.isEmpty()) {
            searchResultsSignal.clear();
            return;
        }

        // Set searching state
        isSearchingSignal.value(true);
        searchCountSignal.value(searchCountSignal.value() + 1);

        // Simulate async search with delay
        CompletableFuture<Void> searchFuture = CompletableFuture
                .runAsync(() -> {
                    try {
                        Thread.sleep(500); // Simulate network delay
                    } catch (InterruptedException e) {
                        // Search cancelled
                        return;
                    }

                    // Filter products
                    List<Product> results = ALL_PRODUCTS.stream()
                            .filter(p -> p.matches(query))
                            .collect(Collectors.toList());

                    getUI().ifPresent(ui -> ui.access(() -> {
                        searchResultsSignal.clear();
                        results.forEach(searchResultsSignal::insertLast);
                        isSearchingSignal.value(false);
                    }));
                });

        currentSearch.set(searchFuture);
    }

    private String highlightMatch(String text, String query) {
        if (query.isEmpty()) {
            return escapeHtml(text);
        }

        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();
        int index = lowerText.indexOf(lowerQuery);

        if (index == -1) {
            return escapeHtml(text);
        }

        String before = text.substring(0, index);
        String match = text.substring(index, index + query.length());
        String after = text.substring(index + query.length());

        return escapeHtml(before)
                + "<mark style='background-color: #ffeb3b; padding: 2px 4px; border-radius: 2px;'>"
                + escapeHtml(match) + "</mark>" + escapeHtml(after);
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
