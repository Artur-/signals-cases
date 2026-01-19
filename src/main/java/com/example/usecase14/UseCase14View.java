package com.example.usecase14;

import com.example.views.MainLayout;

import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.MissingAPI;
import com.example.service.AnalyticsService;
import com.example.service.AnalyticsService.AnalyticsReport;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Use Case 14: Async Data Loading with States
 *
 * Demonstrates async operations with loading/success/error states using analytics
 * report generation as a realistic heavy operation example:
 * - Spring Boot @Async service for background processing
 * - Signal with LoadingState (Loading/Success/Error states)
 * - Loading spinner while processing data
 * - Display analytics dashboard on success
 * - Error message with retry button
 * - Proper separation of concerns (View → Service)
 *
 * Key Patterns:
 * - Spring @Async service integration
 * - CompletableFuture for async operations
 * - Async signal updates with UI thread synchronization
 * - Loading state representation
 * - Error handling with retry
 * - Dashboard-style data visualization
 */
@Route(value = "use-case-14", layout = MainLayout.class)
@PageTitle("Use Case 14: Async Data Loading")
@Menu(order = 14, title = "UC 14: Async Data Loading")
@PermitAll
public class UseCase14View extends VerticalLayout {

    private final AnalyticsService analyticsService;

    private final WritableSignal<LoadingState.State> stateSignal = new ValueSignal<>(
            LoadingState.State.IDLE);

    private final WritableSignal<AnalyticsReport> reportDataSignal = new ValueSignal<>(
            AnalyticsReport.empty());

    private final WritableSignal<String> errorSignal = new ValueSignal<>("");

    private final WritableSignal<Boolean> shouldFailSignal = new ValueSignal<>(
            false);

    private final WritableSignal<String> loadingMessageSignal = new ValueSignal<>("");

