package com.bazaarready.profile;

import com.bazaarready.common.Languages;
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

/** Request and response shapes for the profile API. Prices travel as rupees and are stored as paise. */
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
            @Size(max = 4) String currency,
            @Size(max = 700) String about,
            @Size(max = 200) String location,
            @Size(max = 200) String hours,
            @Size(max = 60) String offeringsTitle,
            @Size(max = 60) String detailsTitle,
            @Pattern(regexp = Languages.CODE_PATTERN) String ownerLanguage,
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
            @DecimalMin("0") BigDecimal priceRupees,
            @Size(max = 40) String priceNote,
            @Size(max = 40) String unit,
            @Size(max = 200) String note) {
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
            String theme,
            String currency,
            String about,
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
            List<ProductResponse> products,
            List<FaqResponse> faqs) {
    }

    public record ProductResponse(UUID id, String name, String description, BigDecimal priceRupees,
                                  String priceNote, String unit, String note) {
    }

    public record FaqResponse(UUID id, String question, String answer, int timesAsked, Faq.Source source) {
    }
}
