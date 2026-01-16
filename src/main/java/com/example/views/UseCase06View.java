package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.MissingAPI;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

@Route(value = "use-case-06", layout = MainLayout.class)
@PageTitle("Use Case 6: Shopping Cart with Real-time Totals")
@Menu(order = 6, title = "UC 6: Shopping Cart")
@PermitAll
public class UseCase06View extends VerticalLayout {

    record CartItem(String id, String name, BigDecimal price, int quantity) {
    }

    record DiscountCode(String code, BigDecimal percentage) {
    }

    enum ShippingOption {
        STANDARD, EXPRESS, OVERNIGHT
    }

    public UseCase06View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 6: Shopping Cart with Real-time Totals");

        Paragraph description = new Paragraph(
                "This use case demonstrates a shopping cart with reactive totals. "
                        + "As you adjust quantities, add/remove items, enter discount codes, or change shipping options, "
                        + "all totals (subtotal, discount, tax, shipping, and grand total) update automatically. "
                        + "Try using discount codes SAVE10 or SAVE20.");

        // Create signals for cart state
        ListSignal<CartItem> cartItemsSignal = new ListSignal<>(CartItem.class);
        cartItemsSignal.insertLast(
                new CartItem("1", "Laptop", new BigDecimal("999.99"), 1));
        cartItemsSignal.insertLast(
                new CartItem("2", "Mouse", new BigDecimal("25.99"), 2));
        cartItemsSignal.insertLast(
                new CartItem("3", "Keyboard", new BigDecimal("79.99"), 1));

        WritableSignal<String> discountCodeSignal = new ValueSignal<>("");
        WritableSignal<ShippingOption> shippingOptionSignal = new ValueSignal<>(
                ShippingOption.STANDARD);

        // Computed signal for subtotal
        Signal<BigDecimal> subtotalSignal = Signal.computed(
                () -> cartItemsSignal.value().stream().map(itemSignal -> {
                    CartItem item = itemSignal.value();
                    return item.price()
                            .multiply(BigDecimal.valueOf(item.quantity()));
                }).reduce(BigDecimal.ZERO, BigDecimal::add));

