package com.bazaarready.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Something the business offers: an item, a service, a class, a route, a piece of work. The price
 * is optional and is stored in the currency's smallest unit (paise, cents) so there is no rounding.
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

    private String category;

    @Column(name = "price_paise")
    private Long pricePaise;

    /** Shown instead of, or beside, the price: "on request", "from", "per month". */
    @Column(name = "price_note")
    private String priceNote;

    private String unit;

    private String note;

    /** For transport: where the route starts and ends, and when it runs. */
    private String origin;

    private String destination;

    private String schedule;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected Product() {
        // for JPA
    }

    public Product(UUID businessId, String name, int sortOrder) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Long getPricePaise() { return pricePaise; }
    public String getPriceNote() { return priceNote; }
    public String getUnit() { return unit; }
    public String getNote() { return note; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getSchedule() { return schedule; }
    public int getSortOrder() { return sortOrder; }

    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setPricePaise(Long pricePaise) { this.pricePaise = pricePaise; }
    public void setPriceNote(String priceNote) { this.priceNote = priceNote; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setNote(String note) { this.note = note; }
    public void setOrigin(String origin) { this.origin = origin; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
}
