package com.bazaarready.site;

import com.bazaarready.common.NotFoundException;
import com.bazaarready.orders.OrderService;
import com.bazaarready.orders.OrderService.Customer;
import com.bazaarready.orders.ShopOrder;
import com.bazaarready.profile.Business;
import com.bazaarready.profile.ProfileDtos.ProductResponse;
import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import com.bazaarready.profile.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The public pages of a generated site: home, catalogue, item, cart and checkout, booking, about
 * and contact. They are plain server-rendered HTML on purpose: that is what the AI test customers
 * read and what the accessibility checks inspect.
 */
@Controller
class SiteController {

    /** Heading and reading fonts per language; the same pairing as the owner's screens. */
    private static final Map<String, String[]> FONTS = Map.of(
            "hi", new String[] {"Baloo 2", "Anek Devanagari"},
            "mr", new String[] {"Baloo 2", "Anek Devanagari"},
            "kn", new String[] {"Baloo Tamma 2", "Anek Kannada"},
            "ta", new String[] {"Baloo Thambi 2", "Anek Tamil"},
            "te", new String[] {"Baloo Tammudu 2", "Anek Telugu"},
            "bn", new String[] {"Baloo Da 2", "Anek Bangla"},
            "gu", new String[] {"Baloo Bhai 2", "Anek Gujarati"});
    private static final String[] LATIN_FONTS = {"Baloo 2", "Anek Latin"};
    /** Sites in other scripts: one family that covers the script, for headings and text. */
    private static final Map<String, String[]> SCRIPT_FONTS = Map.ofEntries(
            Map.entry("ar", new String[] {"Noto Kufi Arabic", "Noto Naskh Arabic"}),
            Map.entry("fa", new String[] {"Noto Kufi Arabic", "Noto Naskh Arabic"}),
            Map.entry("ur", new String[] {"Noto Kufi Arabic", "Noto Naskh Arabic"}),
            Map.entry("he", new String[] {"Noto Sans Hebrew", "Noto Sans Hebrew"}),
            Map.entry("zh", new String[] {"Noto Sans SC", "Noto Sans SC"}),
            Map.entry("ja", new String[] {"Noto Sans JP", "Noto Sans JP"}),
            Map.entry("ko", new String[] {"Noto Sans KR", "Noto Sans KR"}),
            Map.entry("th", new String[] {"Noto Sans Thai", "Noto Sans Thai"}),
            Map.entry("ml", new String[] {"Baloo Chettan 2", "Anek Malayalam"}),
            Map.entry("pa", new String[] {"Baloo Paaji 2", "Anek Gurmukhi"}),
            Map.entry("or", new String[] {"Baloo Bhaina 2", "Anek Odia"}),
            Map.entry("as", new String[] {"Baloo Da 2", "Anek Bangla"}),
            Map.entry("ne", new String[] {"Baloo 2", "Anek Devanagari"}),
            Map.entry("si", new String[] {"Noto Sans Sinhala", "Noto Sans Sinhala"}),
            Map.entry("my", new String[] {"Noto Sans Myanmar", "Noto Sans Myanmar"}),
            Map.entry("km", new String[] {"Noto Sans Khmer", "Noto Sans Khmer"}),
            Map.entry("am", new String[] {"Noto Sans Ethiopic", "Noto Sans Ethiopic"}),
            Map.entry("ru", new String[] {"Noto Sans", "Noto Sans"}),
            Map.entry("uk", new String[] {"Noto Sans", "Noto Sans"}),
            Map.entry("el", new String[] {"Noto Sans", "Noto Sans"}),
            Map.entry("kk", new String[] {"Noto Sans", "Noto Sans"}),
            Map.entry("vi", new String[] {"Noto Sans", "Noto Sans"}));
    private static final List<String> RIGHT_TO_LEFT = List.of("ar", "he", "fa", "ur");
    private static final java.time.format.DateTimeFormatter PLACED_AT =
            java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(java.time.ZoneId.systemDefault());

    private final ProfileService profiles;
    private final SiteLabelService labelService;
    private final OrderService orders;
    private final ObjectMapper json;
    private final ImageService images;

    private final com.bazaarready.testing.TestRunService tests;
    private final com.bazaarready.guard.MessageCheckService checks;
    @org.springframework.beans.factory.annotation.Autowired
    private com.bazaarready.account.OwnerAccess owners;
    @org.springframework.beans.factory.annotation.Autowired
    private com.bazaarready.extras.ExtrasService extras;
    @org.springframework.beans.factory.annotation.Autowired
    private com.bazaarready.extras.HaggleService haggle;
    @org.springframework.beans.factory.annotation.Autowired
    private com.bazaarready.extras.OwnerExtrasController ownerExtras;

