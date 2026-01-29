package com.example.usecase22;

import com.vaadin.signals.local.ValueSignal;
import com.vaadin.signals.shared.SharedValueSignal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import com.example.usecase22.ServiceHealth.Status;
import com.example.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEffect;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import com.vaadin.signals.Signal;
import com.vaadin.signals.shared.SharedListSignal;
import com.vaadin.signals.WritableSignal;

import jakarta.annotation.security.PermitAll;

@PageTitle("Use Case 22: Real-time Dashboard")
@Route(value = "use-case-22", layout = MainLayout.class)
@Menu(order = 22, title = "UC 22: Real-time Dashboard")
@PermitAll
public class UseCase22View extends Main {

    private static final int TIMELINE_POINTS = 12;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final WritableSignal<Number> currentUsersSignal = new ValueSignal<>(745);
    private final WritableSignal<Number> viewEventsSignal = new ValueSignal<>(54600);
    private final WritableSignal<Number> conversionRateSignal = new ValueSignal<>(18);
    private final WritableSignal<Number> customMetricSignal = new ValueSignal<>(-123.45);

    private final Random random = new Random();
    private final List<HighlightCard> highlightCards = new ArrayList<>();
    private Chart viewEventsChart;
    private final SharedListSignal<String> timelineCategoriesSignal = new SharedListSignal<>(String.class);
    private final SharedListSignal<Number> berlinTimelineSignal = new SharedListSignal<>(Number.class);
    private final SharedListSignal<Number> londonTimelineSignal = new SharedListSignal<>(Number.class);
    private final SharedListSignal<Number> newYorkTimelineSignal = new SharedListSignal<>(Number.class);
    private final SharedListSignal<Number> tokyoTimelineSignal = new SharedListSignal<>(Number.class);
    private final SharedListSignal<ServiceHealth> serviceHealthSignal = new SharedListSignal<>(ServiceHealth.class);
    private Chart responseTimesChart;
    private DataSeries responseSeries;
    private final SharedListSignal<Number> responseSignal = new SharedListSignal<>(Number.class);

    public UseCase22View() {
        addClassName("dashboard-view");

        Board board = new Board();
        board.addRow(createHighlightCard("Current users", currentUsersSignal, this::formatNumber),
                createHighlightCard("View events", viewEventsSignal, this::formatCompactNumber),
                createHighlightCard("Conversion rate", conversionRateSignal, number -> String.format("%.1f%%", number.doubleValue())),
                createHighlightCard("Custom metric", customMetricSignal, this::formatNumber));
        board.addRow(createViewEvents());
        board.addRow(createServiceHealth(), createResponseTimes());
        add(board);

        addAttachListener(event -> {
            // TODO do not use polling for this
            UI ui = event.getUI();
            ui.setPollInterval(2000);
            ui.addPollListener(pollEvent -> updateMockData());
        });
    }

    private HighlightCard createHighlightCard(String title, WritableSignal<Number> signal, Function<Number, String> format) {
        
        HighlightCard card = new HighlightCard(title, signal, format);
        card.update(signal.value());
        highlightCards.add(card);
        return card;
    }

    private Component createViewEvents() {
        // Header
        HorizontalLayout header = createHeader("View events", "City / last 2 min");

        // Chart
        Chart chart = new Chart(ChartType.AREASPLINE);
        Configuration conf = chart.getConfiguration();
        conf.getChart().setStyledMode(true);

        XAxis xAxis = new XAxis();
        conf.addxAxis(xAxis);

        conf.getyAxis().setTitle("Values");

        PlotOptionsAreaspline plotOptions = new PlotOptionsAreaspline();
        plotOptions.setPointPlacement(PointPlacement.ON);
        plotOptions.setMarker(new Marker(false));
        conf.addPlotOptions(plotOptions);

        ListSeries berlinSeries = new ListSeries("Berlin", new Number[0]);
        ListSeries londonSeries = new ListSeries("London", new Number[0]);
        ListSeries newYorkSeries = new ListSeries("New York", new Number[0]);
        ListSeries tokyoSeries = new ListSeries("Tokyo", new Number[0]);

        bindData(chart, berlinSeries, berlinTimelineSignal);
        bindData(chart, londonSeries, londonTimelineSignal);
        bindData(chart, newYorkSeries, newYorkTimelineSignal);
        bindData(chart, tokyoSeries, tokyoTimelineSignal);

        ComponentEffect.effect(chart, () -> xAxis.setCategories(timelineCategoriesSignal.value().stream().map(Signal::value).toArray(String[]::new)));

        conf.addSeries(berlinSeries);
        conf.addSeries(londonSeries);
        conf.addSeries(newYorkSeries);
        conf.addSeries(tokyoSeries);

        viewEventsChart = chart;

        // Add it all together
        VerticalLayout viewEvents = new VerticalLayout(header, chart);
        viewEvents.setSpacing(false);
        viewEvents.getElement().getThemeList().add("spacing-l");
        return viewEvents;
    }

