package com.bazaarready.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** A question customers ask, with the owner's answer. */
@Entity
@Table(name = "faq")
public class Faq {

    /** Where the question came from: past chats, the owner, or a fix after a test. */
    public enum Source { CHAT, OWNER, FIX }

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    @Column(name = "times_asked", nullable = false)
    private int timesAsked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected Faq() {
        // for JPA
    }

    public Faq(UUID businessId, String question, String answer, int timesAsked, Source source, int sortOrder) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.question = question;
        this.answer = answer;
        this.timesAsked = timesAsked;
        this.source = source;
        this.sortOrder = sortOrder;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public int getTimesAsked() { return timesAsked; }
    public Source getSource() { return source; }
    public int getSortOrder() { return sortOrder; }
}
