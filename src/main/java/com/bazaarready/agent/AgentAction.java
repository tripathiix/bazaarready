package com.bazaarready.agent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** One line in the activity log: something the agent did, or is waiting to do. */
@Entity
@Table(name = "agent_action")
public class AgentAction {

    public enum Status { DONE, WAITING_OWNER, WAITING_APPROVAL, APPROVED, DECLINED, FAILED }

    @Id
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "business_id")
    private UUID businessId;

    @Column(name = "step_no", nullable = false)
    private int stepNo;

    @Column(nullable = false)
    private String tool;

    @Column(name = "result_summary")
    private String resultSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AgentAction() {
        // for JPA
    }

    public AgentAction(UUID sessionId, UUID businessId, int stepNo, String tool, String resultSummary, Status status) {
        this.id = UUID.randomUUID();
        this.sessionId = sessionId;
        this.businessId = businessId;
        this.stepNo = stepNo;
        this.tool = tool;
        this.resultSummary = resultSummary;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public void resolve(Status status, String resultSummary) {
        this.status = status;
        this.resultSummary = resultSummary;
    }

    public UUID getId() { return id; }
    public int getStepNo() { return stepNo; }
    public String getTool() { return tool; }
    public String getResultSummary() { return resultSummary; }
    public Status getStatus() { return status; }
}
