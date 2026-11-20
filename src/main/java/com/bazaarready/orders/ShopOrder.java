package com.bazaarready.orders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** An order placed from a shop's cart, or a booking request for a service. */
@Entity
@Table(name = "shop_order")
public class ShopOrder {

    public enum Kind { ORDER, BOOKING }

    /**
     * NEW: placed, to be paid on delivery or as agreed. PAY_CHECK: the customer says they paid by UPI and the
     * owner should check. PAID: the owner confirmed the payment. PAID_DEMO: older orders from the demo page.
     */
    public enum Status { NEW, PAY_CHECK, PAID, PAID_DEMO }

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    private String address;

    private String note;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Column(name = "booking_time")
    private String bookingTime;

    /** The lines of the order as JSON: name, unit price and quantity at the time of ordering. */
    @Column(nullable = false)
    private String items;

    @Column(name = "total_paise", nullable = false)
    private long totalPaise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** upi, cod or whatsapp, once the customer has chosen. */
    @Column(name = "payment_method")
    private String paymentMethod;

    /** The UPI transaction reference the customer gave, if any. */
    @Column(name = "payment_ref")
    private String paymentRef;

    protected ShopOrder() {
        // for JPA
    }

    public ShopOrder(UUID businessId, Kind kind, String customerName, String customerPhone, String items, long totalPaise) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.kind = kind;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.items = items;
        this.totalPaise = totalPaise;
        this.status = Status.NEW;
        this.createdAt = Instant.now();
    }

    public void markPaidDemo() {
        this.status = Status.PAID_DEMO;
    }

    /** The customer's choice at checkout. A UPI payment waits for the owner to check it. */
    public void choosePayment(String method, String reference) {
        this.paymentMethod = method;
        this.paymentRef = reference;
        this.status = "upi".equals(method) ? Status.PAY_CHECK : Status.NEW;
    }

    /** The owner has seen the money arrive. */
    public void markPaid() {
        this.status = Status.PAID;
    }

    /** A short number a customer can read out: the first eight characters of the id. */
    public String getNumber() {
        return id.toString().substring(0, 8).toUpperCase();
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public Kind getKind() { return kind; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getAddress() { return address; }
    public String getNote() { return note; }
    public LocalDate getBookingDate() { return bookingDate; }
    public String getBookingTime() { return bookingTime; }
    public String getItems() { return items; }
    public long getTotalPaise() { return totalPaise; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentRef() { return paymentRef; }

    public void setAddress(String address) { this.address = address; }
    public void setNote(String note) { this.note = note; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }
}