    private static void bindData(Chart chart, ListSeries series, SharedListSignal<Number> signal) {
        ComponentEffect.effect(chart, () -> {
            series.setData(signal.value().stream().map(Signal::value).toArray(Number[]::new));
            // TODO issue of getting the values from ListSignal instead of Signal<Number>
        });
    }

    private Component createServiceHealth() {
        // Header
        HorizontalLayout header = createHeader("Service health", "Input / output");

        // Grid
        Grid<SharedValueSignal<ServiceHealth>> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(true);

        grid.addColumn(new ComponentRenderer<>(signal -> {
            Span status = new Span();

            ComponentEffect.effect(status, () -> {
                ServiceHealth serviceHealth = signal.value();
                String statusTextInner = getStatusDisplayName(serviceHealth);
                status.getElement().setAttribute("aria-label", "Status: " + statusTextInner);
                status.getElement().setAttribute("title", "Status: " + statusTextInner);
                status.getElement().getThemeList().clear();
                status.getElement().getThemeList().add(getStatusTheme(serviceHealth));
            });
            return status;
        })).setHeader("").setFlexGrow(0).setAutoWidth(true);
        grid.addColumn(signal -> signal.value().getCity()).setHeader("City").setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(signal -> {
            var input = new Span(String.valueOf(signal.value().getInput()));
    
            ComponentEffect.effect(input, () -> input.setText(String.valueOf(signal.value().getInput())));
            return input;
        })).setHeader("Input").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(new ComponentRenderer<>(signal -> {
            var output = new Span(String.valueOf(signal.value().getOutput()));
    
            ComponentEffect.effect(output, () -> output.setText(String.valueOf(signal.value().getOutput())));
            return output;
        })).setHeader("Output").setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        // Add it all together
        VerticalLayout serviceHealth = new VerticalLayout(header, grid);

        ComponentEffect.effect(grid, () -> {
            grid.setItems(serviceHealthSignal.value());
            // TODO not this, update each signal individually
        });

        mockServiceHealth().forEach(serviceHealthSignal::insertLast);

