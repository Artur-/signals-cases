package com.example.views;

import com.example.MissingAPI;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import jakarta.annotation.security.PermitAll;

@Route(value = "use-case-09", layout = MainLayout.class)
@PageTitle("Use Case 4: Filtered and Sorted Data Grid")
@Menu(order = 4, title = "UC 4: Filtered Data Grid")
@PermitAll
public class UseCase04View extends VerticalLayout {

    public static record Product(String id, String name, String category, double price, int stock) {}

    public UseCase04View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 4: Filtered and Sorted Data Grid");

        Paragraph description = new Paragraph(
            "This use case demonstrates a Grid with reactive client-side filtering and sorting. " +
            "Filter by category, search by name/ID, and toggle in-stock only. " +
            "All filters are applied reactively through computed signals. " +
            "The filtered list updates automatically as you change any filter."
        );

        // Create signals for filter inputs
        WritableSignal<String> categoryFilterSignal = new ValueSignal<>("All");
        WritableSignal<String> searchTermSignal = new ValueSignal<>("");
        WritableSignal<Boolean> inStockOnlySignal = new ValueSignal<>(false);

        // Load all products
        List<Product> allProducts = loadProducts();

        // Computed signal for filtered products
        Signal<List<Product>> filteredProductsSignal = Signal.computed(() -> {
            String category = categoryFilterSignal.value();
            String searchTerm = searchTermSignal.value().toLowerCase();
            boolean inStockOnly = inStockOnlySignal.value();

            return allProducts.stream()
                .filter(p -> category.equals("All") || p.category().equals(category))
                .filter(p -> searchTerm.isEmpty() ||
                           p.name().toLowerCase().contains(searchTerm) ||
                           p.id().toLowerCase().contains(searchTerm))
                .filter(p -> !inStockOnly || p.stock() > 0)
                .toList();
        });

        // Filter UI components
        ComboBox<String> categoryFilter = new ComboBox<>("Category",
            List.of("All", "Electronics", "Clothing", "Books", "Home & Garden"));
        categoryFilter.setValue("All");
        categoryFilter.bindValue(categoryFilterSignal);

        TextField searchField = new TextField("Search");
        searchField.setPlaceholder("Search by name or ID");
        searchField.bindValue(searchTermSignal);

        Checkbox inStockCheckbox = new Checkbox("Show in-stock items only");
        inStockCheckbox.bindValue(inStockOnlySignal);

        // Data grid
        Grid<Product> grid = new Grid<>(Product.class);
        grid.setColumns("id", "name", "category", "price", "stock");
        MissingAPI.bindItems(grid, filteredProductsSignal);

        add(title, description, categoryFilter, searchField, inStockCheckbox, grid);
    }

    private List<Product> loadProducts() {
        // Stub implementation - returns mock data
        return List.of(
            new Product("P001", "Laptop", "Electronics", 999.99, 15),
            new Product("P002", "T-Shirt", "Clothing", 19.99, 50),
            new Product("P003", "Java Programming Book", "Books", 49.99, 0),
            new Product("P004", "Garden Hose", "Home & Garden", 29.99, 30),
            new Product("P005", "Wireless Mouse", "Electronics", 25.99, 0),
            new Product("P006", "Jeans", "Clothing", 59.99, 20),
            new Product("P007", "Fiction Novel", "Books", 14.99, 100),
            new Product("P008", "Plant Pot", "Home & Garden", 12.99, 45),
            new Product("P009", "Keyboard", "Electronics", 79.99, 8),
            new Product("P010", "Winter Jacket", "Clothing", 129.99, 5)
        );
    }
}
