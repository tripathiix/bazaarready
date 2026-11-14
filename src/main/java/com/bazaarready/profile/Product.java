package com.bazaarready.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Something the business sells. The price is stored in paise so there is no rounding. */
@Entity
@Table(name = "product")
public class Product {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_paise", nullable = false)
    private long pricePaise;

    private String unit;

    private String note;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected Product() {
        // for JPA
    }

    public Product(UUID businessId, String name, long pricePaise, String unit, String note, int sortOrder) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.name = name;
        this.pricePaise = pricePaise;
        this.unit = unit;
        this.note = note;
        this.sortOrder = sortOrder;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public String getName() { return name; }
    public long getPricePaise() { return pricePaise; }
    public String getUnit() { return unit; }
    public String getNote() { return note; }
    public int getSortOrder() { return sortOrder; }
}