        return serviceHealth;
    }

    private Component createResponseTimes() {
        HorizontalLayout header = createHeader("Response times", "Average across all systems");

        // Chart
        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.getChart().setStyledMode(true);
        chart.setThemeName("gradient");
        responseTimesChart = chart;

        responseSeries = new DataSeries();
        responseSeries.add(new DataSeriesItem("System 1", 12.5));
        responseSeries.add(new DataSeriesItem("System 2", 12.5));
        responseSeries.add(new DataSeriesItem("System 3", 12.5));
        responseSeries.add(new DataSeriesItem("System 4", 12.5));
        responseSeries.add(new DataSeriesItem("System 5", 12.5));
        responseSeries.add(new DataSeriesItem("System 6", 12.5));
        conf.addSeries(responseSeries);

        ComponentEffect.effect(chart, () -> {
            var responseValues = responseSignal.value();
            responseSeries.get(0).setY(responseValues.get(0).value());
            responseSeries.get(1).setY(responseValues.get(1).value());
            responseSeries.get(2).setY(responseValues.get(2).value());
            responseSeries.get(3).setY(responseValues.get(3).value());
            responseSeries.get(4).setY(responseValues.get(4).value());
            responseSeries.get(5).setY(responseValues.get(5).value());
        });

        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);
        responseSignal.insertLast(12.5);

        // Add it all together
        VerticalLayout serviceHealth = new VerticalLayout(header, chart);
        serviceHealth.setSpacing(false);
        return serviceHealth;
    }

    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames(FontSize.XLARGE, Margin.NONE);

        Span span = new Span(subtitle);
        span.addClassNames(TextColor.SECONDARY, FontSize.XSMALL);

        VerticalLayout column = new VerticalLayout(h2, span);
        column.setPadding(false);
        column.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(column);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }

    private String getStatusDisplayName(ServiceHealth serviceHealth) {
        Status status = serviceHealth.getStatus();
        if (status == Status.OK) {
            return "Ok";
        } else if (status == Status.FAILING) {
            return "Failing";
        } else if (status == Status.EXCELLENT) {
            return "Excellent";
        } else {
            return status.toString();
        }
    }

    private String getStatusTheme(ServiceHealth serviceHealth) {
        Status status = serviceHealth.getStatus();
        String theme = "badge primary small";
        if (status == Status.EXCELLENT) {
            theme += " success";
        } else if (status == Status.FAILING) {
            theme += " error";
        }
        return theme;
    }

    private void updateMockData() {
        if (highlightCards.size() >= 4) {
            currentUsersSignal.value(randomBetween(650, 820));
            viewEventsSignal.value(randomBetween(42000, 62000));
            conversionRateSignal.value(randomBetween(12, 24));
            customMetricSignal.value(randomBetween(-200, 200));
        }

        if (berlinTimelineSignal.value().size() >= TIMELINE_POINTS) {
            berlinTimelineSignal.remove(berlinTimelineSignal.value().getFirst()); // TODO github issue remove first / last? 
        }
        berlinTimelineSignal.insertLast(randomBetween(480, 920));
        
        if (londonTimelineSignal.value().size() >= TIMELINE_POINTS) {
            londonTimelineSignal.remove(londonTimelineSignal.value().getFirst());
        }
        londonTimelineSignal.insertLast(randomBetween(420, 820));

        if (newYorkTimelineSignal.value().size() >= TIMELINE_POINTS) {
            newYorkTimelineSignal.remove(newYorkTimelineSignal.value().getFirst());
        }
        newYorkTimelineSignal.insertLast(randomBetween(220, 520));

        if (tokyoTimelineSignal.value().size() >= TIMELINE_POINTS) {
            tokyoTimelineSignal.remove(tokyoTimelineSignal.value().getFirst());
        }
        tokyoTimelineSignal.insertLast(randomBetween(260, 600));

        if (timelineCategoriesSignal.value().size() >= TIMELINE_POINTS) {
            timelineCategoriesSignal.remove(timelineCategoriesSignal.value().getFirst());
        }
        timelineCategoriesSignal.insertLast(LocalTime.now().format(TIME_FORMATTER));

        if (viewEventsChart != null) {
            viewEventsChart.drawChart();
        }

        for (SharedValueSignal<Number> signal : responseSignal.value()) {
            signal.value(randomBetween(6, 22));
        }

        
        if (responseTimesChart != null) {
            responseTimesChart.drawChart();
        }

        // TODO report issue ListSignal set items / set value
        //serviceHealthSignal.clear();
        var healthValues = serviceHealthSignal.value();
        mockServiceHealth().forEach(newHealth -> healthValues.forEach(currentHealthSignal -> {
            var currentHealt = currentHealthSignal.value();
            if (Objects.equals(currentHealt.getCity(), newHealth.getCity())) {
                currentHealthSignal.value(newHealth);
            }
        }));
    }

    private List<ServiceHealth> mockServiceHealth() {
        return List.of(
                new ServiceHealth(randomStatus(), "Münster", randomBetween(280, 360), randomBetween(1200, 1700)),
                new ServiceHealth(randomStatus(), "Cluj-Napoca", randomBetween(260, 340), randomBetween(1100, 1600)),
                new ServiceHealth(randomStatus(), "Ciudad Victoria", randomBetween(240, 320), randomBetween(1000, 1500)));
    }

    private Status randomStatus() {
        int pick = random.nextInt(3);
        if (pick == 0) {
            return Status.EXCELLENT;
        } else if (pick == 1) {
            return Status.OK;
        }
        return Status.FAILING;
    }

    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private String formatNumber(Number value) {
        return String.valueOf(value);
    }

    private String formatCompactNumber(Number value) {
        if (value.doubleValue() >= 1000) {
            double rounded = Math.round(value.doubleValue() / 100.0) / 10.0;
            return rounded + "k";
        }
        return String.valueOf(value);
    }

    private static final class HighlightCard extends VerticalLayout{
        private final Span valueSpan;
        private final Span badge;
        private final Function<Number, String> format;
        private Number lastNumeric;

        private HighlightCard(String title, WritableSignal<Number> signal, Function<Number, String> format) {
            this.format = format;

            H2 h2 = new H2(title);
            h2.addClassNames(FontWeight.NORMAL, Margin.NONE, TextColor.SECONDARY, FontSize.XSMALL);

            valueSpan = new Span(format.apply(signal.value()));
            valueSpan.addClassNames(FontWeight.SEMIBOLD, FontSize.XXXLARGE);
            ComponentEffect.effect(valueSpan, () -> update(signal.value()));

            badge = new Span();

            add(h2, valueSpan, badge);
            getStyle().setGap("5px");
        }

        private void update(Number newValue) {
            valueSpan.setText(format.apply(newValue));
            badge.removeAll();

            VaadinIcon icon = VaadinIcon.ARROW_UP;
            String prefix = "";
            String theme = "badge";

            double percentage = calculatePercentageChange(newValue, lastNumeric);
            if (percentage == 0) {
                prefix = "±";
            } else if (percentage > 0) {
                prefix = "+";
                theme += " success";
            } else {
                icon = VaadinIcon.ARROW_DOWN;
                theme += " error";
            }

            Icon i = icon.create();
            i.setSize("10px");
            i.getStyle().setMarginRight("4px").setMarginLeft("0");
            badge.add(i, new Span(prefix + percentage));
            badge.getElement().getThemeList().clear();
            badge.getElement().getThemeList().add(theme);

            lastNumeric = newValue;
        }

        private double calculatePercentageChange(Number current, Number previous) {
            if (previous == null) {
                return 0.0;
            }
            double percent = ((current.doubleValue() - previous.doubleValue()) / Math.abs(previous.doubleValue())) * 100.0;
            return Math.round(percent * 10.0) / 10.0;
        }
    }

}
