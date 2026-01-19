package com.example.views;

import jakarta.annotation.security.PermitAll;

import com.example.MissingAPI;
import com.example.service.DataLoadingService;
import com.example.views.UseCase14View.LoadingState;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Use Case 19: Parallel Data Loading with Individual Spinners
 *
 * Demonstrates parallel async loading where each item has its own loading state and spinner.
 * Shows how to manage multiple independent async operations with Signals.
 *
 * Key Patterns:
 * - ListSignal<ValueSignal<DataItem>> for per-item state management
 * - Spring @Async for true parallel execution
 * - Individual loading spinners (ProgressBar indeterminate)
 * - Per-item error handling with retry
 * - CSS Grid for responsive layout
 * - Vaadin Card components with conditional rendering
 */
@Route(value = "use-case-19", layout = MainLayout.class)
@PageTitle("Use Case 19: Parallel Loading")
@Menu(order = 19, title = "UC 19: Parallel Loading")
@PermitAll
public class UseCase19View extends VerticalLayout {

    /**
     * Represents a data item with loading state
     */
    record DataItem(
        String id,
        String name,
        LoadingState.State state,
        String data,
        String error,
        int simulatedDelayMs
    ) {}

    private final DataLoadingService dataLoadingService;
    private final ListSignal<DataItem> itemsSignal = new ListSignal<>(DataItem.class);
    private final WritableSignal<Boolean> simulateErrorsSignal = new ValueSignal<>(false);

    public UseCase19View(DataLoadingService dataLoadingService) {
        this.dataLoadingService = dataLoadingService;
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 19: Parallel Data Loading with Individual Spinners");

        Paragraph description = new Paragraph(
            "This use case demonstrates parallel async loading where each item displays its own loading spinner " +
            "until completion. Click 'Load All Items' to trigger parallel loading of 6 data items with varied " +
            "durations (1-4 seconds). Each item completes independently and displays success/error states. " +
            "Enable 'Simulate Random Errors' to test error handling and individual retry buttons."
        );

        // Initialize data items
        initializeDataItems();

        // Controls
        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);

        Button loadButton = new Button("Load All Items", event -> loadAllItems());
        loadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Checkbox errorCheckbox = new Checkbox("Simulate Random Errors");
        errorCheckbox.bindValue(simulateErrorsSignal);

        controls.add(loadButton, errorCheckbox);

        // Items container with flexbox
        Div itemsContainer = new Div();
        itemsContainer.getStyle()
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("gap", "1em")
            .set("margin", "1em 0");

        // Bind cards to items signal
        MissingAPI.bindChildren(itemsContainer,
            itemsSignal.map(itemSignals -> itemSignals.stream()
                .map(this::createDataItemCard)
                .toList()));

