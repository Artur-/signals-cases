package com.example.views;

import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.example.model.Invoice;
import com.example.model.InvoiceDetails;
import com.example.model.LineItem;
import com.example.service.InvoiceService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEffect;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.ListSignal;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;

import static com.example.service.InvoiceService.EMPTY_DETAILS;
import static com.example.service.InvoiceService.EMPTY_INVOICE;

@Route(value = "use-case-7", layout = MainLayout.class)
@PageTitle("Use Case 7: Master-Detail Invoice View")
@Menu(order = 7, title = "UC 7: Master-Detail Invoice")
@PermitAll
public class UseCase07View extends VerticalLayout {

    private final InvoiceService invoiceService;

    private final ListSignal<Invoice> invoiceListSignal = new ListSignal<>(
            Invoice.class);

    private final ListSignal<LineItem> lineItemsSignal = new ListSignal<>(
            LineItem.class);

    private Grid<Invoice> invoiceGrid;

    public UseCase07View(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;

        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 7: Master-Detail Invoice View");

        Paragraph description = new Paragraph(
                "This use case demonstrates a master-detail pattern with reactive selection handling. "
                        + "Select an invoice from the grid to see its detailed information in the side panel. "
                        + "The detail panel updates automatically when you select a different invoice, "
                        + "showing customer information, line items, and payment status.");

        invoiceService.fetchInvoices().forEach(invoiceListSignal::insertLast);

        // Create signal for selected invoice (use empty invoice as initial
        // value)
        WritableSignal<Invoice> selectedInvoiceSignal = new ValueSignal<>(
                EMPTY_INVOICE);

        WritableSignal<LineItem> selectedLineItemSignal = new ValueSignal<>(
                InvoiceService.EMPTY_LINEITEM);

        // Computed signal for invoice details
        Signal<InvoiceDetails> invoiceDetailsSignal = Signal.computed(() -> {
            Invoice selected = selectedInvoiceSignal.value();
            return (selected != null && !selected.getId().isEmpty())
                    ? invoiceService.fetchInvoiceDetails(selected.getId())
                    : EMPTY_DETAILS;
        });

        // Master: Invoice grid
        invoiceGrid = new Grid<>(Invoice.class);
        invoiceGrid.setColumns("id", "customerName", "dueDate", "total",
                "status");
        ComponentEffect.bind(invoiceGrid, invoiceListSignal, (grid, items) -> {
            var invoices = invoiceListSignal.value().stream()
                    .map(ValueSignal::peek).toList();
            if (items != null) {
                grid.setItems(invoices);
            } else {
                grid.setItems(List.of());
            }
            invoiceListSignal.value().forEach(signal -> ComponentEffect
                    .bind(invoiceGrid, signal, (g, inv) -> {
                        g.getDataProvider().refreshItem(inv);
                    }));
        });
        invoiceGrid.asSingleSelect().addValueChangeListener(e -> {
            Invoice selected = e.getValue();
            selectedInvoiceSignal
                    .value(selected != null ? selected : EMPTY_INVOICE);
        });

        // Detail: Invoice details panel
        VerticalLayout detailsPanel = new VerticalLayout();
        detailsPanel.add(new H3("Invoice Details"));

        // Customer information
        FormLayout customerInfo = new FormLayout();
        customerInfo.setWidthFull();
        TextField customerName = new TextField();
        customerInfo.addFormItem(customerName, "Customer:");

        EmailField customerEmail = new EmailField();
        customerEmail.setHelperText("Valid email address required");
        customerInfo.addFormItem(customerEmail, "Email:");

        TextField customerAddress = new TextField();
        customerInfo.addFormItem(customerAddress, "Address:");

        DatePicker dueDate = new DatePicker();
        customerInfo.addFormItem(dueDate, "Due Date:");

        Binder<Invoice> invoiceBinder = new Binder<>(Invoice.class);
        invoiceBinder.forField(customerName).bind(Invoice::getCustomerName,
                Invoice::setCustomerName);
        invoiceBinder.forField(dueDate).bind(Invoice::getDueDate,
                Invoice::setDueDate);
        ComponentEffect.effect(this, () -> invoiceBinder
                .setBean(invoiceDetailsSignal.value().getInvoice()));
        invoiceBinder.addValueChangeListener(event -> {
            var status = invoiceBinder.validate();
            if (status.isOk()) {
                Invoice invoice = invoiceBinder.getBean();
                invoiceService.updateInvoice(invoice);
                // update invoiceListSignal's matching ValueSignal value for the
                // changed invoice
                updateInvoiceListSignalItem(invoice);
            } else if (status.hasErrors()) {
                Notification.show("Invoice has validation errors.");
            }
        });

        Binder<InvoiceDetails> detailsBinder = new Binder<>(
                InvoiceDetails.class);
        detailsBinder.forField(customerEmail)
                .withValidator(new EmailValidator("Invalid email address"))
                .bind(InvoiceDetails::getCustomerEmail,
                        InvoiceDetails::setCustomerEmail);
        detailsBinder.forField(customerAddress).bind(
                InvoiceDetails::getCustomerAddress,
                InvoiceDetails::setCustomerAddress);
        ComponentEffect.effect(this,
                () -> detailsBinder.setBean(invoiceDetailsSignal.value()));
        detailsBinder.addValueChangeListener(event -> {
            var status = detailsBinder.validate();
            if (status.isOk()) {
                InvoiceDetails details = detailsBinder.getBean();
                invoiceService.updateDetails(details);
            } else if (status.hasErrors()) {
                Notification.show("Invoice details has validation errors.");
            }
        });

        // Line items grid
        Grid<LineItem> lineItemsGrid = new Grid<>(LineItem.class);
        lineItemsGrid.setHeightFull();
        lineItemsGrid.setAllRowsVisible(true);
        lineItemsGrid.setSelectionMode(Grid.SelectionMode.NONE);
        lineItemsGrid.setColumns("description", "quantity", "unitPrice",
                "total");
        lineItemsGrid.appendFooterRow();
        lineItemsGrid.addComponentColumn(lineItem -> {
            Button button = new Button();
            button.setTooltipText("Remove Line Item");
            button.addClickListener(e -> {
                removeLineItem(invoiceDetailsSignal.value(), lineItem);
            });
            button.setIcon(VaadinIcon.CLOSE.create());
            return button;
        });

        ComponentEffect.bind(lineItemsGrid, lineItemsSignal, (grid, items) -> {
            var lineItems = lineItemsSignal.value().stream()
                    .map(ValueSignal::peek).toList();
            if (items != null) {
                grid.setItems(lineItems);
            } else {
                grid.setItems(List.of());
            }
            updateFooterTotal(lineItemsGrid);
            lineItemsSignal.value().forEach(signal -> ComponentEffect
                    .bind(lineItemsGrid, signal, (g, inv) -> {
                        // refresh the item in the grid
                        g.getDataProvider().refreshItem(inv);
                        // update footer total
                        updateFooterTotal(lineItemsGrid);
                    }));
        });

        ComponentEffect.effect(lineItemsGrid, () -> {
            List<LineItem> items = invoiceDetailsSignal.value().getLineItems();
            lineItemsSignal.clear();
            items.forEach(lineItemsSignal::insertLast);
        });

        // Line item editor fields
        TextField descriptionField = new TextField();
        descriptionField.setSizeFull();
        descriptionField.setThemeVariants(TextFieldVariant.LUMO_SMALL);

        IntegerField quantityField = new IntegerField();
        quantityField.setSizeFull();
        quantityField.setThemeVariants(TextFieldVariant.LUMO_SMALL);

        BigDecimalField unitPriceField = new BigDecimalField();
        unitPriceField.setSizeFull();
        unitPriceField.setThemeVariants(TextFieldVariant.LUMO_SMALL);

        // Line item editor binder (unbuffered)
        Binder<LineItem> lineItemBinder = new Binder<>(LineItem.class);
        lineItemBinder.forField(descriptionField).bind(LineItem::getDescription,
                LineItem::setDescription);
        lineItemBinder.forField(quantityField).bind(LineItem::getQuantity,
                LineItem::setQuantity);
        lineItemBinder.forField(unitPriceField).bind(LineItem::getUnitPrice,
                LineItem::setUnitPrice);

        ComponentEffect.effect(this,
                () -> lineItemBinder.setBean(selectedLineItemSignal.value()));
        lineItemBinder.addValueChangeListener(event -> {
            var status = lineItemBinder.validate();
            if (status.isOk()) {
                LineItem lineItem = lineItemBinder.getBean();
                InvoiceDetails details = invoiceService.updateLineItem(
                        detailsBinder.getBean().getInvoice(), lineItem);
                // update invoiceListSignal's matching ValueSignal value for the
                // changed invoice
                updateInvoiceListSignalItem(details.getInvoice());
                // update lineItemsSignal's matching ValueSignal value for the
                // changed line item
                lineItemsSignal.peek().stream().filter(
                        signal -> signal.peek().getId() == lineItem.getId())
                        .findFirst().ifPresent(signal -> {
                            signal.value(
                                    details.getLineItemById(lineItem.getId()));
                        });
            } else if (status.hasErrors()) {
                Notification.show("Line item has validation errors.");
            }
        });

        lineItemsGrid.getEditor().setBinder(lineItemBinder);
        lineItemsGrid.getColumnByKey("description")
                .setEditorComponent(descriptionField);
        lineItemsGrid.getColumnByKey("quantity")
                .setEditorComponent(quantityField);
        lineItemsGrid.getColumnByKey("unitPrice")
                .setEditorComponent(unitPriceField);
        lineItemsGrid.addItemDoubleClickListener(e -> {
            lineItemsGrid.getEditor().editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable<?> focusable) {
                focusable.focus();
            }
        });

