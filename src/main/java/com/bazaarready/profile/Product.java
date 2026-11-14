package com.bazaarready.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Something the business offers: an item, a service, a class, a piece of work. The price is
 * optional and is stored in paise so there is no rounding.
 */
@Entity
@Table(name = "product")
public class Product {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "price_paise")
    private Long pricePaise;

    /** Shown instead of, or beside, the price: "on request", "from", "per month". */
    @Column(name = "price_note")
    private String priceNote;

    private String unit;

    private String note;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected Product() {
        // for JPA
    }

    public Product(UUID businessId, String name, String description, Long pricePaise, String priceNote,
                   String unit, String note, int sortOrder) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.name = name;
        this.description = description;
        this.pricePaise = pricePaise;
        this.priceNote = priceNote;
        this.unit = unit;
        this.note = note;
        this.sortOrder = sortOrder;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getPricePaise() { return pricePaise; }
    public String getPriceNote() { return priceNote; }
    public String getUnit() { return unit; }
    public String getNote() { return note; }
    public int getSortOrder() { return sortOrder; }
}
