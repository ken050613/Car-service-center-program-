package com.apu.asc.model;

import java.math.BigDecimal;
import java.util.Objects;

/** Priced catalog entry maintained by a Manager. */
public class ServiceItem {
    private final String code;
    private String description;
    private BigDecimal unitPrice;

    public ServiceItem(String code, String description, BigDecimal unitPrice) {
        this.code = Objects.requireNonNull(code);
        this.description = description != null ? description : "";
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