    public UseCase14View(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 14: Async Data Loading with States");

        Paragraph description = new Paragraph(
                "This use case demonstrates multi-step async operations with proper loading/success/error states. "
                        + "Click 'Generate Analytics Report' to start a two-step process: first fetching relevant data, then generating the report. "
                        + "Toggle 'Simulate Error' to see error handling. "
                        + "The UI reactively shows progress through each step, displays data on success, or shows error messages with retry.");

        // Controls
        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing(true);

        Button loadButton = new Button("Generate Analytics Report", event -> loadReport());
        loadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Disable load button while loading
        Signal<Boolean> isLoadingSignal = stateSignal
                .map(state -> state == LoadingState.State.LOADING);
        loadButton.bindEnabled(isLoadingSignal.map(loading -> !loading));

        Div errorToggle = new Div();
        errorToggle.getStyle().set("display", "flex")
                .set("align-items", "center").set("gap", "0.5em");

        var checkbox = new com.vaadin.flow.component.checkbox.Checkbox(
                "Simulate Error");
        checkbox.bindValue(shouldFailSignal);
        errorToggle.add(checkbox);

        controls.add(loadButton, errorToggle);

        // State display box
        Div stateBox = new Div();
        stateBox.getStyle()
                .set("border", "2px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "8px").set("padding", "1.5em")
                .set("margin", "1em 0").set("min-height", "300px");

        // Idle state
        Div idleContent = new Div();
        Paragraph idleMessage = new Paragraph(
                "👆 Click 'Generate Analytics Report' to start the two-step process: fetch data, then generate comprehensive report with sales metrics and performance data");
        idleMessage.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic");
        idleContent.add(idleMessage);
        Signal<Boolean> isIdleSignal = stateSignal.map(state -> state == LoadingState.State.IDLE);
        idleContent.bindVisible(isIdleSignal);

        // Loading state
        Div loadingContent = new Div();
        loadingContent.getStyle().set("display", "flex")
                .set("flex-direction", "column").set("align-items", "center")
                .set("gap", "1em");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setWidth("200px");

        Paragraph loadingMessage = new Paragraph();
        loadingMessage.bindText(loadingMessageSignal);
        loadingMessage.getStyle().set("color", "var(--lumo-primary-color)");

        loadingContent.add(progressBar, loadingMessage);
        loadingContent.bindVisible(isLoadingSignal);

        // Success state
        Div successContent = new Div();
        H3 successTitle = new H3();
        Signal<String> reportTitleSignal = reportDataSignal
                .map(report -> !report.isEmpty()
                        ? "Analytics Report - " + report.getPeriod()
                        : "Analytics Report");
        successTitle.bindText(reportTitleSignal);
        successTitle.getStyle().set("margin-top", "0").set("color",
                "var(--lumo-success-color)");

        // Metrics grid
        Div metricsGrid = new Div();
        metricsGrid.getStyle().set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                .set("gap", "1em").set("margin-top", "1em");

        // Create metric cards with signals
        Signal<String> revenueSignal = reportDataSignal.map(report -> {
            if (!report.isEmpty()) {
                return String.format("$%,d", report.getTotalRevenue());
            }
            return "";
        });
        Card revenueCard = createMetricCardWithSignal("Total Revenue", revenueSignal,
                VaadinIcon.DOLLAR, "#4CAF50");

        Signal<String> ordersSignal = reportDataSignal.map(report -> {
            if (!report.isEmpty()) {
                return String.format("%,d", report.getTotalOrders());
            }
            return "";
        });
        Card ordersCard = createMetricCardWithSignal("Total Orders", ordersSignal,
                VaadinIcon.PACKAGE, "#2196F3");

        Signal<String> conversionSignal = reportDataSignal.map(report -> {
            if (!report.isEmpty()) {
                return String.format("%.2f%%", report.getConversionRate());
            }
            return "";
        });
        Card conversionCard = createMetricCardWithSignal("Conversion Rate", conversionSignal,
                VaadinIcon.TRENDING_UP, "#FF9800");

        Signal<String> usersSignalValue = reportDataSignal.map(report -> {
            if (!report.isEmpty()) {
                return String.format("%,d", report.getActiveUsers());
            }
            return "";
        });
        Card usersCard = createMetricCardWithSignal("Active Users", usersSignalValue,
                VaadinIcon.USERS, "#9C27B0");

        metricsGrid.add(revenueCard, ordersCard, conversionCard, usersCard);

        successContent.add(successTitle, metricsGrid);
        Signal<Boolean> isSuccessSignal = stateSignal
                .map(state -> state == LoadingState.State.SUCCESS);
        successContent.bindVisible(isSuccessSignal);

        // Error state
        Div errorContent = new Div();
        errorContent.getStyle().set("background-color", "#ffebee")
                .set("padding", "1em").set("border-radius", "4px")
                .set("border-left", "4px solid var(--lumo-error-color)");

        H3 errorTitle = new H3("❌ Report Generation Failed");
        errorTitle.getStyle().set("margin-top", "0").set("color",
                "var(--lumo-error-color)");

        Paragraph errorMessage = new Paragraph("Analytics report generation failed. Please contact the administrator.");
        errorMessage.getStyle().set("margin", "0.5em 0");

        errorContent.add(errorTitle, errorMessage);
        Signal<Boolean> isErrorSignal = stateSignal.map(state -> state == LoadingState.State.ERROR);
        errorContent.bindVisible(isErrorSignal);

        stateBox.add(idleContent, loadingContent, successContent, errorContent);

        add(title, description, controls, stateBox);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Could auto-load data on attach if desired
    }

    private void loadReport() {
        // Set loading state immediately
        stateSignal.value(LoadingState.State.LOADING);
        reportDataSignal.value(AnalyticsReport.empty());
        errorSignal.value("");
        loadingMessageSignal.value("Fetching relevant data... (1/2)");

        // Step 1: Fetch raw data from data sources
        // The @Async annotation causes Spring to execute this in a separate thread
        analyticsService.fetchReportData(shouldFailSignal.value())
                .thenCompose(rawData -> {
                    // Signals are thread-safe - update directly from background thread
                    loadingMessageSignal.value("Generating report... (2/2)");

                    // Step 2: Process the fetched data into a report
                    return analyticsService.generateReportFromData(rawData, shouldFailSignal.value());
                })
                .thenAccept(report -> {
                    // Signals are thread-safe - update directly from background thread
                    reportDataSignal.value(report);
                    stateSignal.value(LoadingState.State.SUCCESS);
                })
                .exceptionally(error -> {
                    // Signals are thread-safe - update directly from background thread
                    stateSignal.value(LoadingState.State.ERROR);
                    return null;
                });
    }

    private Card createMetricCardWithSignal(String label, Signal<String> valueSignal,
            VaadinIcon iconType, String color) {
        // Create card using Vaadin Card component with proper slots
        Card card = new Card();

        // Use headerPrefix slot for icon
        Icon icon = new Icon(iconType);
        icon.setColor(color);
        icon.setSize("var(--lumo-icon-size-m)");
        card.setHeaderPrefix(icon);

        // Use title slot for label
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500");
        card.setTitle(labelSpan);

        // Main content slot for value display
        Span valueSpan = new Span();
        valueSpan.bindText(valueSignal);
        valueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xxxl)")
                .set("font-weight", "bold")
                .set("color", color)
                .set("line-height", "1");
        card.add(valueSpan);

        // Custom styling for colored border
        card.getStyle().set("border-left", "4px solid " + color);

        return card;
    }
}
