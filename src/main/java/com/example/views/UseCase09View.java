package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "use-case-09", layout = MainLayout.class)
@PageTitle("Use Case 9: Filtered and Sorted Data Grid")
@Menu(order = 40, title = "UC 9: Filtered Data Grid")
public class UseCase09View extends VerticalLayout {

    record Product(String id, String name, String category, double price, int stock) {}

    public UseCase09View() {
        // Create signals for filter inputs
        WritableSignal<String> categoryFilterSignal = Signal.create("All");
        WritableSignal<String> searchTermSignal = Signal.create("");
        WritableSignal<Boolean> inStockOnlySignal = Signal.create(false);

        // Load all products
        List<Product> allProducts = loadProducts();

        // Computed signal for filtered products
        ReadableSignal<List<Product>> filteredProductsSignal = Signal.compute(() -> {
            String category = categoryFilterSignal.get();
            String searchTerm = searchTermSignal.get().toLowerCase();
            boolean inStockOnly = inStockOnlySignal.get();

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
        grid.bindItems(filteredProductsSignal);

        add(categoryFilter, searchField, inStockCheckbox, grid);
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
