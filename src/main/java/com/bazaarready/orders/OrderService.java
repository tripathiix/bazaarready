package com.bazaarready.orders;

import com.bazaarready.common.NotFoundException;
import com.bazaarready.profile.ProfileDtos.ProductResponse;
import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import com.bazaarready.profile.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Orders, booking requests and contact-form messages for a site. */
@Service
public class OrderService {

    /** One line of an order, frozen at the time of ordering so later price changes do not alter it. */
    public record OrderLine(UUID productId, String name, long unitPricePaise, int quantity) {
        public long lineTotalPaise() {
            return unitPricePaise * quantity;
        }
    }

    public record Customer(String name, String phone, String address, String note) {
    }

    private static final int MAX_QUANTITY = 99;

    private final ShopOrderRepository orders;
    private final EnquiryRepository enquiries;
    private final ObjectMapper json;

    OrderService(ShopOrderRepository orders, EnquiryRepository enquiries, ObjectMapper json) {
        this.orders = orders;
        this.enquiries = enquiries;
        this.json = json;
    }

    /**
     * Places an order from a cart of product ids and quantities. Names and prices are taken from
     * the saved profile, never from the browser, so a customer cannot change a price.
     */
    @Transactional
    public ShopOrder placeOrder(ProfileResponse shop, Map<UUID, Integer> cart, Customer customer, LocalDate deliveryDate) {
        List<OrderLine> lines = new ArrayList<>();
        for (ProductResponse product : shop.products()) {
            Integer quantity = cart.get(product.id());
            if (quantity == null || quantity < 1 || product.priceRupees() == null) {
                continue;
            }
            lines.add(new OrderLine(product.id(), product.name(), ProfileService.toMinor(product.priceRupees()),
                    Math.min(quantity, MAX_QUANTITY)));
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("The cart is empty.");
        }
        long total = lines.stream().mapToLong(OrderLine::lineTotalPaise).sum();
        ShopOrder order = new ShopOrder(shop.id(), ShopOrder.Kind.ORDER, customer.name(), customer.phone(), write(lines), total);
        order.setAddress(customer.address());
        order.setNote(customer.note());
        order.setBookingDate(deliveryDate);
        return orders.save(order);
    }

    /** Records a booking request for one offering on a date. Nothing is charged. */
    @Transactional
    public ShopOrder placeBooking(ProfileResponse shop, UUID productId, LocalDate date, String time, Customer customer) {
        List<OrderLine> lines = shop.products().stream()
                .filter(product -> product.id().equals(productId))
                .map(product -> new OrderLine(product.id(), product.name(),
                        product.priceRupees() == null ? 0 : ProfileService.toMinor(product.priceRupees()), 1))
                .toList();
        long total = lines.stream().mapToLong(OrderLine::lineTotalPaise).sum();
        ShopOrder booking = new ShopOrder(shop.id(), ShopOrder.Kind.BOOKING, customer.name(), customer.phone(), write(lines), total);
        booking.setBookingDate(date);
        booking.setBookingTime(time);
        booking.setNote(customer.note());
        return orders.save(booking);
    }

    @Transactional
    public ShopOrder markPaidDemo(UUID businessId, UUID orderId) {
        ShopOrder order = find(businessId, orderId);
        order.markPaidDemo();
        return order;
    }

    @Transactional(readOnly = true)
    public ShopOrder find(UUID businessId, UUID orderId) {
        return orders.findById(orderId)
                .filter(order -> order.getBusinessId().equals(businessId))
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    @Transactional
    public Enquiry saveEnquiry(UUID businessId, String name, String contact, String message) {
        return enquiries.save(new Enquiry(businessId, name, contact, message));
    }

    @Transactional(readOnly = true)
    public List<ShopOrder> ordersFor(UUID businessId) {
        return orders.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    @Transactional(readOnly = true)
    public List<Enquiry> enquiriesFor(UUID businessId) {
        return enquiries.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    public List<OrderLine> linesOf(ShopOrder order) {
        try {
            return json.readValue(order.getItems(), new TypeReference<List<OrderLine>>() { });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String write(List<OrderLine> lines) {
        try {
            return json.writeValueAsString(lines);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not store the order lines", e);
        }
    }
}
