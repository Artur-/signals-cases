package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Route(value = "use-case-04", layout = MainLayout.class)
@PageTitle("Use Case 4: E-commerce Product Configurator")
@Menu(order = 20, title = "UC 4: Product Configurator")
public class UseCase04View extends VerticalLayout {

    record ProductVariant(String size, String color, BigDecimal price, int stock, String imageUrl) {}

    public UseCase04View() {
        // Create signals for product options
        WritableSignal<String> sizeSignal = Signal.create("M");
        WritableSignal<String> colorSignal = Signal.create("Blue");
        WritableSignal<Integer> quantitySignal = Signal.create(1);

        // ComboBoxes for size and color
        ComboBox<String> sizeSelect = new ComboBox<>("Size", List.of("S", "M", "L", "XL"));
        sizeSelect.setValue("M");
        sizeSelect.bindValue(sizeSignal);

        ComboBox<String> colorSelect = new ComboBox<>("Color", List.of("Blue", "Red", "Black", "White"));
        colorSelect.setValue("Blue");
        colorSelect.bindValue(colorSignal);

        IntegerField quantityField = new IntegerField("Quantity");
        quantityField.setValue(1);
        quantityField.setMin(1);
        quantityField.setMax(10);
        quantityField.bindValue(quantitySignal);

        // Computed signal for selected variant
        ReadableSignal<ProductVariant> variantSignal = Signal.compute(() -> {
            String size = sizeSignal.get();
            String color = colorSignal.get();
            return getVariant(size, color);
        });

        // Computed signal for total price
        ReadableSignal<BigDecimal> totalPriceSignal = Signal.compute(() -> {
            ProductVariant variant = variantSignal.get();
            int quantity = quantitySignal.get();
            return variant.price().multiply(BigDecimal.valueOf(quantity));
        });

        // Computed signal for availability status
        ReadableSignal<String> availabilitySignal = Signal.compute(() -> {
            ProductVariant variant = variantSignal.get();
            int quantity = quantitySignal.get();
            if (variant.stock() >= quantity) {
                return "In Stock (" + variant.stock() + " available)";
            } else if (variant.stock() > 0) {
                return "Only " + variant.stock() + " left";
            } else {
                return "Out of Stock";
            }
        });

        // Display components
        Image productImage = new Image();
        productImage.setWidth("300px");
        productImage.setHeight("300px");
        productImage.bindAttribute("src", variantSignal.map(v -> v.imageUrl()));

        Span priceLabel = new Span();
        priceLabel.bindText(totalPriceSignal.map(price -> "Total: $" + price));

        Span availabilityLabel = new Span();
        availabilityLabel.bindText(availabilitySignal);
        availabilityLabel.bindAttribute("style", availabilitySignal.map(status ->
            status.startsWith("Out of Stock") ? "color: red;" : "color: green;"
        ));

        add(sizeSelect, colorSelect, quantityField, productImage, priceLabel, availabilityLabel);
    }

    private ProductVariant getVariant(String size, String color) {
        // Stub implementation - returns mock data
        Map<String, Map<String, ProductVariant>> inventory = Map.of(
            "M", Map.of(
                "Blue", new ProductVariant("M", "Blue", new BigDecimal("29.99"), 50, "/images/shirt-m-blue.jpg"),
                "Red", new ProductVariant("M", "Red", new BigDecimal("29.99"), 30, "/images/shirt-m-red.jpg"),
                "Black", new ProductVariant("M", "Black", new BigDecimal("29.99"), 20, "/images/shirt-m-black.jpg"),
                "White", new ProductVariant("M", "White", new BigDecimal("29.99"), 40, "/images/shirt-m-white.jpg")
            ),
            "L", Map.of(
                "Blue", new ProductVariant("L", "Blue", new BigDecimal("31.99"), 45, "/images/shirt-l-blue.jpg"),
                "Red", new ProductVariant("L", "Red", new BigDecimal("31.99"), 25, "/images/shirt-l-red.jpg"),
                "Black", new ProductVariant("L", "Black", new BigDecimal("31.99"), 15, "/images/shirt-l-black.jpg"),
                "White", new ProductVariant("L", "White", new BigDecimal("31.99"), 35, "/images/shirt-l-white.jpg")
            )
        );
        return inventory.getOrDefault(size, Map.of()).getOrDefault(color,
            new ProductVariant(size, color, new BigDecimal("29.99"), 0, "/images/shirt-default.jpg"));
    }
}
