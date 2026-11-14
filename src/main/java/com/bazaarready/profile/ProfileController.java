package com.bazaarready.profile;

import com.bazaarready.profile.ProfileDtos.ProfileRequest;
import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
class ProfileController {

    private final ProfileService profiles;

    ProfileController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProfileResponse create(@Valid @RequestBody ProfileRequest request) {
        return profiles.create(request);
    }

    @GetMapping("/{id}/profile")
    ProfileResponse get(@PathVariable UUID id) {
        return profiles.get(id);
    }

    @PutMapping("/{id}/profile")
    ProfileResponse update(@PathVariable UUID id, @Valid @RequestBody ProfileRequest request) {
        return profiles.update(id, request);
    }

    @PostMapping("/{id}/publish")
    ProfileResponse publish(@PathVariable UUID id) {
        return profiles.publish(id);
    }
}
