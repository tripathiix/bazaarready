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

    /**
     * Upload WhatsApp chat exports (.txt or .zip) or Instagram's "Download your information" export (.zip or
     * the message_N.json files). Files are read in memory and never stored.
     */
    @PostMapping("/whatsapp")
    @ResponseStatus(HttpStatus.CREATED)
    SessionView startFromChats(@org.springframework.web.bind.annotation.RequestParam("files") java.util.List<org.springframework.web.multipart.MultipartFile> files,
                               @org.springframework.web.bind.annotation.RequestParam(defaultValue = "en") String language,
                               @org.springframework.web.bind.annotation.RequestParam(defaultValue = "") String ownerName) throws java.io.IOException {
        if (files.isEmpty() || files.size() > 300) {
            throw new IllegalArgumentException("Upload between 1 and 300 chat files.");
        }
        java.util.List<byte[]> data = new java.util.ArrayList<>();
        java.util.List<String> names = new java.util.ArrayList<>();
        for (var file : files) {
            data.add(file.getBytes());
            names.add(file.getOriginalFilename());
        }
        // One upload can mix WhatsApp exports and Instagram downloads; each file is read the right way.
        java.util.List<byte[]> igData = new java.util.ArrayList<>(), waData = new java.util.ArrayList<>();
        java.util.List<String> igNames = new java.util.ArrayList<>(), waNames = new java.util.ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            boolean ig = com.bazaarready.intake.InstagramChat.isInstagram(data.get(i), names.get(i));
            (ig ? igData : waData).add(data.get(i));
            (ig ? igNames : waNames).add(names.get(i));
        }
        boolean instagram = !igData.isEmpty();
        var messages = new java.util.ArrayList<com.bazaarready.intake.WhatsAppChat.Message>();
        if (instagram) {
            messages.addAll(com.bazaarready.intake.InstagramChat.read(igData, igNames));
        }
        if (!waData.isEmpty()) {
            messages.addAll(com.bazaarready.intake.WhatsAppChat.parse(com.bazaarready.intake.WhatsAppChat.readFiles(waData, waNames)));
        }
        if (messages.size() < 3) {
            throw new IllegalArgumentException(instagram
                    ? "We couldn't find messages in this Instagram download. Download your information again, choose Messages, and upload the zip."
                    : "These files don't look like WhatsApp chat exports. In WhatsApp, open a chat, choose Export chat, then Without media.");
        }
        String language2 = language.matches(com.bazaarready.common.Languages.CODE_PATTERN) ? language : "en";
        return agent.startFromChats(com.bazaarready.intake.WhatsAppChat.clean(messages, ownerName), language2,
                instagram && waData.isEmpty() ? "Instagram" : instagram ? "WhatsApp and Instagram" : "WhatsApp");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badUpload(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(java.io.IOException.class)
    ProblemDetail unreadable(java.io.IOException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The files could not be read: " + e.getMessage());
    }

    /** Change an existing site from the owner's feedback. */
    @PostMapping("/update")
    @ResponseStatus(HttpStatus.CREATED)
    SessionView update(@Valid @RequestBody AgentDtos.UpdateRequest request) {
        return agent.startUpdate(request.ownerKey(), request.feedback(), request.language());
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
