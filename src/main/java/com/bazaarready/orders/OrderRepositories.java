package com.bazaarready.orders;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ShopOrderRepository extends JpaRepository<ShopOrder, UUID> {

    List<ShopOrder> findByBusinessIdOrderByCreatedAtDesc(UUID businessId);
}

interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {

    List<Enquiry> findByBusinessIdOrderByCreatedAtDesc(UUID businessId);
}
