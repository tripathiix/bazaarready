package com.bazaarready.profile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Request and response shapes for the profile API. Prices travel in whole currency units. */
public final class ProfileDtos {

    private ProfileDtos() {
    }

    public record ProfileRequest(
            @NotBlank @Size(max = 80) String name,
            /** The name in English letters, used for the web address when the name is in another script. */
            @Size(max = 60) String addressName,
            @Size(max = 160) String tagline,
            @Pattern(regexp = Business.KIND_PATTERN) String kind,
            @Pattern(regexp = Business.THEME_PATTERN) String theme,
            @Pattern(regexp = Business.LAYOUT_PATTERN) String layout,
            @Size(max = 4) String currency,
            @Size(max = 700) String about,
            /** Stock photo search words for the banner, in English. */
            @Size(max = 80) String heroImageQuery,
            @Size(max = 200) String location,
            @Size(max = 200) String hours,
            @Size(max = 60) String offeringsTitle,
            @Size(max = 60) String detailsTitle,
            /** Any language code: en, hi, es, ar, pt-BR. Sites are not limited to the screen's languages. */
            @Pattern(regexp = "[A-Za-z]{2,8}(-[A-Za-z0-9]{2,8})?") String ownerLanguage,
            @Pattern(regexp = "\\+?[0-9]{10,15}", message = "must be 10 to 15 digits") String whatsappNumber,
            @Size(max = 500) String deliveryNotes,
            @Min(0) @Max(60) Integer noticeDays,
            Business.Source source,
            @Valid @Size(max = 100) List<ProductRequest> products,
            @Valid @Size(max = 100) List<FaqRequest> faqs) {
    }

    public record ProductRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 300) String description,
            @Size(max = 40) String category,
            @DecimalMin("0") BigDecimal priceRupees,
            @Size(max = 40) String priceNote,
            @Size(max = 40) String unit,
            @Size(max = 200) String note,
            @Size(max = 80) String origin,
            @Size(max = 80) String destination,
            @Size(max = 120) String schedule,
            @Size(max = 80) String imageQuery) {
    }

    public record FaqRequest(
            @NotBlank @Size(max = 300) String question,
            @NotBlank @Size(max = 600) String answer,
            @Min(0) Integer timesAsked) {
    }

    public record ProfileResponse(
            UUID id,
            String name,
            String slug,
            String tagline,
            String kind,
            /** How the site behaves: STORE (cart and checkout), BOOKING (booking requests) or SHOWCASE. */
            String style,
            String theme,
            String layout,
            String currency,
            String about,
            String heroImageQuery,
            String location,
            String hours,
            String offeringsTitle,
            String detailsTitle,
            String ownerLanguage,
            String whatsappNumber,
            String deliveryNotes,
            Integer noticeDays,
            Business.Source source,
            Business.Status status,
            Instant publishedAt,
            String shopPath,
            String ownerPath,
            List<ProductResponse> products,
            List<FaqResponse> faqs) {
    }

    public record ProductResponse(UUID id, String name, String description, String category,
                                  BigDecimal priceRupees, String priceNote, String unit, String note,
                                  String origin, String destination, String schedule,
                                  String imageQuery) {
    }

    public record FaqResponse(UUID id, String question, String answer, int timesAsked, Faq.Source source) {
    }
}