    SiteController(ProfileService profiles, SiteLabelService labelService, OrderService orders, ObjectMapper json,
                   ImageService images, com.bazaarready.testing.TestRunService tests,
                   com.bazaarready.guard.MessageCheckService checks) {
        this.tests = tests;
        this.checks = checks;
        this.images = images;
        this.profiles = profiles;
        this.labelService = labelService;
        this.orders = orders;
        this.json = json;
    }

    @GetMapping("/s/{slug}")
    String home(@PathVariable String slug, Model model, jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response) {
        ProfileResponse shop = common(slug, model, "home");
        model.addAttribute("featured", shop.products().stream().limit(6).toList());
        var ex = (com.bazaarready.extras.ExtrasService.Extras) model.getAttribute("ex");
        if (ex != null && ex.comingSoon() && !ex.priceOptions().isEmpty()) {
            model.addAttribute("testPrice", priceFor(ex, request, response, shop.slug()));
        }
        return "site/home";
    }

    /**
     * The coming-soon price test: each visitor is shown one of the owner's prices, chosen at random on the
     * first visit and kept in a cookie, so sign-ups can be compared per price.
     */
    private BigDecimal priceFor(com.bazaarready.extras.ExtrasService.Extras ex, jakarta.servlet.http.HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response, String slug) {
        String name = "pv-" + slug.replaceAll("[^a-z0-9-]", "");
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if (name.equals(c.getName())) {
                    try {
                        int i = Integer.parseInt(c.getValue());
                        if (i >= 0 && i < ex.priceOptions().size()) {
                            return ex.priceOptions().get(i);
                        }
                    } catch (NumberFormatException ignored) {
                        // pick again
                    }
                }
            }
        }
        int i = java.util.concurrent.ThreadLocalRandom.current().nextInt(ex.priceOptions().size());
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, String.valueOf(i));
        cookie.setPath("/s/" + slug);
        cookie.setMaxAge(60 * 60 * 24 * 90);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ex.priceOptions().get(i);
    }

    // ---- Coming soon: the waitlist ----

    @PostMapping("/s/{slug}/waitlist")
    String joinWaitlist(@PathVariable String slug, @RequestParam(defaultValue = "") String name,
                        @RequestParam(defaultValue = "") String contact, @RequestParam(required = false) String website,
                        Model model, jakarta.servlet.http.HttpServletRequest request,
                        jakarta.servlet.http.HttpServletResponse response) {
        String sampleStop = sampleStop(slug);
        if (sampleStop != null) {
            return sampleStop;
        }
        ProfileResponse shop = common(slug, model, "home");
        var ex = extras.get(shop.id());
        if (isBlank(name) || isBlank(contact)) {
            return "redirect:/s/" + slug + "?join=missing#waitlist";
        }
        if (isBlank(website)) {
            String seen = ex.priceOptions().isEmpty() ? null : priceFor(ex, request, response, shop.slug()).toPlainString();
            extras.addSignup(shop.id(), clip(name, 80), clip(contact, 120), seen);
        }
        model.addAttribute("doneKind", "waitlist");
        return "site/done";
    }

    // ---- Creator media kit ----

    @GetMapping("/s/{slug}/media-kit")
    String mediaKit(@PathVariable String slug, Model model) {
        ProfileResponse shop = common(slug, model, "kit");
        if (!extras.get(shop.id()).hasMediaKit()) {
            throw new NotFoundException("No media kit");
        }
        model.addAttribute("form", new HashMap<String, String>());
        return "site/kit";
    }

    @PostMapping("/s/{slug}/collab")
    String collab(@PathVariable String slug, @RequestParam(defaultValue = "") String brand,
                  @RequestParam(defaultValue = "") String contact, @RequestParam(defaultValue = "") String budget,
                  @RequestParam(defaultValue = "") String message, @RequestParam(required = false) String website, Model model) {
        String sampleStop = sampleStop(slug);
        if (sampleStop != null) {
            return sampleStop;
        }
        ProfileResponse shop = common(slug, model, "kit");
        if (isBlank(brand) || isBlank(contact) || isBlank(message)) {
            model.addAttribute("error", true);
            Map<String, String> form = new HashMap<>();
            form.put("brand", clip(brand, 80));
            form.put("contact", clip(contact, 120));
            form.put("budget", clip(budget, 60));
            form.put("message", clip(message, 2000));
            model.addAttribute("form", form);
            return "site/kit";
        }
        if (isBlank(website)) {
            String body = "[Collab enquiry]" + (isBlank(budget) ? "" : " Budget: " + clip(budget, 60) + ".") + "\n" + clip(message, 2000);
            var enquiry = orders.saveEnquiry(shop.id(), clip(brand, 80), clip(contact, 120), body);
            checks.checkEnquiry(shop.id(), enquiry.getId(), enquiry.getName(), enquiry.getContact(), enquiry.getMessage());
        }
        model.addAttribute("doneKind", "message");
        return "site/done";
    }

    @GetMapping("/s/{slug}/catalogue")
    String catalogue(@PathVariable String slug, Model model) {
        ProfileResponse shop = common(slug, model, "catalogue");
        model.addAttribute("categories", shop.products().stream()
                .map(ProductResponse::category).filter(Objects::nonNull).distinct().toList());
        model.addAttribute("hasRoutes", shop.products().stream().anyMatch(p -> p.origin() != null && p.destination() != null));
        return "site/catalogue";
    }

    @GetMapping("/s/{slug}/item/{itemId}")
    String item(@PathVariable String slug, @PathVariable UUID itemId, Model model) {
        ProfileResponse shop = common(slug, model, "catalogue");
        model.addAttribute("item", shop.products().stream().filter(p -> p.id().equals(itemId)).findFirst()
                .orElseThrow(() -> new NotFoundException("No such item")));
        return "site/item";
    }

    @GetMapping("/s/{slug}/about")
    String about(@PathVariable String slug, Model model) {
        common(slug, model, "about");
        return "site/about";
    }

    // ---- Cart, checkout and demo payment (shops) ----

    @GetMapping("/s/{slug}/cart")
    String cart(@PathVariable String slug, Model model) {
        ProfileResponse shop = common(slug, model, "cart");
        model.addAttribute("productsJson", productsJson(shop));
        return "site/cart";
    }

    /** Someone opening the checkout address directly is sent to the cart instead of an error page. */
    @GetMapping("/s/{slug}/checkout")
    String checkoutPage(@PathVariable String slug) {
        profiles.getBySlug(slug);
        return "redirect:/s/" + slug + "/cart";
    }

    @PostMapping("/s/{slug}/checkout")
    String checkout(@PathVariable String slug, @RequestParam String name, @RequestParam String phone,
                    @RequestParam String address, @RequestParam(required = false) String note,
                    @RequestParam(required = false) String deliveryDate,
                    @RequestParam String cart, @RequestParam(defaultValue = "") String deals, Model model) {
        String sampleStop = sampleStop(slug);
        if (sampleStop != null) {
            return sampleStop;
        }
        ProfileResponse shop = profiles.getBySlug(slug);
        LocalDate wanted = isBlank(deliveryDate) ? null : parseDate(deliveryDate);
        Map<String, String> errors = new HashMap<>();
        if (isBlank(name)) {
            errors.put("name", "err_required");
        }
        if (!looksLikePhone(phone)) {
            errors.put("phone", "err_phone");
        }
        if (isBlank(address)) {
            errors.put("address", "err_required");
        }
        if (!isBlank(deliveryDate) && (wanted == null || wanted.isBefore(LocalDate.now()))) {
            errors.put("deliveryDate", "err_date");
        }
        if (!errors.isEmpty()) {
            common(slug, model, "cart");
            model.addAttribute("productsJson", productsJson(shop));
            model.addAttribute("error", true);
            model.addAttribute("errors", errors);
            model.addAttribute("keptCart", cart.length() > 4000 ? "{}" : cart);
            Map<String, String> form = new HashMap<>();
            form.put("name", clip(name, 80));
            form.put("phone", clip(phone, 20));
            form.put("address", clip(address, 300));
            form.put("note", clip(note, 300));
            form.put("deliveryDate", clip(deliveryDate, 10));
            model.addAttribute("form", form);
            return "site/cart";
        }
        ShopOrder order;
        String orderNote = clip(note, 300);
        if (!extras.get(shop.id()).openNow()) {
            // Placed while the shutter was down: the owner sees it marked as a pre-order.
            orderNote = "Pre-order (placed while closed)" + (isBlank(orderNote) ? "" : ". " + orderNote);
        }
        try {
            order = orders.placeOrder(shop, parseCart(cart), haggle.redeem(shop, parseDeals(deals)),
                    new Customer(clip(name, 80), clip(phone, 20), clip(address, 300), orderNote), wanted);
        } catch (IllegalArgumentException e) {
            return "redirect:/s/" + slug + "/cart";
        }
        return "redirect:/s/" + slug + "/pay/" + order.getId();
    }

    @GetMapping("/s/{slug}/pay/{orderId}")
    String pay(@PathVariable String slug, @PathVariable UUID orderId, Model model) {
        ProfileResponse shop = common(slug, model, "cart");
        addOrder(model, shop, orders.find(shop.id(), orderId));
        return "site/pay";
    }

    @PostMapping("/s/{slug}/pay/{orderId}")
    String payDemo(@PathVariable String slug, @PathVariable UUID orderId, @RequestParam String method) {
        String sampleStop = sampleStop(slug);
        if (sampleStop != null) {
            return sampleStop;
        }
        ProfileResponse shop = profiles.getBySlug(slug);
        if ("demo".equals(method)) {
            orders.markPaidDemo(shop.id(), orderId);
        } else {
            orders.find(shop.id(), orderId);
        }
        return "redirect:/s/" + slug + "/order/" + orderId;
    }

    @GetMapping("/s/{slug}/order/{orderId}")
    String orderDone(@PathVariable String slug, @PathVariable UUID orderId, Model model) {
        ProfileResponse shop = common(slug, model, "cart");
        ShopOrder order = orders.find(shop.id(), orderId);
        addOrder(model, shop, order);
        model.addAttribute("doneKind", order.getKind() == ShopOrder.Kind.BOOKING ? "booking" : "order");
        return "site/done";
    }

    // ---- Booking requests (services, classes, transport, events) ----

    @GetMapping("/s/{slug}/book")
    String book(@PathVariable String slug, @RequestParam(required = false) UUID item, Model model) {
        common(slug, model, "book");
        model.addAttribute("selected", item);
        model.addAttribute("today", LocalDate.now().toString());
        return "site/book";
    }

    @PostMapping("/s/{slug}/book")
    String placeBooking(@PathVariable String slug, @RequestParam UUID item, @RequestParam String date,
                        @RequestParam(required = false) String time, @RequestParam String name,
                        @RequestParam String phone, @RequestParam(required = false) String note, Model model) {
        String sampleStop = sampleStop(slug);
        if (sampleStop != null) {
            return sampleStop;
        }
        ProfileResponse shop = profiles.getBySlug(slug);
        LocalDate day = parseDate(date);
        Map<String, String> errors = new HashMap<>();
        if (day == null || day.isBefore(LocalDate.now())) {
            errors.put("date", "err_date");
        }
        if (isBlank(name)) {
            errors.put("name", "err_required");
        }
        if (!looksLikePhone(phone)) {
            errors.put("phone", "err_phone");
        }
        if (!errors.isEmpty()) {
            common(slug, model, "book");
            model.addAttribute("selected", item);
            model.addAttribute("today", LocalDate.now().toString());
            model.addAttribute("error", true);
            model.addAttribute("errors", errors);
            Map<String, String> form = new HashMap<>();
            form.put("date", clip(date, 10));
            form.put("time", clip(time, 10));
            form.put("name", clip(name, 80));
            form.put("phone", clip(phone, 20));
            form.put("note", clip(note, 300));
            model.addAttribute("form", form);
            return "site/book";
        }
        ShopOrder booking = orders.placeBooking(shop, item, day, clip(time, 40),
                new Customer(clip(name, 80), clip(phone, 20), null, clip(note, 300)));
        return "redirect:/s/" + slug + "/order/" + booking.getId();
    }

    // ---- Contact form ----

    @GetMapping("/s/{slug}/contact")
    String contact(@PathVariable String slug, Model model) {
        common(slug, model, "contact");
        return "site/contact";
    }

    @PostMapping("/s/{slug}/contact")
    String sendMessage(@PathVariable String slug, @RequestParam String name, @RequestParam String contact,
                       @RequestParam String message, @RequestParam(required = false) String website, Model model) {
        String sampleStop = sampleStop(slug);
        if (sampleStop != null) {
            return sampleStop;
        }
        ProfileResponse shop = common(slug, model, "contact");
        // "website" is a hidden field people never see; only automated form-fillers complete it.
        if (isBlank(website)) {
            if (isBlank(name) || isBlank(contact) || isBlank(message)) {
                Map<String, String> errors = new HashMap<>();
                if (isBlank(name)) {
                    errors.put("name", "err_required");
                }
                if (isBlank(contact)) {
                    errors.put("contact", "err_required");
                }
                if (isBlank(message)) {
                    errors.put("message", "err_required");
                }
                Map<String, String> form = new HashMap<>();
                form.put("name", clip(name, 80));
                form.put("contact", clip(contact, 120));
                form.put("message", clip(message, 2000));
                model.addAttribute("error", true);
                model.addAttribute("errors", errors);
                model.addAttribute("form", form);
                return "site/contact";
            }
            var enquiry = orders.saveEnquiry(shop.id(), clip(name, 80), clip(contact, 120), clip(message, 2000));
            checks.checkEnquiry(shop.id(), enquiry.getId(), enquiry.getName(), enquiry.getContact(), enquiry.getMessage());
        }
        model.addAttribute("doneKind", "message");
        return "site/done";
    }

    // ---- The owner's private page ----

    @GetMapping("/owner/{ownerKey}")
    String owner(@PathVariable UUID ownerKey, Model model) {
        ProfileResponse shop = owners.open(ownerKey);
        common(shop, model, "owner");
        List<ShopOrder> all = orders.ordersFor(shop.id());
        Map<UUID, List<OrderService.OrderLine>> lines = new HashMap<>();
        all.forEach(order -> lines.put(order.getId(), orders.linesOf(order)));
        model.addAttribute("orders", all);
        model.addAttribute("lines", lines);
        Map<UUID, String> placed = new HashMap<>();
        all.forEach(order -> placed.put(order.getId(), PLACED_AT.format(order.getCreatedAt())));
        model.addAttribute("placed", placed);
        model.addAttribute("enquiries", orders.enquiriesFor(shop.id()));
        model.addAttribute("ownerKey", ownerKey);
        model.addAttribute("run", tests.latest(shop.id()));
        var allChecks = checks.recent(shop.id());
        model.addAttribute("pastedChecks", allChecks.stream().filter(c -> c.enquiryId() == null).toList());
        Map<UUID, com.bazaarready.guard.MessageCheckService.CheckView> byEnquiry = new HashMap<>();
        allChecks.stream().filter(c -> c.enquiryId() != null).forEach(c -> byEnquiry.putIfAbsent(c.enquiryId(), c));
        model.addAttribute("enquiryChecks", byEnquiry);
        model.addAttribute("checksRunning", allChecks.stream().anyMatch(c -> c.running()));
        ownerExtras.addTo(model, shop);
        return "site/owner";
    }

    /** The owner adds or replaces the photo for one offering, or for the banner when the name is empty. */
    @PostMapping("/owner/{ownerKey}/photo")
    String uploadPhoto(@PathVariable UUID ownerKey, @RequestParam(defaultValue = "") String item,
                       @RequestParam(required = false) MultipartFile file,
                       @RequestParam(defaultValue = "upload") String action) throws java.io.IOException {
        ProfileResponse shop = owners.open(ownerKey);
        boolean known = item.isEmpty() || shop.products().stream().anyMatch(p -> p.name().equals(item));
        if (!known) {
            return "redirect:/owner/" + ownerKey + "?photo=unknown";
        }
        if ("remove".equals(action)) {
            images.removeUpload(shop.id(), item);
            return "redirect:/owner/" + ownerKey + "?photo=removed#photos";
        }
        if (file == null || file.isEmpty() || file.getSize() > ImageService.MAX_UPLOAD) {
            return "redirect:/owner/" + ownerKey + "?photo=size#photos";
        }
        byte[] data = file.getBytes();
        String type = ImageService.detectType(data);
        if (type == null) {
            return "redirect:/owner/" + ownerKey + "?photo=type#photos";
        }
        images.saveUpload(shop.id(), item, type, data);
        return "redirect:/owner/" + ownerKey + "?photo=saved#photos";
    }

    // ---- Products, managed by the owner ----

    private static final int MAX_PRODUCTS = 60;

    @PostMapping("/owner/{ownerKey}/products")
    String addProduct(@PathVariable UUID ownerKey, @RequestParam(defaultValue = "") String name,
                      @RequestParam(defaultValue = "") String price, @RequestParam(defaultValue = "") String unit,
                      @RequestParam(defaultValue = "") String description,
                      @RequestParam(required = false) MultipartFile file) throws java.io.IOException {
        ProfileResponse shop = owners.open(ownerKey);
        ProfileService.ProductEdit edit = productEdit(name, price, unit, description);
        if (edit == null) {
            return "redirect:/owner/" + ownerKey + "?product=invalid#photos";
        }
        if (shop.products().size() >= MAX_PRODUCTS) {
            return "redirect:/owner/" + ownerKey + "?product=full#photos";
        }
        if (shop.products().stream().anyMatch(p -> p.name().equalsIgnoreCase(edit.name()))) {
            return "redirect:/owner/" + ownerKey + "?product=duplicate#photos";
        }
        profiles.addProduct(shop.id(), edit);
        String photo = savePhotoIfAny(shop.id(), edit.name(), file);
        return "redirect:/owner/" + ownerKey + "?product=" + (photo == null ? "added" : photo) + "#photos";
    }

    @PostMapping("/owner/{ownerKey}/products/{productId}")
    String editProduct(@PathVariable UUID ownerKey, @PathVariable UUID productId,
                       @RequestParam(defaultValue = "save") String action, @RequestParam(defaultValue = "") String name,
                       @RequestParam(defaultValue = "") String price, @RequestParam(defaultValue = "") String unit,
                       @RequestParam(defaultValue = "") String description) {
        ProfileResponse shop = owners.open(ownerKey);
        try {
            if ("remove".equals(action)) {
                String removed = profiles.removeProduct(shop.id(), productId);
                images.removeUpload(shop.id(), removed);
                return "redirect:/owner/" + ownerKey + "?product=removed#photos";
            }
            ProfileService.ProductEdit edit = productEdit(name, price, unit, description);
            if (edit == null) {
                return "redirect:/owner/" + ownerKey + "?product=invalid#photos";
            }
            boolean clash = shop.products().stream()
                    .anyMatch(p -> !p.id().equals(productId) && p.name().equalsIgnoreCase(edit.name()));
            if (clash) {
                return "redirect:/owner/" + ownerKey + "?product=duplicate#photos";
            }
            String oldName = profiles.updateProduct(shop.id(), productId, edit);
            images.renameUpload(shop.id(), oldName, edit.name());
        } catch (NotFoundException e) {
            // Already removed, or not this owner's: just show the page again.
        }
        return "redirect:/owner/" + ownerKey + "?product=saved#photos";
    }

    /** Reads the owner's typed product, or null when the name is missing or the price is not a number. */
    private static ProfileService.ProductEdit productEdit(String name, String price, String unit, String description) {
        String n = name.trim();
        if (n.isEmpty() || n.length() > 80 || unit.length() > 30 || description.length() > 600) {
            return null;
        }
        BigDecimal amount = null;
        String p = price.replaceAll("(?i)[\\s,₹$€£]|rs\\.?|inr", "");
        if (!p.matches("[0-9]*(\\.[0-9]{1,2})?")) {
            return null; // letters or other marks in the price
        }
        if (!p.isEmpty()) {
            try {
                amount = new BigDecimal(p);
            } catch (NumberFormatException e) {
                return null;
            }
            if (amount.signum() < 0 || amount.compareTo(BigDecimal.valueOf(10_000_000)) > 0) {
                return null;
            }
        }
        return new ProfileService.ProductEdit(n, amount, unit, description);
    }

    /** Saves a photo sent with a new product. Returns a problem code, or null when fine or none sent. */
    private String savePhotoIfAny(UUID businessId, String itemName, MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > ImageService.MAX_UPLOAD) {
            return "added-photo-size";
        }
        byte[] data = file.getBytes();
        String type = ImageService.detectType(data);
        if (type == null) {
            return "added-photo-type";
        }
        images.saveUpload(businessId, itemName, type, data);
        return null;
    }

    @GetMapping("/media/{id}")
    @ResponseBody
    ResponseEntity<byte[]> media(@PathVariable UUID id) {
        return images.upload(id)
                .map(u -> ResponseEntity.ok().contentType(MediaType.parseMediaType(u.contentType()))
                        .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(30)).cachePublic())
                        .header("X-Content-Type-Options", "nosniff").body(u.data()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String notFound() {
        return "shop-not-found";
    }

    // ---- Shared ----

    /** Sample sites are for looking only: their forms send the visitor back with a note instead of saving anything. */
    private String sampleStop(String slug) {
        ProfileResponse shop = profiles.getBySlug(slug);
        return profiles.isSample(shop.id()) ? "redirect:/s/" + slug + "?sample" : null;
    }

    private ProfileResponse common(String slug, Model model, String page) {
        ProfileResponse shop = profiles.getBySlug(slug);
        common(shop, model, page);
        model.addAttribute("today", LocalDate.now().toString());
        return shop;
    }

    private void common(ProfileResponse shop, Model model, String page) {
        String language = shop.ownerLanguage() == null ? "en" : shop.ownerLanguage().toLowerCase();
        SiteLabels labels = labelService.labelsFor(language);
        String layout = shop.layout() == null ? "classic" : shop.layout();
        String[] fonts = FONTS.getOrDefault(language, SCRIPT_FONTS.getOrDefault(language, LATIN_FONTS));
        if (fonts == LATIN_FONTS) {
            // Latin-script sites get a heading face that suits the layout.
            fonts = new String[] {switch (layout) {
                case "split" -> "Bricolage Grotesque";
                case "boutique" -> "Fraunces";
                case "magazine" -> "Unbounded";
                case "bento" -> "Manrope";
                case "noir" -> "DM Serif Display";
                case "mart" -> "Figtree";
                case "globe" -> "Playfair Display";
                case "poppy" -> "Big Shoulders Display";
                case "gradient" -> "Outfit";
                case "neon" -> "Nunito";
                default -> "Baloo 2";
            }, "Anek Latin"};
        }
        model.addAttribute("layout", layout);
        model.addAttribute("sample", profiles.isSample(shop.id()));
        model.addAttribute("categories", shop.products().stream()
                .map(ProductResponse::category).filter(Objects::nonNull).distinct().toList());
        String digits = shop.whatsappNumber() == null ? null : shop.whatsappNumber().replaceAll("[^0-9]", "");
        model.addAttribute("whatsappDisplay", digits == null ? null : "+" + (digits.length() == 10 ? "91" + digits : digits));

        model.addAttribute("shop", shop);
        model.addAttribute("L", labels);
        model.addAttribute("page", page);
        model.addAttribute("base", "/s/" + shop.slug());
        model.addAttribute("draft", shop.status() == Business.Status.DRAFT);
        var ex = extras.get(shop.id());
        model.addAttribute("ex", ex);
        model.addAttribute("openNow", ex.openNow());
        model.addAttribute("comingSoon", ex.comingSoon());
        model.addAttribute("haggleOn", ex.haggleOn() && "STORE".equals(shop.style()) && !ex.comingSoon());
        model.addAttribute("chatOn", !profiles.isSample(shop.id()));
        model.addAttribute("chatLanguages", com.bazaarready.extras.ChatService.LANGUAGES);
        model.addAttribute("store", "STORE".equals(shop.style()) && !ex.comingSoon());
        model.addAttribute("booking", "BOOKING".equals(shop.style()) && !ex.comingSoon());
        model.addAttribute("money", new Money(shop.currency()));
        model.addAttribute("htmlLang", "hinglish".equals(language) ? "hi-Latn" : language);
        model.addAttribute("dir", RIGHT_TO_LEFT.contains(language.split("-")[0]) ? "rtl" : "ltr");
        model.addAttribute("offeringsTitle", shop.offeringsTitle() != null ? shop.offeringsTitle() : labels.of("offerings"));
        model.addAttribute("detailsTitle", shop.detailsTitle() != null ? shop.detailsTitle() : labels.of("details_title"));
        model.addAttribute("hasDetails", shop.deliveryNotes() != null || shop.hours() != null || shop.location() != null
                || (shop.noticeDays() != null && shop.noticeDays() > 0));
        model.addAttribute("whatsappLink", whatsappLink(shop, labels));
        model.addAttribute("fontsUrl", "https://fonts.googleapis.com/css2?family=" + fonts[0].replace(' ', '+')
                + ":wght@600;800&family=" + fonts[1].replace(' ', '+') + ":wght@400;600&display=swap");
        model.addAttribute("fontVars", "--display:'" + fonts[0] + "';--body:'" + fonts[1] + "'");
        addPictures(shop, model);
        @SuppressWarnings("unchecked")
        Map<UUID, ImageService.Picture> pictures = (Map<UUID, ImageService.Picture>) model.getAttribute("pictures");
        model.addAttribute("mosaic", shop.products().stream().filter(p -> pictures != null && pictures.containsKey(p.id())).limit(5).toList());
    }

    /** Pictures for the banner and every offering, plus the photographers to credit. */
    private void addPictures(ProfileResponse shop, Model model) {
        Map<UUID, ImageService.Picture> thumbs = new HashMap<>();
        Map<UUID, ImageService.Picture> large = new HashMap<>();
        Map<String, ImageService.Picture> credits = new java.util.LinkedHashMap<>();
        for (ProductResponse p : shop.products()) {
            images.resolve(shop.id(), p.name(), p.imageQuery(), true).ifPresent(pic -> thumbs.put(p.id(), pic));
            images.resolve(shop.id(), p.name(), p.imageQuery(), false).ifPresent(pic -> {
                large.put(p.id(), pic);
                if (pic.isStock()) {
                    credits.putIfAbsent(pic.photographer(), pic);
                }
            });
        }
        List<String> missing = new java.util.ArrayList<>();
        for (ProductResponse p : shop.products()) {
            if (!large.containsKey(p.id()) && p.imageQuery() != null) {
                missing.add(p.imageQuery());
            }
        }
        ImageService.Picture hero = images.resolve(shop.id(), "", shop.heroImageQuery(), false).orElse(null);
        if (hero == null && shop.heroImageQuery() != null) {
            missing.add(shop.heroImageQuery());
        }
        images.ensureLater(missing);
        if (hero != null && hero.isStock()) {
            credits.putIfAbsent(hero.photographer(), hero);
        }
        model.addAttribute("hero", hero);
        model.addAttribute("thumbs", thumbs);
        model.addAttribute("pictures", large);
        model.addAttribute("credits", credits.values());
    }

    private void addOrder(Model model, ProfileResponse shop, ShopOrder order) {
        model.addAttribute("order", order);
        model.addAttribute("orderLines", orders.linesOf(order));
    }

    /** The priced products as JSON for the cart page. "</" is escaped so the data cannot close its script tag. */
    private String productsJson(ProfileResponse shop) {
        ArrayNode array = json.createArrayNode();
        for (ProductResponse product : shop.products()) {
            if (product.priceRupees() == null) {
                continue;
            }
            ObjectNode node = array.addObject();
            node.put("id", product.id().toString());
            node.put("name", product.name());
            node.put("price", product.priceRupees());
            node.put("unit", product.unit());
        }
        return array.toString().replace("</", "<\\/");
    }

    /** Deal ids from the browser; only their ids are used, the prices come from the database. */
    private static List<UUID> parseDeals(String deals) {
        List<UUID> ids = new java.util.ArrayList<>();
        for (String part : deals.split(",")) {
            try {
                if (!part.isBlank() && ids.size() < 10) {
                    ids.add(UUID.fromString(part.trim()));
                }
            } catch (IllegalArgumentException ignored) {
                // not a deal id
            }
        }
        return ids;
    }

    private Map<UUID, Integer> parseCart(String cart) {
        Map<UUID, Integer> result = new HashMap<>();
        try {
            Map<String, Integer> raw = json.readValue(cart, new TypeReference<Map<String, Integer>>() { });
            raw.forEach((id, quantity) -> {
                try {
                    if (quantity != null) {
                        result.put(UUID.fromString(id), quantity);
                    }
                } catch (IllegalArgumentException ignored) {
                    // not a product id; skip it
                }
            });
        } catch (JsonProcessingException e) {
            // an unreadable cart is treated as empty
        }
        return result;
    }

    private static String whatsappLink(ProfileResponse shop, SiteLabels labels) {
        if (shop.whatsappNumber() == null) {
            return null;
        }
        String digits = shop.whatsappNumber().replaceAll("[^0-9]", "");
        // A bare 10-digit number is taken to be Indian; the agent is asked to include the country code otherwise.
        if (digits.length() == 10) {
            digits = "91" + digits;
        }
        String text = URLEncoder.encode(labels.of("whatsapp_message", "name", shop.name()), StandardCharsets.UTF_8);
        return "https://wa.me/" + digits + "?text=" + text;
    }

    private static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    private static boolean looksLikePhone(String phone) {
        return phone != null && phone.replaceAll("[^0-9]", "").length() >= 6 && phone.trim().length() <= 20;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }

    /** Formats prices for the page with the business's currency: 850 becomes "₹850", 40.5 becomes "$40.50". */
    public static final class Money {
        private final String symbol;

        Money(String symbol) {
            this.symbol = symbol;
        }

        public String format(BigDecimal units) {
            BigDecimal value = units.stripTrailingZeros();
            return symbol + (value.scale() <= 0 ? value.toBigInteger().toString() : units.setScale(2).toPlainString());
        }

        /** Formats an amount held in the smallest unit (paise, cents). */
        public String minor(long amount) {
            return format(ProfileService.toUnits(amount));
        }
    }
}
