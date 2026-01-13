package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "use-case-16", layout = MainLayout.class)
@PageTitle("Use Case 16: Responsive Dashboard with ComponentToggle")
@Menu(order = 62, title = "UC 16: ComponentToggle")
public class UseCase16View extends VerticalLayout {

    enum ViewMode { TABLE, CARDS, CHART }

    record DataItem(String name, String category, double value, String status) {}

    public UseCase16View() {
        // Load data
        List<DataItem> data = loadData();

        // Create signal for view mode
        WritableSignal<ViewMode> viewModeSignal = Signal.create(ViewMode.TABLE);

        // View mode selector buttons
        HorizontalLayout viewModeButtons = new HorizontalLayout();
        Button tableButton = new Button("Table View", e -> viewModeSignal.set(ViewMode.TABLE));
        Button cardsButton = new Button("Cards View", e -> viewModeSignal.set(ViewMode.CARDS));
        Button chartButton = new Button("Chart View", e -> viewModeSignal.set(ViewMode.CHART));
        viewModeButtons.add(tableButton, cardsButton, chartButton);

        // Table view
        Div tableView = new Div();
        Grid<DataItem> grid = new Grid<>(DataItem.class);
        grid.setColumns("name", "category", "value", "status");
        grid.setItems(data);
        tableView.add(grid);

        // Use ComponentToggle for lazy instantiation
        tableView.bindVisible(viewModeSignal.map(mode -> mode == ViewMode.TABLE));

        // Cards view
        Div cardsView = new Div();
        VerticalLayout cardsLayout = new VerticalLayout();
        for (DataItem item : data) {
            Div card = new Div();
            card.getStyle()
                .set("border", "1px solid #ccc")
                .set("padding", "1em")
                .set("margin", "0.5em")
                .set("border-radius", "4px");
            card.add(
                new H3(item.name()),
                new Div("Category: " + item.category()),
                new Div("Value: " + item.value()),
                new Div("Status: " + item.status())
            );
            cardsLayout.add(card);
        }
        cardsView.add(cardsLayout);
        cardsView.bindVisible(viewModeSignal.map(mode -> mode == ViewMode.CARDS));

        // Chart view
        Div chartView = new Div();
        Chart chart = new Chart(ChartType.COLUMN);
        Configuration config = chart.getConfiguration();
        config.setTitle("Data Visualization");

        DataSeries series = new DataSeries("Values");
        for (DataItem item : data) {
            series.add(new DataSeriesItem(item.name(), item.value()));
        }
        config.addSeries(series);

        chartView.add(chart);
        chartView.bindVisible(viewModeSignal.map(mode -> mode == ViewMode.CHART));

        // Responsive behavior: default view based on viewport width
        // This would typically be set based on actual viewport detection
        ReadableSignal<Boolean> isMobileSignal = Signal.create(false);

        // Auto-select view mode on first load based on device
        Signal.effect(() -> {
            if (isMobileSignal.get() && viewModeSignal.get() == ViewMode.TABLE) {
                viewModeSignal.set(ViewMode.CARDS);
            }
        });

        add(
            new H3("Business Dashboard"),
            viewModeButtons,
            tableView,
            cardsView,
            chartView
        );
    }

    private List<DataItem> loadData() {
        // Stub implementation - returns mock data
        return List.of(
            new DataItem("Product A", "Electronics", 12500.00, "Active"),
            new DataItem("Product B", "Clothing", 8200.50, "Active"),
            new DataItem("Product C", "Books", 3400.00, "Low Stock"),
            new DataItem("Product D", "Home & Garden", 15600.75, "Active"),
            new DataItem("Product E", "Electronics", 9800.00, "Discontinued"),
            new DataItem("Product F", "Clothing", 11200.25, "Active")
        );
    }
}
