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

    @Column(name = "pending_answer_type")
    private String pendingAnswerType;

    /** JSON list of form fields when the question is asked as a short form. */
    @Column(name = "pending_fields")
    private String pendingFields;

    @Column(name = "final_message")
    private String finalMessage;

    /** Language code the owner picked on the start screen; see Languages. */
    private String language;

    /** DESCRIPTION or WHATSAPP: what the owner gave the agent to start from. */
    @Column(nullable = false)
    private String source = "DESCRIPTION";

    @Column(name = "model_calls", nullable = false)
    private int modelCalls;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AgentSession() {
        // for JPA
    }

    public AgentSession(String messages, String language) {
        this.id = UUID.randomUUID();
        this.language = language;
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
        this.pendingAnswerType = null;
        this.pendingFields = null;
    }

    public void resume() {
        this.status = Status.RUNNING;
        this.pendingToolUseId = null;
        this.pendingTool = null;
        this.pendingQuestion = null;
        this.pendingAnswerType = null;
        this.pendingFields = null;
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
    public String getPendingAnswerType() { return pendingAnswerType; }
    public String getFinalMessage() { return finalMessage; }
    public int getModelCalls() { return modelCalls; }
    public String getLanguage() { return language; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public void setBusinessId(UUID businessId) { this.businessId = businessId; }
    public void setPendingAnswerType(String pendingAnswerType) { this.pendingAnswerType = pendingAnswerType; }
    public String getPendingFields() { return pendingFields; }
    public void setPendingFields(String pendingFields) { this.pendingFields = pendingFields; }
    public void setMessages(String messages) { this.messages = messages; }
}
