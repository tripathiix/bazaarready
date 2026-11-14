package com.bazaarready.agent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** One conversation between an owner and the launch agent, including where it is paused. */
@Entity
@Table(name = "agent_session")
public class AgentSession {

    public enum Status { RUNNING, WAITING_OWNER, WAITING_APPROVAL, DONE, FAILED }

    @Id
    private UUID id;

    @Column(name = "business_id")
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /** The Claude message history as a JSON array. */
    @Column(nullable = false)
    private String messages;

    @Column(name = "pending_tool_use_id")
    private String pendingToolUseId;

    @Column(name = "pending_tool")
    private String pendingTool;

    @Column(name = "pending_question")
    private String pendingQuestion;

    @Column(name = "final_message")
    private String finalMessage;

    @Column(name = "model_calls", nullable = false)
    private int modelCalls;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AgentSession() {
        // for JPA
    }

    public AgentSession(String messages) {
        this.id = UUID.randomUUID();
        this.status = Status.RUNNING;
        this.messages = messages;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void pause(Status status, String toolUseId, String tool, String question) {
        this.status = status;
        this.pendingToolUseId = toolUseId;
        this.pendingTool = tool;
        this.pendingQuestion = question;
    }

    public void resume() {
        this.status = Status.RUNNING;
        this.pendingToolUseId = null;
        this.pendingTool = null;
        this.pendingQuestion = null;
    }

    public void finish(Status status, String finalMessage) {
        resume();
        this.status = status;
        this.finalMessage = finalMessage;
    }

    public void countModelCall() {
        this.modelCalls++;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public Status getStatus() { return status; }
    public String getMessages() { return messages; }
    public String getPendingToolUseId() { return pendingToolUseId; }
    public String getPendingTool() { return pendingTool; }
    public String getPendingQuestion() { return pendingQuestion; }
    public String getFinalMessage() { return finalMessage; }
    public int getModelCalls() { return modelCalls; }

    public void setBusinessId(UUID businessId) { this.businessId = businessId; }
    public void setMessages(String messages) { this.messages = messages; }
}
