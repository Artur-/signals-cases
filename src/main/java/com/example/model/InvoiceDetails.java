package com.example.model;

import java.util.List;

public class InvoiceDetails {
    private Invoice invoice;
    private String customerEmail;
    private String customerAddress;
    private List<LineItem> lineItems;
    private String paymentStatus;

    public InvoiceDetails() {
    }

    public InvoiceDetails(Invoice invoice, String customerEmail,
            String customerAddress, List<LineItem> lineItems,
            String paymentStatus) {
        this.invoice = invoice;
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.lineItems = lineItems;
        this.paymentStatus = paymentStatus;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LineItem getLineItemById(int id) {
        return lineItems.stream().filter(li -> li.getId() == id).findFirst()
                .orElse(null);
    }
}
