package com.bazaarready.orders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** A message sent through a site's contact form. */
@Entity
@Table(name = "enquiry")
public class Enquiry {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Enquiry() {
        // for JPA
    }

    public Enquiry(UUID businessId, String name, String contact, String message) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.name = name;
        this.contact = contact;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
