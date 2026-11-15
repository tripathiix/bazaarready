package com.bazaarready.profile;

import com.bazaarready.common.NotFoundException;
import com.bazaarready.profile.ProfileDtos.FaqRequest;
import com.bazaarready.profile.ProfileDtos.FaqResponse;
import com.bazaarready.profile.ProfileDtos.ProductRequest;
import com.bazaarready.profile.ProfileDtos.ProductResponse;
import com.bazaarready.profile.ProfileDtos.ProfileRequest;
import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * The single place that reads and writes a business profile. The site pages, the tests and the
 * agent's tools all go through here, so a fix is always a change to the profile and never a
 * hand edit to a page.
 */
@Service
public class ProfileService {

    private static final BigDecimal MINOR_PER_UNIT = BigDecimal.valueOf(100);

    private final BusinessRepository businesses;
    private final ProductRepository products;
    private final FaqRepository faqs;

    ProfileService(BusinessRepository businesses, ProductRepository products, FaqRepository faqs) {
        this.businesses = businesses;
        this.products = products;
        this.faqs = faqs;
    }

    @Transactional
    public ProfileResponse create(ProfileRequest request) {
        Business.Source source = request.source() != null ? request.source() : Business.Source.DESCRIPTION;
        String slug = uniqueSlug(request.addressName(), request.name());
        Business business = new Business(request.name().trim(), slug, source);
        applyDetails(business, request);
        businesses.save(business);
        replaceItems(business.getId(), request);
        return toResponse(business);
    }

    @Transactional
    public ProfileResponse update(UUID id, ProfileRequest request) {
        Business business = find(id);
        business.setName(request.name().trim());
        applyDetails(business, request);
        replaceItems(id, request);
        return toResponse(business);
    }

    /** Adds one FAQ entry, for example after a test customer could not find an answer. */
    @Transactional
    public void addFaq(UUID id, String question, String answer) {
        find(id);
        int position = faqs.findByBusinessIdOrderBySortOrder(id).size();
        faqs.save(new Faq(id, question.trim(), answer.trim(), 0, Faq.Source.FIX, position));
    }

