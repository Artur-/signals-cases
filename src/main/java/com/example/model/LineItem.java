package com.example.model;

import java.math.BigDecimal;
import java.util.Objects;

public class LineItem {
    private int id;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;

    public LineItem() {
    }

    public LineItem(int id, String description, int quantity,
            BigDecimal unitPrice, BigDecimal total) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        LineItem lineItem = (LineItem) o;
        return getId() == lineItem.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
