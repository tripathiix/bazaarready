package com.bazaarready.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** One small business or creator. Everything its site shows is derived from this profile. */
@Entity
@Table(name = "business")
public class Business {

    public enum Source { DESCRIPTION, WHATSAPP, PHOTO }

    public enum Status { DRAFT, PUBLISHED }

    /** Accepted site kinds and colour themes, for request validation. */
    public static final String KIND_PATTERN = "shop|food|services|classes|transport|portfolio|events|organisation|other";
    public static final String LAYOUT_PATTERN = "classic|split|boutique|magazine|bento|noir|mart";
    public static final String THEME_PATTERN = "marigold|indigo|forest|rose|ocean|slate";

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String tagline;

    @Column(nullable = false)
    private String kind;

    @Column(nullable = false)
    private String theme;

    /** Page layout: classic, split, boutique or magazine. */
    @Column(nullable = false)
    private String layout;

    /** Currency symbol shown before prices, such as ₹, $, € or £. */
    @Column(nullable = false)
    private String currency;

    private String about;

    /** Extra page sections as JSON: [{title, body, style}]. */
    private String sections;

    /** Stock photo search words for the site's banner. */
    @Column(name = "hero_query")
    private String heroQuery;

    private String location;

    private String hours;

    @Column(name = "offerings_title")
    private String offeringsTitle;

    @Column(name = "details_title")
    private String detailsTitle;

    @Column(name = "owner_language", nullable = false)
    private String ownerLanguage;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "delivery_notes")
    private String deliveryNotes;

    @Column(name = "notice_days")
    private Integer noticeDays;

    @Column(name = "reply_style")
    private String replyStyle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /** Makes up the owner's private link to orders and messages. Never shown on the public site. */
    @Column(name = "owner_key", nullable = false)
    private UUID ownerKey;

    @Column(name = "published_at")
    private Instant publishedAt;

    /** A built-in example site for the Templates gallery: never listed, and its forms do not submit. */
    @Column(nullable = false)
    private boolean sample;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Business() {
        // for JPA
    }

    public Business(String name, String slug, Source source) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.slug = slug;
        this.source = source;
        this.ownerLanguage = "en";
        this.kind = "shop";
        this.theme = "marigold";
        this.layout = "classic";
        this.currency = "₹";
        this.status = Status.DRAFT;
        this.ownerKey = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public void publish() {
        this.status = Status.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getTagline() { return tagline; }
    public String getKind() { return kind; }
    public String getTheme() { return theme; }
    public String getLayout() { return layout; }
    public String getCurrency() { return currency; }
    public String getAbout() { return about; }
    public String getSections() { return sections; }
    public String getHeroQuery() { return heroQuery; }
    public String getLocation() { return location; }
    public String getHours() { return hours; }
    public String getOfferingsTitle() { return offeringsTitle; }
    public String getDetailsTitle() { return detailsTitle; }
    public String getOwnerLanguage() { return ownerLanguage; }
    public String getWhatsappNumber() { return whatsappNumber; }
    public String getDeliveryNotes() { return deliveryNotes; }
    public Integer getNoticeDays() { return noticeDays; }
    public String getReplyStyle() { return replyStyle; }
    public Source getSource() { return source; }
    public Status getStatus() { return status; }
    public UUID getOwnerKey() { return ownerKey; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isSample() { return sample; }

    public void setName(String name) { this.name = name; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public void setKind(String kind) { this.kind = kind; }
    public void setTheme(String theme) { this.theme = theme; }
    public void setLayout(String layout) { this.layout = layout; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setAbout(String about) { this.about = about; }
    public void setSections(String sections) { this.sections = sections; }
    public void setHeroQuery(String heroQuery) { this.heroQuery = heroQuery; }
    public void setLocation(String location) { this.location = location; }
    public void setHours(String hours) { this.hours = hours; }
    public void setOfferingsTitle(String offeringsTitle) { this.offeringsTitle = offeringsTitle; }
    public void setDetailsTitle(String detailsTitle) { this.detailsTitle = detailsTitle; }
    public void setOwnerLanguage(String ownerLanguage) { this.ownerLanguage = ownerLanguage; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
    public void setNoticeDays(Integer noticeDays) { this.noticeDays = noticeDays; }
    public void setReplyStyle(String replyStyle) { this.replyStyle = replyStyle; }
}