    @Transactional
    public ProfileResponse publish(UUID id) {
        Business business = find(id);
        business.publish();
        return toResponse(business);
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getBySlug(String slug) {
        return toResponse(businesses.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("No site at this address")));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByOwnerKey(UUID ownerKey) {
        return toResponse(businesses.findByOwnerKey(ownerKey)
                .orElseThrow(() -> new NotFoundException("This owner link is not valid")));
    }

    /** Whether a kind of business gets a cart, a booking form, or neither. */
    public static String styleOf(String kind) {
        return switch (kind) {
            case "shop", "food" -> "STORE";
            case "services", "classes", "transport", "events" -> "BOOKING";
            default -> "SHOWCASE";
        };
    }

    private Business find(UUID id) {
        return businesses.findById(id).orElseThrow(() -> new NotFoundException("Business not found"));
    }

    private void applyDetails(Business business, ProfileRequest request) {
        business.setTagline(blankToNull(request.tagline()));
        if (request.kind() != null) {
            business.setKind(request.kind());
        }
        if (request.theme() != null) {
            business.setTheme(request.theme());
        }
        if (request.layout() != null) {
            business.setLayout(request.layout());
        }
        if (blankToNull(request.currency()) != null) {
            business.setCurrency(request.currency().trim());
        }
        business.setAbout(blankToNull(request.about()));
        business.setHeroQuery(blankToNull(request.heroImageQuery()));
        business.setLocation(blankToNull(request.location()));
        business.setHours(blankToNull(request.hours()));
        business.setOfferingsTitle(blankToNull(request.offeringsTitle()));
        business.setDetailsTitle(blankToNull(request.detailsTitle()));
        if (request.ownerLanguage() != null) {
            business.setOwnerLanguage(request.ownerLanguage());
        }
        business.setWhatsappNumber(blankToNull(request.whatsappNumber()));
        business.setDeliveryNotes(blankToNull(request.deliveryNotes()));
        business.setReplyStyle(blankToNull(request.replyStyle()));
        business.setNoticeDays(request.noticeDays());
    }

    private void replaceItems(UUID businessId, ProfileRequest request) {
        products.deleteByBusinessId(businessId);
        faqs.deleteByBusinessId(businessId);
        products.flush();
        faqs.flush();

        List<ProductRequest> productRequests = request.products() != null ? request.products() : List.of();
        for (int i = 0; i < productRequests.size(); i++) {
            ProductRequest p = productRequests.get(i);
            Product product = new Product(businessId, p.name().trim(), i);
            product.setDescription(blankToNull(p.description()));
            product.setCategory(blankToNull(p.category()));
            product.setPricePaise(p.priceRupees() == null ? null : toMinor(p.priceRupees()));
            product.setPriceNote(blankToNull(p.priceNote()));
            product.setUnit(blankToNull(p.unit()));
            product.setNote(blankToNull(p.note()));
            product.setOrigin(blankToNull(p.origin()));
            product.setDestination(blankToNull(p.destination()));
            product.setSchedule(blankToNull(p.schedule()));
            product.setImageQuery(blankToNull(p.imageQuery()));
            products.save(product);
        }

        List<FaqRequest> faqRequests = request.faqs() != null ? request.faqs() : List.of();
        Faq.Source faqSource = request.source() == Business.Source.WHATSAPP ? Faq.Source.CHAT : Faq.Source.OWNER;
        for (int i = 0; i < faqRequests.size(); i++) {
            FaqRequest f = faqRequests.get(i);
            int timesAsked = f.timesAsked() != null ? f.timesAsked() : 0;
            faqs.save(new Faq(businessId, f.question().trim(), f.answer().trim(), timesAsked, faqSource, i));
        }
    }

    private ProfileResponse toResponse(Business b) {
        List<ProductResponse> productList = products.findByBusinessIdOrderBySortOrder(b.getId()).stream()
                .map(p -> new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getCategory(),
                        p.getPricePaise() == null ? null : toUnits(p.getPricePaise()),
                        p.getPriceNote(), p.getUnit(), p.getNote(), p.getOrigin(), p.getDestination(), p.getSchedule(), p.getImageQuery()))
                .toList();
        List<FaqResponse> faqList = faqs.findByBusinessIdOrderBySortOrder(b.getId()).stream()
                .map(f -> new FaqResponse(f.getId(), f.getQuestion(), f.getAnswer(), f.getTimesAsked(), f.getSource()))
                .toList();
        return new ProfileResponse(b.getId(), b.getName(), b.getSlug(), b.getTagline(), b.getKind(),
                styleOf(b.getKind()), b.getTheme(), b.getLayout(), b.getCurrency(), b.getAbout(), b.getHeroQuery(), b.getLocation(), b.getHours(),
                b.getOfferingsTitle(), b.getDetailsTitle(), b.getOwnerLanguage(), b.getWhatsappNumber(),
                b.getDeliveryNotes(), b.getNoticeDays(), b.getSource(), b.getStatus(), b.getPublishedAt(),
                "/s/" + b.getSlug(), "/owner/" + b.getOwnerKey(), productList, faqList);
    }

    /**
     * Builds the web address. "Sunita's Cakes" becomes "sunitas-cakes". A name in another script
     * (किसान डायरेक्ट) has no English letters, so the address comes from the English-letter name the
     * agent supplies ("kisan direct"). A number is added if the address is taken.
     */
    private String uniqueSlug(String addressName, String name) {
        String base = slugify(addressName);
        if (base.isEmpty()) {
            base = slugify(name);
        }
        if (base.isEmpty()) {
            base = "site";
        }
        String slug = base;
        int n = 2;
        while (businesses.existsBySlug(slug)) {
            slug = base + "-" + n++;
        }
        return slug;
    }

    private static String slugify(String text) {
        if (text == null) {
            return "";
        }
        String slug = Normalizer.normalize(text, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("['’]", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (slug.length() > 40) {
            slug = slug.substring(0, 40).replaceAll("-+$", "");
        }
        return slug;
    }

    /** Whole currency units to the smallest unit: 850 rupees becomes 85000 paise. */
    public static long toMinor(BigDecimal units) {
        return units.multiply(MINOR_PER_UNIT).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public static BigDecimal toUnits(long minor) {
        return BigDecimal.valueOf(minor).divide(MINOR_PER_UNIT, 2, RoundingMode.UNNECESSARY);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