        // Payment status
        Span paymentStatus = new Span();
        paymentStatus.bindText(invoiceDetailsSignal.map(
                details -> "Payment Status: " + details.getPaymentStatus()));
        paymentStatus.getStyle().set("font-weight", "bold");

        detailsPanel.add(customerInfo, new H3("Line Items"),
                new Span("(Double-click a cell to edit)"));
        detailsPanel.add(new Button("Add Line Item",
                e -> addNewLineItem(invoiceDetailsSignal.value())));
        detailsPanel.addAndExpand(lineItemsGrid);
        detailsPanel.add(paymentStatus);
        detailsPanel
                .bindVisible(invoiceDetailsSignal.map(details -> details != null
                        && details.getInvoice().getId() != null
                        && details != EMPTY_DETAILS));

        Button addInvoiceButton = new Button("New Invoice",
                e -> addNewInvoice());

        // Layout
        VerticalLayout mainLayout = new VerticalLayout();
        HorizontalLayout contentLayout = new HorizontalLayout(invoiceGrid,
                detailsPanel);
        contentLayout.setWidthFull();
        mainLayout.add(addInvoiceButton);
        mainLayout.addAndExpand(contentLayout);
        invoiceGrid.setWidth("60%");
        detailsPanel.setWidth("50%");
        detailsPanel.setHeightFull();
        add(title, description, mainLayout);
    }

    private void updateInvoiceListSignalItem(Invoice invoice) {
        invoiceListSignal.peek().stream()
                .filter(signal -> signal.peek().getId().equals(invoice.getId()))
                .findFirst().ifPresent(signal -> {
                    signal.value(invoice);
                });
    }

    private void updateFooterTotal(Grid<LineItem> lineItemsGrid) {
        lineItemsGrid.getColumnByKey("total")
                .setFooter("Total: "
                        + lineItemsSignal.peek().stream().map(ValueSignal::peek)
                                .toList().stream().map(LineItem::getTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private void addNewInvoice() {
        Invoice invoice = invoiceService.createNewEmptyInvoice();
        invoiceListSignal.insertLast(invoice);
        invoiceGrid.select(invoice);
    }

    private void addNewLineItem(InvoiceDetails details) {
        LineItem item = invoiceService
                .createNewEmptyLineItem(details.getInvoice().getId());
        lineItemsSignal.insertLast(item);
    }

    private void removeLineItem(InvoiceDetails value, LineItem lineItem) {
        invoiceService.removeLineItem(value.getInvoice().getId(), lineItem);
        lineItemsSignal.value().stream().filter(v -> v.peek().equals(lineItem))
                .findFirst().ifPresent(lineItemsSignal::remove);
        // need to also update the invoice total in the invoice list
        updateInvoiceListSignalItem(
                invoiceService.fetchInvoice(value.getInvoice().getId()));
    }
}