        // Computed signal for discount
        Signal<BigDecimal> discountSignal = Signal.computed(() -> {
            String code = discountCodeSignal.value();
            DiscountCode discount = validateDiscountCode(code);
            if (discount != null) {
                return subtotalSignal.value().multiply(discount.percentage())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        });

        // Computed signal for shipping cost
        Signal<BigDecimal> shippingSignal = Signal
                .computed(() -> getShippingCost(shippingOptionSignal.value()));

        // Computed signal for tax (8%)
        Signal<BigDecimal> taxSignal = Signal.computed(
                () -> subtotalSignal.value().subtract(discountSignal.value())
                        .multiply(new BigDecimal("0.08"))
                        .setScale(2, RoundingMode.HALF_UP));

        // Computed signal for grand total
        Signal<BigDecimal> totalSignal = Signal.computed(
                () -> subtotalSignal.value().subtract(discountSignal.value())
                        .add(shippingSignal.value()).add(taxSignal.value())
                        .setScale(2, RoundingMode.HALF_UP));

        // Cart items display
        H3 cartTitle = new H3("Shopping Cart");
        Div cartItemsContainer = new Div();
        cartItemsContainer.getStyle().set("background-color", "#f5f5f5")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-bottom", "1em");

        MissingAPI
                .bindChildren(cartItemsContainer,
                        cartItemsSignal
                                .map(itemSignals -> itemSignals.stream()
                                        .map(itemSignal -> createCartItemRow(
                                                itemSignal, cartItemsSignal))
                                        .toList()));

        // Options section
        HorizontalLayout optionsLayout = new HorizontalLayout();
        optionsLayout.setWidthFull();
        optionsLayout.setSpacing(true);

        TextField discountField = new TextField("Discount Code");
        discountField.setPlaceholder("Enter SAVE10 or SAVE20");
        discountField.setWidth("250px");
        discountField.bindValue(discountCodeSignal);

        ComboBox<ShippingOption> shippingSelect = new ComboBox<>(
                "Shipping Method", ShippingOption.values());
        shippingSelect.setValue(ShippingOption.STANDARD);
        shippingSelect.setWidth("200px");
        shippingSelect.bindValue(shippingOptionSignal);

        optionsLayout.add(discountField, shippingSelect);

        // Totals display
        Div totalsBox = new Div();
        totalsBox.getStyle().set("background-color", "#e8f5e9")
                .set("padding", "1.5em").set("border-radius", "4px")
                .set("margin-top", "1em");

        H3 summaryTitle = new H3("Order Summary");
        summaryTitle.getStyle().set("margin-top", "0");

        Span subtotalLabel = new Span();
        subtotalLabel.bindText(subtotalSignal.map(total -> "Subtotal: $"
                + total.setScale(2, RoundingMode.HALF_UP)));
        subtotalLabel.getStyle().set("display", "block").set("margin-bottom",
                "0.5em");

        Span discountLabel = new Span();
        discountLabel.bindText(discountSignal.map(discount -> "Discount: -$"
                + discount.setScale(2, RoundingMode.HALF_UP)));
        discountLabel.bindVisible(
                discountSignal.map(d -> d.compareTo(BigDecimal.ZERO) > 0));
        discountLabel.getStyle().set("display", "block")
                .set("margin-bottom", "0.5em")
                .set("color", "var(--lumo-success-color)");

        Span shippingLabel = new Span();
        shippingLabel.bindText(shippingSignal.map(shipping -> "Shipping: $"
                + shipping.setScale(2, RoundingMode.HALF_UP)));
        shippingLabel.getStyle().set("display", "block").set("margin-bottom",
                "0.5em");

        Span taxLabel = new Span();
        taxLabel.bindText(taxSignal.map(
                tax -> "Tax (8%): $" + tax.setScale(2, RoundingMode.HALF_UP)));
        taxLabel.getStyle().set("display", "block").set("margin-bottom",
                "0.5em");

        Div divider = new Div();
        divider.getStyle()
                .set("border-top", "2px solid var(--lumo-contrast-20pct)")
                .set("margin", "0.75em 0");

        Span totalLabel = new Span();
        totalLabel.bindText(totalSignal.map(
                total -> "Total: $" + total.setScale(2, RoundingMode.HALF_UP)));
        totalLabel.getStyle().set("display", "block").set("font-weight", "bold")
                .set("font-size", "1.5em")
                .set("color", "var(--lumo-primary-color)");

        totalsBox.add(summaryTitle, subtotalLabel, discountLabel, shippingLabel,
                taxLabel, divider, totalLabel);

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "💡 This use case demonstrates computed signals with multiple levels of calculation. "
                        + "The subtotal is computed from cart items, the discount is computed from the subtotal and discount code, "
                        + "the tax is computed from (subtotal - discount), and the final total combines all values. "
                        + "All calculations happen reactively as you modify any input."));

        add(title, description, cartTitle, cartItemsContainer, optionsLayout,
                totalsBox, infoBox, new SourceCodeLink(getClass()));
    }

    private HorizontalLayout createCartItemRow(ValueSignal<CartItem> itemSignal,
            ListSignal<CartItem> cartItemsSignal) {
        CartItem item = itemSignal.value();

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(HorizontalLayout.Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("background-color", "#ffffff")
                .set("padding", "0.75em").set("border-radius", "4px")
                .set("margin-bottom", "0.5em");

        Span nameLabel = new Span(item.name() + " - $" + item.price());
        nameLabel.getStyle().set("flex-grow", "1").set("font-weight", "500");

        IntegerField quantityField = new IntegerField();
        quantityField.setValue(item.quantity());
        quantityField.setMin(1);
        quantityField.setMax(99);
        quantityField.setWidth("80px");
        quantityField.addValueChangeListener(e -> {
            CartItem current = itemSignal.value();
            itemSignal.value(new CartItem(current.id(), current.name(),
                    current.price(), e.getValue()));
        });

        Span itemTotalLabel = new Span("$"
                + item.price().multiply(BigDecimal.valueOf(item.quantity())));
        itemTotalLabel.setWidth("100px");
        itemTotalLabel.getStyle().set("text-align", "right")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        Button removeButton = new Button("Remove", e -> {
            cartItemsSignal.remove(itemSignal);
        });
        removeButton.addThemeName("error");
        removeButton.addThemeName("small");

        row.add(nameLabel, quantityField, itemTotalLabel, removeButton);
        return row;
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
