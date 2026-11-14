package com.bazaarready.agent;

import com.bazaarready.agent.AgentDtos.ActionView;
import com.bazaarready.agent.AgentDtos.SessionView;
import com.bazaarready.claude.ClaudeClient;
import com.bazaarready.claude.ClaudeException;
import com.bazaarready.common.Languages;
import com.bazaarready.common.NotFoundException;
import com.bazaarready.profile.Business;
import com.bazaarready.profile.ProfileDtos.ProfileRequest;
import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import com.bazaarready.profile.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The launch agent. Claude is given a goal and tools and decides the steps; this class runs the
 * tools, pauses for the owner where a tool needs an answer or approval, and logs every action.
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    /** Hard limit on Claude calls per session, so a confused run cannot loop forever. */
    private static final int MAX_MODEL_CALLS = 14;
    private static final int MAX_TOKENS = 2000;

    private final ClaudeClient claude;
    private final ProfileService profiles;
    private final AgentSessionRepository sessions;
    private final AgentActionRepository actions;
    private final ObjectMapper json;
    private final Validator validator;
    private final ArrayNode tools;

    AgentService(ClaudeClient claude, ProfileService profiles, AgentSessionRepository sessions,
                 AgentActionRepository actions, ObjectMapper json, Validator validator) throws JsonProcessingException {
        this.claude = claude;
        this.profiles = profiles;
        this.sessions = sessions;
        this.actions = actions;
        this.json = json;
        this.validator = validator;
        this.tools = (ArrayNode) json.readTree(AgentPrompts.TOOLS);
    }

    public SessionView start(String description, String language) {
        if (!claude.isConfigured()) {
            throw new ClaudeException("The Claude API key is not set. Add ANTHROPIC_API_KEY to the .env file and restart.");
        }
        ArrayNode messages = json.createArrayNode();
        messages.addObject().put("role", "user").put("content", description.trim());
        AgentSession session = sessions.save(new AgentSession(messages.toString(), Languages.of(language).code()));
        logAction(session, "start", "Read your description", AgentAction.Status.DONE, null);
        return run(session, messages);
    }

    public SessionView reply(UUID sessionId, String answer) {
        AgentSession session = find(sessionId);
        requireStatus(session, AgentSession.Status.WAITING_OWNER);
        ArrayNode messages = readMessages(session);
        addToolResult(messages, session.getPendingToolUseId(), "The owner answered: " + answer.trim(), false);
        ObjectNode asked = json.createObjectNode();
        asked.put("question", session.getPendingQuestion());
        asked.put("answer", answer.trim());
        resolveLastAction(session, AgentAction.Status.DONE,
                "Asked: " + session.getPendingQuestion() + " You answered: " + answer.trim(), asked);
        session.resume();
        return run(session, messages);
    }

    public SessionView approve(UUID sessionId, boolean approved) {
        AgentSession session = find(sessionId);
        requireStatus(session, AgentSession.Status.WAITING_APPROVAL);
        ArrayNode messages = readMessages(session);
        if (approved) {
            ProfileResponse shop = profiles.publish(session.getBusinessId());
            addToolResult(messages, session.getPendingToolUseId(),
                    "The owner approved. The shop is published at " + shop.shopPath(), false);
            resolveLastAction(session, AgentAction.Status.APPROVED, "Published your shop", null);
        } else {
            addToolResult(messages, session.getPendingToolUseId(),
                    "The owner declined to publish for now. Do not ask again; finish with a short message.", false);
            resolveLastAction(session, AgentAction.Status.DECLINED, "You chose not to publish yet", null);
        }
        session.resume();
        return run(session, messages);
    }

    public SessionView view(UUID sessionId) {
        return toView(find(sessionId));
    }

    /** Calls Claude repeatedly, running the tool it picks each time, until it finishes or must wait for the owner. */
    private SessionView run(AgentSession session, ArrayNode messages) {
        String system = AgentPrompts.SYSTEM + AgentPrompts.languageRule(Languages.of(session.getLanguage()));
        try {
            while (session.getStatus() == AgentSession.Status.RUNNING) {
                if (session.getModelCalls() >= MAX_MODEL_CALLS) {
                    session.finish(AgentSession.Status.FAILED, "I could not finish in a reasonable number of steps. Please start again.");
                    break;
                }
                session.countModelCall();
                JsonNode response = claude.createMessage(ClaudeClient.AGENT_MODEL, MAX_TOKENS, system, tools, messages);
                JsonNode content = response.path("content");
                messages.addObject().put("role", "assistant").set("content", content);

                JsonNode toolUse = firstToolUse(content);
                if (toolUse == null) {
                    session.finish(AgentSession.Status.DONE, textOf(content));
                    break;
                }
                handleTool(session, messages, toolUse);
            }
        } catch (ClaudeException e) {
            log.warn("Agent session {} failed: {}", session.getId(), e.getMessage());
            session.finish(AgentSession.Status.FAILED, e.getMessage());
            logAction(session, "error", "Stopped: " + e.getMessage(), AgentAction.Status.FAILED, null);
        }
        session.setMessages(messages.toString());
        session.touch();
        sessions.save(session);
        return toView(session);
    }

    private void handleTool(AgentSession session, ArrayNode messages, JsonNode toolUse) {
        String toolUseId = toolUse.path("id").asText();
        String name = toolUse.path("name").asText();
        JsonNode input = toolUse.path("input");

        switch (name) {
            case "ask_owner" -> {
                String question = input.path("question").asText("").trim();
                session.pause(AgentSession.Status.WAITING_OWNER, toolUseId, name, question);
                session.setPendingAnswerType("phone".equals(input.path("answerType").asText()) ? "phone" : "text");
                logAction(session, name, "Asked: " + question, AgentAction.Status.WAITING_OWNER,
                        json.createObjectNode().put("question", question));
            }
            case "save_profile" -> saveProfile(session, messages, toolUseId, input);
            case "publish_shop" -> {
                if (session.getBusinessId() == null) {
                    addToolResult(messages, toolUseId, "There is no saved profile yet. Call save_profile first.", true);
                    logAction(session, name, "Tried to publish before saving the details", AgentAction.Status.FAILED, null);
                } else {
                    session.pause(AgentSession.Status.WAITING_APPROVAL, toolUseId, name, null);
                    logAction(session, name, "Ready to publish your shop. Waiting for your approval", AgentAction.Status.WAITING_APPROVAL, null);
                }
            }
            default -> {
                addToolResult(messages, toolUseId, "Unknown tool: " + name, true);
                logAction(session, name, "Tried a tool that does not exist", AgentAction.Status.FAILED, null);
            }
        }
    }

    private void saveProfile(AgentSession session, ArrayNode messages, String toolUseId, JsonNode input) {
        ProfileRequest request;
        try {
            ObjectNode copy = input.deepCopy();
            copy.put("source", Business.Source.DESCRIPTION.name());
            request = json.treeToValue(copy, ProfileRequest.class);
        } catch (JsonProcessingException | IllegalArgumentException | ClassCastException e) {
            addToolResult(messages, toolUseId, "The profile could not be read: " + e.getMessage(), true);
            logAction(session, "save_profile", "Could not read the details; trying again", AgentAction.Status.FAILED, null);
            return;
        }

        Set<ConstraintViolation<ProfileRequest>> problems = validator.validate(request);
        if (!problems.isEmpty()) {
            String list = problems.stream()
                    .map(p -> p.getPropertyPath() + " " + p.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            addToolResult(messages, toolUseId, "Not saved. Fix these and call save_profile again: " + list, true);
            logAction(session, "save_profile", "Some details were not valid; correcting them", AgentAction.Status.FAILED, null);
            return;
        }

        ProfileResponse saved = session.getBusinessId() == null
                ? profiles.create(request)
                : profiles.update(session.getBusinessId(), request);
        session.setBusinessId(saved.id());
        addToolResult(messages, toolUseId, "Saved. The shop preview is at " + saved.shopPath()
                + ". It is not published yet.", false);
        logAction(session, "save_profile", "Saved your business details: " + saved.products().size()
                + " item(s), " + saved.faqs().size() + " question(s)", AgentAction.Status.DONE,
                json.createObjectNode().put("items", saved.products().size()).put("faqs", saved.faqs().size()));
    }

    private void addToolResult(ArrayNode messages, String toolUseId, String text, boolean isError) {
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        ObjectNode result = message.putArray("content").addObject();
        result.put("type", "tool_result");
        result.put("tool_use_id", toolUseId);
        result.put("content", text);
        if (isError) {
            result.put("is_error", true);
        }
    }

    private static JsonNode firstToolUse(JsonNode content) {
        for (JsonNode block : content) {
            if ("tool_use".equals(block.path("type").asText())) {
                return block;
            }
        }
        return null;
    }

    private static String textOf(JsonNode content) {
        StringBuilder text = new StringBuilder();
        for (JsonNode block : content) {
            if ("text".equals(block.path("type").asText())) {
                text.append(block.path("text").asText());
            }
        }
        return text.toString().trim();
    }

    private void logAction(AgentSession session, String tool, String summary, AgentAction.Status status, ObjectNode data) {
        int step = actions.countBySessionId(session.getId()) + 1;
        actions.save(new AgentAction(session.getId(), session.getBusinessId(), step, tool, summary, status,
                data == null ? null : data.toString()));
    }

    private void resolveLastAction(AgentSession session, AgentAction.Status status, String summary, ObjectNode data) {
        actions.findFirstBySessionIdOrderByStepNoDesc(session.getId()).ifPresent(action -> {
            action.resolve(status, summary, data == null ? null : data.toString());
            actions.save(action);
        });
    }

    private JsonNode readData(String data) {
        if (data == null) {
            return null;
        }
        try {
            return json.readTree(data);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private ArrayNode readMessages(AgentSession session) {
        try {
            return (ArrayNode) json.readTree(session.getMessages());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored agent messages are not valid JSON", e);
        }
    }

    private AgentSession find(UUID id) {
        return sessions.findById(id).orElseThrow(() -> new NotFoundException("Session not found"));
    }

    private static void requireStatus(AgentSession session, AgentSession.Status expected) {
        if (session.getStatus() != expected) {
            throw new IllegalStateException("This step is not available right now.");
        }
    }

    private SessionView toView(AgentSession session) {
        List<ActionView> log = actions.findBySessionIdOrderByStepNo(session.getId()).stream()
                .map(a -> new ActionView(a.getStepNo(), a.getTool(), a.getResultSummary(), a.getStatus(), readData(a.getData())))
                .toList();
        String shopPath = null;
        boolean published = false;
        if (session.getBusinessId() != null) {
            ProfileResponse shop = profiles.get(session.getBusinessId());
            shopPath = shop.shopPath();
            published = shop.status() == Business.Status.PUBLISHED;
        }
        String approval = session.getStatus() == AgentSession.Status.WAITING_APPROVAL
                ? "Publish your shop so customers can open it?"
                : null;
        return new SessionView(session.getId(), session.getStatus(), session.getPendingQuestion(), session.getPendingAnswerType(), approval,
                session.getFinalMessage(), session.getBusinessId(), shopPath, published, log);
    }
}
