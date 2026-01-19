package com.example.usecase06;

import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.example.views.MainLayout;
import com.vaadin.flow.component.ComponentEffect;
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

@Route(value = "use-case-06", layout = MainLayout.class)
@PageTitle("Use Case 6: Shopping Cart with Real-time Totals")
@Menu(order = 6, title = "UC 6: Shopping Cart")
@PermitAll
public class UseCase06View extends VerticalLayout {

    public UseCase06View() {
        setSpacing(true);
        setPadding(true);

        var title = new H2("Use Case 6: Shopping Cart with Real-time Totals");

        var description = new Paragraph(
                "This use case demonstrates a shopping cart with reactive totals. "
                        + "As you adjust quantities, add/remove items, enter discount codes, or change shipping options, "
                        + "all totals (subtotal, discount, tax, shipping, and grand total) update automatically. "
                        + "Try using discount codes SAVE10 or SAVE20.");

        // Create signals for cart state
        var cartItemsSignal = new ListSignal<>(CartItem.class);
        var discountCodeSignal = new ValueSignal<>("");
        var shippingOptionSignal = new ValueSignal<>(ShippingOption.STANDARD);

        // Computed signal for subtotal
        var subtotalSignal = Signal.computed(
                () -> cartItemsSignal.value().stream().map(ValueSignal::value)
                        .map(item -> item.product().price()
                                .multiply(BigDecimal.valueOf(item.quantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Computed signal for discount
        var discountSignal = Signal.computed(() -> {
            String code = discountCodeSignal.value();
            DiscountCode discount = validateDiscountCode(code);
            if (discount != null) {
                return subtotalSignal.value().multiply(discount.percentage())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        });

        // Computed signal for shipping cost
        var shippingSignal = Signal
                .computed(() -> getShippingCost(shippingOptionSignal.value()));

        // Computed signal for tax (8%)
        var taxSignal = Signal.computed(
                () -> subtotalSignal.value().subtract(discountSignal.value())
                        .multiply(new BigDecimal("0.08"))
                        .setScale(2, RoundingMode.HALF_UP));

        // Computed signal for grand total
        var totalSignal = Signal.computed(() -> subtotalSignal.value()
                .subtract(discountSignal.value()).add(shippingSignal.value())
                .add(taxSignal.value()).setScale(2, RoundingMode.HALF_UP));

        var products = List.of(
                new Product("1", "Laptop", new BigDecimal("999.99")),
                new Product("2", "Mouse", new BigDecimal("25.99")),
                new Product("3", "Keyboard", new BigDecimal("79.99")),
                new Product("4", "Monitor", new BigDecimal("249.50")));

        // Products list
        var productsTitle = new H3("Available Products");
        var productsContainer = new Div();
        productsContainer.getStyle().set("background-color", "#fff3e0")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-bottom", "1em");
        products.forEach(product -> productsContainer
                .add(createProductRow(product, cartItemsSignal)));

        // Cart items display
        var cartTitle = new H3("Shopping Cart");
        var emptyCartButton = new Button("Empty cart",
                e -> cartItemsSignal.clear());
        emptyCartButton.addThemeName("error");
        emptyCartButton.addThemeName("small");

        var cartHeader = new HorizontalLayout(cartTitle, emptyCartButton);
        cartHeader.setWidthFull();
        cartHeader.setAlignItems(Alignment.CENTER);
        cartHeader.expand(cartTitle);
        var cartItemsContainer = new Div();
        cartItemsContainer.getStyle().set("background-color", "#f5f5f5")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-bottom", "1em");

        var cartItemsList = new Div();
        ComponentEffect.bindChildren(cartItemsList, cartItemsSignal,
                itemSignal -> createCartItemRow(itemSignal, cartItemsSignal));

        var emptyCart = new Paragraph("Empty cart");
        emptyCart.getStyle().set("margin", "0").set("font-style", "italic")
                .set("color", "var(--lumo-secondary-text-color)");
        emptyCart.bindVisible(
                Signal.computed(() -> cartItemsSignal.value().isEmpty()));
        cartItemsContainer.add(emptyCart, cartItemsList);

        // Options section
        var optionsLayout = new HorizontalLayout();
        optionsLayout.setWidthFull();
        optionsLayout.setSpacing(true);

        var discountField = new TextField("Discount Code");
        discountField.setPlaceholder("Enter SAVE10 or SAVE20");
        discountField.setWidth("250px");
        discountField.bindValue(discountCodeSignal);

        var shippingSelect = new ComboBox<>("Shipping Method",
                ShippingOption.values());
        shippingSelect.setValue(ShippingOption.STANDARD);
        shippingSelect.setWidth("200px");
        shippingSelect.bindValue(shippingOptionSignal);

        optionsLayout.add(discountField, shippingSelect);

        // Totals display
        var totalsBox = new Div();
        totalsBox.getStyle().set("background-color", "#e8f5e9")
                .set("padding", "1.5em").set("border-radius", "4px")
                .set("margin-top", "1em");

        var summaryTitle = new H3("Order Summary");
        summaryTitle.getStyle().set("margin-top", "0");

        var subtotalLabel = new Span();
        subtotalLabel.bindText(subtotalSignal.map(total -> "Subtotal: $"
                + total.setScale(2, RoundingMode.HALF_UP)));
        subtotalLabel.getStyle().set("display", "block").set("margin-bottom",
                "0.5em");

        var discountLabel = new Span();
        discountLabel.bindText(discountSignal.map(discount -> "Discount: -$"
                + discount.setScale(2, RoundingMode.HALF_UP)));
        discountLabel.bindVisible(
                discountSignal.map(d -> d.compareTo(BigDecimal.ZERO) > 0));
        discountLabel.getStyle().set("display", "block")
                .set("margin-bottom", "0.5em")
                .set("color", "var(--lumo-success-color)");

        var shippingLabel = new Span();
        shippingLabel.bindText(shippingSignal.map(shipping -> "Shipping: $"
                + shipping.setScale(2, RoundingMode.HALF_UP)));
        shippingLabel.getStyle().set("display", "block").set("margin-bottom",
                "0.5em");

        var taxLabel = new Span();
        taxLabel.bindText(taxSignal.map(
                tax -> "Tax (8%): $" + tax.setScale(2, RoundingMode.HALF_UP)));
        taxLabel.getStyle().set("display", "block").set("margin-bottom",
                "0.5em");

        var divider = new Div();
        divider.getStyle()
                .set("border-top", "2px solid var(--lumo-contrast-20pct)")
                .set("margin", "0.75em 0");

        var totalLabel = new Span();
        totalLabel.bindText(totalSignal.map(
                total -> "Total: $" + total.setScale(2, RoundingMode.HALF_UP)));
        totalLabel.getStyle().set("display", "block").set("font-weight", "bold")
                .set("font-size", "1.5em")
                .set("color", "var(--lumo-primary-color)");

        totalsBox.add(summaryTitle, subtotalLabel, discountLabel, shippingLabel,
                taxLabel, divider, totalLabel);

        // Info box
        var infoBox = new Div();
        infoBox.getStyle().set("background-color", "#e0f7fa")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "💡 This use case demonstrates computed signals with multiple levels of calculation. "
                        + "The subtotal is computed from cart items, the discount is computed from the subtotal and discount code, "
                        + "the tax is computed from (subtotal - discount), and the final total combines all values. "
                        + "All calculations happen reactively as you modify any input."));

        add(title, description, productsTitle, productsContainer, cartHeader,
                cartItemsContainer, optionsLayout, totalsBox, infoBox);
    }

    private HorizontalLayout createProductRow(Product product,
            ListSignal<CartItem> cartItemsSignal) {
        var row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("background-color", "#ffffff")
                .set("padding", "0.75em").set("border-radius", "4px")
                .set("margin-bottom", "0.5em");

        var nameLabel = new Span(product.name() + " - $"
                + product.price().setScale(2, RoundingMode.HALF_UP));
        nameLabel.getStyle().set("flex-grow", "1").set("font-weight", "500");

        var addButton = new Button("Add",
                e -> addToCart(product, cartItemsSignal));
        addButton.addThemeName("primary");
        addButton.addThemeName("small");

        row.add(nameLabel, addButton);
        return row;
    }

    private HorizontalLayout createCartItemRow(ValueSignal<CartItem> itemSignal,
            ListSignal<CartItem> cartItemsSignal) {
        var item = itemSignal.value();

        var row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("background-color", "#ffffff")
                .set("padding", "0.75em").set("border-radius", "4px")
                .set("margin-bottom", "0.5em");

        var nameLabel = new Span(item.product().name() + " - $"
                + item.product().price().setScale(2, RoundingMode.HALF_UP));
        nameLabel.getStyle().set("flex-grow", "1").set("font-weight", "500");

        var quantityField = new IntegerField();
        quantityField.setValue(item.quantity());
        quantityField.setMin(1);
        quantityField.setMax(99);
        quantityField.setWidth("120px");
        quantityField.setStepButtonsVisible(true);
        quantityField.addValueChangeListener(e -> {
            var value = e.getValue();
            updateQuantity(itemSignal, cartItemsSignal,
                    value == null ? 0 : value);
        });
        ComponentEffect.bind(quantityField, itemSignal.map(CartItem::quantity),
                (field, value) -> {
                    if (value != null && !value.equals(field.getValue())) {
                        field.setValue(value);
                    }
                });

        var itemTotalLabel = new Span();
        itemTotalLabel.bindText(Signal.computed(() -> {
            var current = itemSignal.value();
            return "$" + current.product().price()
                    .multiply(BigDecimal.valueOf(current.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
        }));
        itemTotalLabel.setWidth("100px");
        itemTotalLabel.getStyle().set("text-align", "right")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        var removeButton = new Button("Remove", e -> {
            cartItemsSignal.remove(itemSignal);
        });
        removeButton.addThemeName("error");
        removeButton.addThemeName("small");

        row.add(nameLabel, quantityField, itemTotalLabel, removeButton);
        return row;
    }

    private void addToCart(Product product,
            ListSignal<CartItem> cartItemsSignal) {
        cartItemsSignal.value().stream().filter(
                signal -> signal.value().product().id().equals(product.id()))
                .findFirst().ifPresentOrElse(
                        existing -> updateQuantity(existing, cartItemsSignal,
                                existing.value().quantity() + 1),
                        () -> cartItemsSignal
                                .insertLast(new CartItem(product, 1)));
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

    private void updateQuantity(ValueSignal<CartItem> itemSignal,
            ListSignal<CartItem> cartItemsSignal, int quantity) {
        if (quantity < 1) {
            cartItemsSignal.remove(itemSignal);
            return;
        }

        var current = itemSignal.value();
        itemSignal.value(new CartItem(current.product(), quantity));
    }
}
