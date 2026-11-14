package com.bazaarready.agent;

import com.bazaarready.agent.AgentDtos.ApprovalRequest;
import com.bazaarready.agent.AgentDtos.ReplyRequest;
import com.bazaarready.agent.AgentDtos.SessionView;
import com.bazaarready.agent.AgentDtos.StartRequest;
import com.bazaarready.claude.ClaudeException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/agent/sessions")
class AgentController {

    private final AgentService agent;

    AgentController(AgentService agent) {
        this.agent = agent;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SessionView start(@Valid @RequestBody StartRequest request) {
        return agent.start(request.description(), request.language());
    }

    @GetMapping("/{id}")
    SessionView view(@PathVariable UUID id) {
        return agent.view(id);
    }

    @PostMapping("/{id}/reply")
    SessionView reply(@PathVariable UUID id, @Valid @RequestBody ReplyRequest request) {
        return agent.reply(id, request.answer());
    }

    @PostMapping("/{id}/approval")
    SessionView approval(@PathVariable UUID id, @RequestBody ApprovalRequest request) {
        return agent.approve(id, request.approved());
    }

    @ExceptionHandler(ClaudeException.class)
    ProblemDetail claudeUnavailable(ClaudeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail wrongStep(IllegalStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
}
