package com.bazaarready.extras;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * How a shop takes payment: UPI straight to the owner's UPI ID, cash on delivery, or ordering on WhatsApp.
 * BazaarReady never holds money. Without a payment gateway a UPI payment can't be confirmed automatically,
 * so the customer marks it as paid (with the reference if they like) and the owner confirms it on the
 * dashboard. Card payments are not offered: they need each owner's own gateway account.
 */
@Service
public class PaymentService {

    /** A UPI ID (virtual payment address): handle@bank, for example sunita@okaxis. */
    public static final Pattern UPI_ID = Pattern.compile("^[a-zA-Z0-9._-]{2,256}@[a-zA-Z][a-zA-Z0-9]{1,63}$");

    public record Payments(boolean upi, String upiId, boolean cod, boolean whatsapp) {
        static final Payments DEFAULTS = new Payments(false, null, true, false);

        /** UPI can be offered: switched on, with a valid ID. */
        public boolean upiReady() {
            return upi && upiId != null && UPI_ID.matcher(upiId).matches();
        }

        /** The methods in the order they are shown at checkout. */
        public List<String> methods(boolean rupees, boolean hasWhatsApp) {
            List<String> list = new ArrayList<>();
            if (upiReady() && rupees) {
                list.add("upi");
            }
            if (cod) {
                list.add("cod");
            }
            if (whatsapp && hasWhatsApp) {
                list.add("whatsapp");
            }
            if (list.isEmpty()) {
                list.add("cod"); // a shop always has at least one way to pay
            }
            return list;
        }
    }

    private final JdbcTemplate jdbc;

    PaymentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Payments get(UUID businessId) {
        List<Payments> rows = jdbc.query("select pay_upi, upi_id, pay_cod, pay_whatsapp from site_extras where business_id = ?",
                (rs, i) -> new Payments(rs.getBoolean("pay_upi"), rs.getString("upi_id"), rs.getBoolean("pay_cod"),
                        rs.getBoolean("pay_whatsapp")), businessId);
        return rows.isEmpty() ? Payments.DEFAULTS : rows.get(0);
    }

    /** Saves the owner's choice. A UPI ID that doesn't look like one is refused. */
    public void save(UUID businessId, boolean upi, String upiId, boolean cod, boolean whatsapp) {
        String id = upiId == null ? null : upiId.trim();
        if (id != null && id.isEmpty()) {
            id = null;
        }
        if (id != null && !UPI_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("That doesn't look like a UPI ID. It looks like name@bank, for example sunita@okaxis.");
        }
        if (upi && id == null) {
            throw new IllegalArgumentException("Add your UPI ID to take UPI payments.");
        }
        jdbc.update("insert into site_extras (business_id) values (?) on conflict do nothing", businessId);
        jdbc.update("update site_extras set pay_upi = ?, upi_id = ?, pay_cod = ?, pay_whatsapp = ?, updated_at = now() where business_id = ?",
                upi, id, cod, whatsapp, businessId);
    }

    /** Payment choices the agent gathered: a list of methods ("upi", "cod", "whatsapp") and a UPI ID. */
    public void saveFromAgent(UUID businessId, List<String> methods, String upiId) {
        boolean upi = methods.contains("upi") && upiId != null && UPI_ID.matcher(upiId.trim()).matches();
        boolean whatsapp = methods.contains("whatsapp");
        boolean cod = methods.contains("cod") || (!upi && !whatsapp);
        save(businessId, upi, upi ? upiId : null, cod, whatsapp);
    }

    /** The upi:// link that opens the customer's UPI app with the payee, amount and order number filled in. */
    public static String upiLink(String upiId, String payeeName, long totalPaise, String orderNumber) {
        String amount = BigDecimal.valueOf(totalPaise).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toPlainString();
        return "upi://pay?pa=" + enc(upiId).replace("%40", "@") + "&pn=" + enc(payeeName) + "&am=" + amount + "&cu=INR&tn=" + enc("Order " + orderNumber);
    }

    /** A QR code for a text, as a PNG data URI for an img tag. */
    public static String qrDataUri(String text) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 240, 240, Map.of(EncodeHintType.MARGIN, 1));
            BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < matrix.getWidth(); x++) {
                for (int y = 0; y < matrix.getHeight(); y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            return null;
        }
    }

    /** A wa.me link with the order written out, ready to send to the shop. */
    public static String whatsappOrderLink(String digits, String text) {
        return "https://wa.me/" + digits + "?text=" + enc(text);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
