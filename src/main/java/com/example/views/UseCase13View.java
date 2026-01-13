package com.example.views;

// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Route(value = "use-case-13", layout = MainLayout.class)
@PageTitle("Use Case 13: Master-Detail Invoice View")
@Menu(order = 52, title = "UC 13: Master-Detail Invoice")
public class UseCase13View extends VerticalLayout {

    record Invoice(String id, String customerName, LocalDate date, BigDecimal total, String status) {}
    record LineItem(String description, int quantity, BigDecimal unitPrice, BigDecimal total) {}
    record InvoiceDetails(Invoice invoice, String customerEmail, String customerAddress,
                         List<LineItem> lineItems, String paymentStatus) {}

    public UseCase13View() {
        // Create signal for selected invoice
        WritableSignal<Invoice> selectedInvoiceSignal = Signal.create(null);

        // Computed signal for invoice details
        ReadableSignal<InvoiceDetails> invoiceDetailsSignal = Signal.compute(() -> {
            Invoice selected = selectedInvoiceSignal.get();
            return selected != null ? loadInvoiceDetails(selected.id()) : null;
        });

        // Master: Invoice grid
        Grid<Invoice> invoiceGrid = new Grid<>(Invoice.class);
        invoiceGrid.setColumns("id", "customerName", "date", "total", "status");
        invoiceGrid.setItems(loadInvoices());
        invoiceGrid.asSingleSelect().addValueChangeListener(e ->
            selectedInvoiceSignal.set(e.getValue())
        );

        // Detail: Invoice details panel
        VerticalLayout detailsPanel = new VerticalLayout();
        detailsPanel.add(new H3("Invoice Details"));

        // Customer information
        Div customerInfo = new Div();
        Span customerName = new Span();
        customerName.bindText(invoiceDetailsSignal.map(details ->
            details != null ? "Customer: " + details.invoice().customerName() : ""
        ));

        Span customerEmail = new Span();
        customerEmail.bindText(invoiceDetailsSignal.map(details ->
            details != null ? "Email: " + details.customerEmail() : ""
        ));

        Span customerAddress = new Span();
        customerAddress.bindText(invoiceDetailsSignal.map(details ->
            details != null ? "Address: " + details.customerAddress() : ""
        ));

        customerInfo.add(customerName, customerEmail, customerAddress);

        // Line items grid
        Grid<LineItem> lineItemsGrid = new Grid<>(LineItem.class);
        lineItemsGrid.setColumns("description", "quantity", "unitPrice", "total");
        lineItemsGrid.bindItems(invoiceDetailsSignal.map(details ->
            details != null ? details.lineItems() : List.of()
        ));

        // Payment status
        Span paymentStatus = new Span();
        paymentStatus.bindText(invoiceDetailsSignal.map(details ->
            details != null ? "Payment Status: " + details.paymentStatus() : ""
        ));
        paymentStatus.getStyle().set("font-weight", "bold");

        detailsPanel.add(customerInfo, new H3("Line Items"), lineItemsGrid, paymentStatus);
        detailsPanel.bindVisible(invoiceDetailsSignal.map(details -> details != null));

        // Layout
        HorizontalLayout mainLayout = new HorizontalLayout(invoiceGrid, detailsPanel);
        mainLayout.setSizeFull();
        add(new H3("Invoices"), mainLayout);
    }

    private List<Invoice> loadInvoices() {
        // Stub implementation - returns mock data
        return List.of(
            new Invoice("INV-001", "Acme Corp", LocalDate.of(2025, 1, 15), new BigDecimal("1250.00"), "Paid"),
            new Invoice("INV-002", "TechStart Inc", LocalDate.of(2025, 1, 16), new BigDecimal("3400.50"), "Pending"),
            new Invoice("INV-003", "Global Solutions", LocalDate.of(2025, 1, 17), new BigDecimal("890.00"), "Paid"),
            new Invoice("INV-004", "Beta Corp", LocalDate.of(2025, 1, 18), new BigDecimal("2100.75"), "Overdue")
        );
    }

    private InvoiceDetails loadInvoiceDetails(String invoiceId) {
        // Stub implementation - returns mock data
        return switch (invoiceId) {
            case "INV-001" -> new InvoiceDetails(
                new Invoice("INV-001", "Acme Corp", LocalDate.of(2025, 1, 15), new BigDecimal("1250.00"), "Paid"),
                "contact@acmecorp.com",
                "123 Main St, New York, NY 10001",
                List.of(
                    new LineItem("Software License", 5, new BigDecimal("200.00"), new BigDecimal("1000.00")),
                    new LineItem("Support Package", 1, new BigDecimal("250.00"), new BigDecimal("250.00"))
                ),
                "Paid on 2025-01-20"
            );
            case "INV-002" -> new InvoiceDetails(
                new Invoice("INV-002", "TechStart Inc", LocalDate.of(2025, 1, 16), new BigDecimal("3400.50"), "Pending"),
                "info@techstart.com",
                "456 Tech Ave, San Francisco, CA 94102",
                List.of(
                    new LineItem("Enterprise License", 10, new BigDecimal("300.00"), new BigDecimal("3000.00")),
                    new LineItem("Training Session", 2, new BigDecimal("200.00"), new BigDecimal("400.00")),
                    new LineItem("Setup Fee", 1, new BigDecimal("0.50"), new BigDecimal("0.50"))
                ),
                "Payment pending - Due 2025-02-15"
            );
            case "INV-003" -> new InvoiceDetails(
                new Invoice("INV-003", "Global Solutions", LocalDate.of(2025, 1, 17), new BigDecimal("890.00"), "Paid"),
                "billing@globalsolutions.com",
                "789 Business Blvd, Boston, MA 02108",
                List.of(
                    new LineItem("Consulting Hours", 8, new BigDecimal("100.00"), new BigDecimal("800.00")),
                    new LineItem("Travel Expenses", 1, new BigDecimal("90.00"), new BigDecimal("90.00"))
                ),
                "Paid on 2025-01-25"
            );
            case "INV-004" -> new InvoiceDetails(
                new Invoice("INV-004", "Beta Corp", LocalDate.of(2025, 1, 18), new BigDecimal("2100.75"), "Overdue"),
                "accounts@betacorp.com",
                "321 Commerce St, Chicago, IL 60601",
                List.of(
                    new LineItem("Annual Subscription", 1, new BigDecimal("2000.00"), new BigDecimal("2000.00")),
                    new LineItem("Extra Storage", 1, new BigDecimal("100.75"), new BigDecimal("100.75"))
                ),
                "OVERDUE - Payment was due 2025-01-31"
            );
            default -> null;
        };
    }
}
