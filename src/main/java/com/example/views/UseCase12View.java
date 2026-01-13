package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Route(value = "use-case-12", layout = MainLayout.class)
@PageTitle("Use Case 12: Shopping Cart with Real-time Totals")
@Menu(order = 51, title = "UC 12: Shopping Cart")
public class UseCase12View extends VerticalLayout {

    record CartItem(String id, String name, BigDecimal price, int quantity) {}
    record DiscountCode(String code, BigDecimal percentage) {}

    enum ShippingOption { STANDARD, EXPRESS, OVERNIGHT }

    public UseCase12View() {
        // Create signals for cart state
        WritableSignal<List<CartItem>> cartItemsSignal = Signal.create(new ArrayList<>(List.of(
            new CartItem("1", "Laptop", new BigDecimal("999.99"), 1),
            new CartItem("2", "Mouse", new BigDecimal("25.99"), 2),
            new CartItem("3", "Keyboard", new BigDecimal("79.99"), 1)
        )));

        WritableSignal<String> discountCodeSignal = Signal.create("");
        WritableSignal<ShippingOption> shippingOptionSignal = Signal.create(ShippingOption.STANDARD);

        // Computed signal for subtotal
        ReadableSignal<BigDecimal> subtotalSignal = Signal.compute(() ->
            cartItemsSignal.get().stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        // Computed signal for discount
        ReadableSignal<BigDecimal> discountSignal = Signal.compute(() -> {
            String code = discountCodeSignal.get();
            DiscountCode discount = validateDiscountCode(code);
            if (discount != null) {
                return subtotalSignal.get()
                    .multiply(discount.percentage())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        });

        // Computed signal for shipping cost
        ReadableSignal<BigDecimal> shippingSignal = Signal.compute(() ->
            getShippingCost(shippingOptionSignal.get())
        );

        // Computed signal for tax (8%)
        ReadableSignal<BigDecimal> taxSignal = Signal.compute(() ->
            subtotalSignal.get()
                .subtract(discountSignal.get())
                .multiply(new BigDecimal("0.08"))
                .setScale(2, RoundingMode.HALF_UP)
        );

        // Computed signal for grand total
        ReadableSignal<BigDecimal> totalSignal = Signal.compute(() ->
            subtotalSignal.get()
                .subtract(discountSignal.get())
                .add(shippingSignal.get())
                .add(taxSignal.get())
                .setScale(2, RoundingMode.HALF_UP)
        );

        // Cart items display
        VerticalLayout cartItemsLayout = new VerticalLayout();
        cartItemsLayout.add(new H3("Shopping Cart"));
        cartItemsLayout.bindChildren(cartItemsSignal.map(items ->
            items.stream()
                .map(item -> createCartItemRow(item, cartItemsSignal))
                .toList()
        ));

        // Discount code input
        TextField discountField = new TextField("Discount Code");
        discountField.bindValue(discountCodeSignal);

        // Shipping options
        ComboBox<ShippingOption> shippingSelect = new ComboBox<>("Shipping Method", ShippingOption.values());
        shippingSelect.setValue(ShippingOption.STANDARD);
        shippingSelect.bindValue(shippingOptionSignal);

        // Totals display
        Div totalsDiv = new Div();
        totalsDiv.add(new H3("Order Summary"));

        Span subtotalLabel = new Span();
        subtotalLabel.bindText(subtotalSignal.map(total -> "Subtotal: $" + total.setScale(2, RoundingMode.HALF_UP)));

        Span discountLabel = new Span();
        discountLabel.bindText(discountSignal.map(discount -> "Discount: -$" + discount.setScale(2, RoundingMode.HALF_UP)));
        discountLabel.bindVisible(discountSignal.map(d -> d.compareTo(BigDecimal.ZERO) > 0));

        Span shippingLabel = new Span();
        shippingLabel.bindText(shippingSignal.map(shipping -> "Shipping: $" + shipping.setScale(2, RoundingMode.HALF_UP)));

        Span taxLabel = new Span();
        taxLabel.bindText(taxSignal.map(tax -> "Tax (8%): $" + tax.setScale(2, RoundingMode.HALF_UP)));

        Span totalLabel = new Span();
        totalLabel.bindText(totalSignal.map(total -> "Total: $" + total.setScale(2, RoundingMode.HALF_UP)));
        totalLabel.getStyle().set("font-weight", "bold");
        totalLabel.getStyle().set("font-size", "1.5em");

        totalsDiv.add(subtotalLabel, discountLabel, shippingLabel, taxLabel, totalLabel);

        add(cartItemsLayout, discountField, shippingSelect, totalsDiv);
    }

    private HorizontalLayout createCartItemRow(CartItem item, WritableSignal<List<CartItem>> cartItemsSignal) {
        Span nameLabel = new Span(item.name() + " - $" + item.price());
        nameLabel.getStyle().set("flex-grow", "1");

        IntegerField quantityField = new IntegerField();
        quantityField.setValue(item.quantity());
        quantityField.setMin(1);
        quantityField.setMax(99);
        quantityField.setWidth("80px");
        quantityField.addValueChangeListener(e -> {
            List<CartItem> currentItems = new ArrayList<>(cartItemsSignal.get());
            int index = currentItems.indexOf(item);
            if (index >= 0) {
                currentItems.set(index, new CartItem(item.id(), item.name(), item.price(), e.getValue()));
                cartItemsSignal.set(currentItems);
            }
        });

        Span itemTotalLabel = new Span("$" + item.price().multiply(BigDecimal.valueOf(item.quantity())));
        itemTotalLabel.setWidth("100px");
        itemTotalLabel.getStyle().set("text-align", "right");

        Button removeButton = new Button("Remove", e -> {
            List<CartItem> currentItems = new ArrayList<>(cartItemsSignal.get());
            currentItems.remove(item);
            cartItemsSignal.set(currentItems);
        });
        removeButton.addThemeName("error");
        removeButton.addThemeName("small");

        return new HorizontalLayout(nameLabel, quantityField, itemTotalLabel, removeButton);
    }

    private DiscountCode validateDiscountCode(String code) {
        // Stub implementation
        return switch (code.toUpperCase()) {
            case "SAVE10" -> new DiscountCode("SAVE10", new BigDecimal("10"));
            case "SAVE20" -> new DiscountCode("SAVE20", new BigDecimal("20"));
            default -> null;
        };
    }

    private BigDecimal getShippingCost(ShippingOption option) {
        return switch (option) {
            case STANDARD -> new BigDecimal("5.99");
            case EXPRESS -> new BigDecimal("12.99");
            case OVERNIGHT -> new BigDecimal("24.99");
        };
    }
}