        add(title, description, controls, itemsContainer);
    }

    /**
     * Initialize the 6 data items with different delays
     */
    private void initializeDataItems() {
        itemsSignal.insertLast(new DataItem("1", "Dashboard Metrics", LoadingState.State.IDLE, null, null, 1500));
        itemsSignal.insertLast(new DataItem("2", "User Statistics", LoadingState.State.IDLE, null, null, 3000));
        itemsSignal.insertLast(new DataItem("3", "Sales Report", LoadingState.State.IDLE, null, null, 2000));
        itemsSignal.insertLast(new DataItem("4", "Inventory Status", LoadingState.State.IDLE, null, null, 2500));
        itemsSignal.insertLast(new DataItem("5", "Performance Data", LoadingState.State.IDLE, null, null, 1000));
        itemsSignal.insertLast(new DataItem("6", "Analytics Summary", LoadingState.State.IDLE, null, null, 3500));
    }

    /**
     * Load all items in parallel
     */
    private void loadAllItems() {
        itemsSignal.value().forEach(itemSignal -> {
            DataItem item = itemSignal.value();
            // Update to LOADING state
            itemSignal.value(new DataItem(
                item.id(),
                item.name(),
                LoadingState.State.LOADING,
                null,
                null,
                item.simulatedDelayMs()
            ));

            // Launch async operation (truly parallel via Spring @Async)
            dataLoadingService.loadDataAsync(item.id(), item.simulatedDelayMs(), simulateErrorsSignal.value())
                .thenAccept(data -> {
                    // Update to SUCCESS state
                    // Signals are thread-safe - update directly from background thread
                    itemSignal.value(new DataItem(
                        item.id(),
                        item.name(),
                        LoadingState.State.SUCCESS,
                        data,
                        null,
                        item.simulatedDelayMs()
                    ));
                })
                .exceptionally(error -> {
                    // Update to ERROR state
                    // Signals are thread-safe - update directly from background thread
                    itemSignal.value(new DataItem(
                        item.id(),
                        item.name(),
                        LoadingState.State.ERROR,
                        null,
                        extractErrorMessage(error),
                        item.simulatedDelayMs()
                    ));
                    return null;
                });
        });
    }

    /**
     * Retry loading a single item
     */
    private void retryItem(ValueSignal<DataItem> itemSignal) {
        DataItem item = itemSignal.value();

        // Update to LOADING state
        itemSignal.value(new DataItem(
            item.id(),
            item.name(),
            LoadingState.State.LOADING,
            null,
            null,
            item.simulatedDelayMs()
        ));

        // Launch async operation
        dataLoadingService.loadDataAsync(item.id(), item.simulatedDelayMs(), simulateErrorsSignal.value())
            .thenAccept(data -> {
                // Update to SUCCESS state
                itemSignal.value(new DataItem(
                    item.id(),
                    item.name(),
                    LoadingState.State.SUCCESS,
                    data,
                    null,
                    item.simulatedDelayMs()
                ));
            })
            .exceptionally(error -> {
                // Update to ERROR state
                itemSignal.value(new DataItem(
                    item.id(),
                    item.name(),
                    LoadingState.State.ERROR,
                    null,
                    extractErrorMessage(error),
                    item.simulatedDelayMs()
                ));
                return null;
            });
    }

    /**
     * Extract clean error message from exception cause
     */
    private String extractErrorMessage(Throwable error) {
        Throwable cause = error.getCause();
        if (cause != null && cause.getMessage() != null) {
            return cause.getMessage();
        }
        return error.getMessage() != null ? error.getMessage() : "An error occurred";
    }

    /**
     * Create a Card component for a data item with conditional rendering based on state
     */
    private Card createDataItemCard(ValueSignal<DataItem> itemSignal) {
        Card card = new Card();

        // Set card size for flex layout
        card.getStyle()
            .set("flex", "1 1 300px")
            .set("min-width", "300px");

        // Header with item name
        Signal<String> nameSignal = itemSignal.map(DataItem::name);
        Span titleSpan = new Span();
        titleSpan.bindText(nameSignal);
        card.setTitle(titleSpan);

        // IDLE state content
        Div idleContent = new Div();
        idleContent.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center")
            .set("gap", "0.5em")
            .set("padding", "2em")
            .set("color", "var(--lumo-secondary-text-color)");

        Icon idleIcon = new Icon(VaadinIcon.CIRCLE);
        idleIcon.setColor("var(--lumo-contrast-30pct)");
        idleIcon.setSize("2em");

        Span idleText = new Span("Ready to load");
        idleText.getStyle().set("font-style", "italic");

        idleContent.add(idleIcon, idleText);
        Signal<Boolean> isIdleSignal = itemSignal.map(item -> item.state() == LoadingState.State.IDLE);
        idleContent.bindVisible(isIdleSignal);

        // LOADING state content
        Div loadingContent = new Div();
        loadingContent.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center")
            .set("gap", "1em")
            .set("padding", "2em");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setWidth("80%");

        Span loadingText = new Span("Loading...");
        loadingText.getStyle().set("color", "var(--lumo-primary-color)");

        loadingContent.add(progressBar, loadingText);
        Signal<Boolean> isLoadingSignal = itemSignal.map(item -> item.state() == LoadingState.State.LOADING);
        loadingContent.bindVisible(isLoadingSignal);

        // SUCCESS state content
        Div successContent = new Div();
        successContent.getStyle()
            .set("padding", "1em");

        Div successHeader = new Div();
        successHeader.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5em")
            .set("margin-bottom", "1em");

        Icon successIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
        successIcon.setColor("var(--lumo-success-color)");

        Span successLabel = new Span("Loaded successfully");
        successLabel.getStyle()
            .set("color", "var(--lumo-success-color)")
            .set("font-weight", "500");

        successHeader.add(successIcon, successLabel);

        Div dataContent = new Div();
        dataContent.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("white-space", "pre-line")
            .set("font-family", "monospace")
            .set("font-size", "0.875em");

        Span dataText = new Span();
        dataText.bindText(itemSignal.map(item -> item.data() != null ? item.data() : ""));
        dataContent.add(dataText);

        successContent.add(successHeader, dataContent);
        Signal<Boolean> isSuccessSignal = itemSignal.map(item -> item.state() == LoadingState.State.SUCCESS);
        successContent.bindVisible(isSuccessSignal);

        // ERROR state content
        Div errorContent = new Div();
        errorContent.getStyle()
            .set("padding", "1em");

        Div errorHeader = new Div();
        errorHeader.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5em")
            .set("margin-bottom", "1em");

        Icon errorIcon = new Icon(VaadinIcon.CLOSE_CIRCLE);
        errorIcon.setColor("var(--lumo-error-color)");

        Span errorLabel = new Span("Failed to load");
        errorLabel.getStyle()
            .set("color", "var(--lumo-error-color)")
            .set("font-weight", "500");

        errorHeader.add(errorIcon, errorLabel);

        Div errorMessage = new Div();
        errorMessage.getStyle()
            .set("background-color", "#ffebee")
            .set("padding", "1em")
            .set("border-radius", "4px")
            .set("margin-bottom", "1em")
            .set("color", "var(--lumo-error-text-color)");

        Span errorText = new Span();
        errorText.bindText(itemSignal.map(item -> item.error() != null ? item.error() : "Unknown error"));
        errorMessage.add(errorText);

        Button retryButton = new Button("Retry", event -> retryItem(itemSignal));
        retryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        errorContent.add(errorHeader, errorMessage, retryButton);
        Signal<Boolean> isErrorSignal = itemSignal.map(item -> item.state() == LoadingState.State.ERROR);
        errorContent.bindVisible(isErrorSignal);

        // Add all state contents to card
        card.add(idleContent, loadingContent, successContent, errorContent);

        // Card styling based on state
        Signal<String> borderColorSignal = itemSignal.map(item -> {
            return switch (item.state()) {
                case IDLE -> "var(--lumo-contrast-20pct)";
                case LOADING -> "var(--lumo-primary-color)";
                case SUCCESS -> "var(--lumo-success-color)";
                case ERROR -> "var(--lumo-error-color)";
            };
        });

        // Apply border color using ComponentEffect
        com.vaadin.flow.component.ComponentEffect.bind(card, borderColorSignal,
            (c, color) -> c.getStyle().set("border-left", "4px solid " + color));

        return card;
    }
}
