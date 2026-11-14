package com.bazaarready.agent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public final class AgentDtos {

    private AgentDtos() {
    }

    public record StartRequest(@NotBlank @Size(max = 4000) String description) {
    }

    public record ReplyRequest(@NotBlank @Size(max = 2000) String answer) {
    }

    public record ApprovalRequest(boolean approved) {
    }

    /** Everything the owner's screen needs to show the current state of a session. */
    public record SessionView(
            UUID id,
            AgentSession.Status status,
            String question,
            String approvalRequest,
            String finalMessage,
            UUID businessId,
            String shopPath,
            boolean published,
            List<ActionView> actions) {
    }

    public record ActionView(int stepNo, String text, AgentAction.Status status) {
    }
}
