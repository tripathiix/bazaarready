package com.bazaarready.agent;

import com.bazaarready.common.Languages;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public final class AgentDtos {

    private AgentDtos() {
    }

    public record StartRequest(
            @NotBlank @Size(max = 4000) String description,
            @Pattern(regexp = Languages.CODE_PATTERN) String language) {
    }

    public record UpdateRequest(
            @jakarta.validation.constraints.NotNull UUID ownerKey,
            @NotBlank @Size(max = 3000) String feedback,
            @Pattern(regexp = Languages.CODE_PATTERN) String language) {
    }

    public record ReplyRequest(@NotBlank @Size(max = 2000) String answer) {
    }

    public record ApprovalRequest(boolean approved) {
    }

    /** Everything the owner's screen needs to show the current state of a session. */
    /** One box in a question asked as a short form. kind: text, long, choice (with options) or email. */
    public record FieldView(String label, String kind, String placeholder, List<String> options) {
    }

    public record SessionView(
            UUID id,
            AgentSession.Status status,
            String question,
            String questionType,
            List<FieldView> fields,
            String approvalRequest,
            String finalMessage,
            UUID businessId,
            String shopPath,
            String ownerPath,
            boolean published,
            List<ActionView> actions) {
    }

    /**
     * One line of the activity log. The screen shows it in the owner's language from {@code tool},
     * {@code status} and {@code data}; {@code text} is an English fallback.
     */
    public record ActionView(int stepNo, String tool, String text, AgentAction.Status status, JsonNode data) {
    }
}
