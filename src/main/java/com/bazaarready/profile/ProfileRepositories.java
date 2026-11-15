package com.bazaarready.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Optional<Business> findByOwnerKey(UUID ownerKey);
}

interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByBusinessIdOrderBySortOrder(UUID businessId);

    void deleteByBusinessId(UUID businessId);
}

interface FaqRepository extends JpaRepository<Faq, UUID> {

    List<Faq> findByBusinessIdOrderBySortOrder(UUID businessId);

    void deleteByBusinessId(UUID businessId);
}
