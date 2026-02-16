package ru.binarysimple.delivery.filter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.binarysimple.delivery.model.Delivery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DeliveryFilter(List<Long> orderIdIn, String username, BigDecimal priceLte, BigDecimal priceGte,
                             List<Delivery.Status> statusIn, LocalDateTime expiresAtLte, LocalDateTime expiresAtGte,
                             LocalDateTime createdAtLte, LocalDateTime createdAtGte, LocalDateTime updatedAtLte,
                             LocalDateTime updatedAtGte) {
    public Specification<Delivery> toSpecification() {
        return orderIdInSpec()
                .and(usernameSpec())
                .and(priceLteSpec())
                .and(priceGteSpec())
                .and(statusInSpec())
                .and(expiresAtLteSpec())
                .and(expiresAtGteSpec())
                .and(createdAtLteSpec())
                .and(createdAtGteSpec())
                .and(updatedAtLteSpec())
                .and(updatedAtGteSpec());
    }

    private Specification<Delivery> orderIdInSpec() {
        return ((root, query, cb) -> orderIdIn != null
                ? root.get("orderId").in(orderIdIn)
                : null);
    }

    private Specification<Delivery> usernameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(username)
                ? cb.equal(root.get("username"), username)
                : null);
    }

    private Specification<Delivery> priceLteSpec() {
        return ((root, query, cb) -> priceLte != null
                ? cb.lessThanOrEqualTo(root.get("price"), priceLte)
                : null);
    }

    private Specification<Delivery> priceGteSpec() {
        return ((root, query, cb) -> priceGte != null
                ? cb.greaterThanOrEqualTo(root.get("price"), priceGte)
                : null);
    }

    private Specification<Delivery> statusInSpec() {
        return ((root, query, cb) -> statusIn != null
                ? root.get("status").in(statusIn)
                : null);
    }

    private Specification<Delivery> expiresAtLteSpec() {
        return ((root, query, cb) -> expiresAtLte != null
                ? cb.lessThanOrEqualTo(root.get("expiresAt"), expiresAtLte)
                : null);
    }

    private Specification<Delivery> expiresAtGteSpec() {
        return ((root, query, cb) -> expiresAtGte != null
                ? cb.greaterThanOrEqualTo(root.get("expiresAt"), expiresAtGte)
                : null);
    }

    private Specification<Delivery> createdAtLteSpec() {
        return ((root, query, cb) -> createdAtLte != null
                ? cb.lessThanOrEqualTo(root.get("createdAt"), createdAtLte)
                : null);
    }

    private Specification<Delivery> createdAtGteSpec() {
        return ((root, query, cb) -> createdAtGte != null
                ? cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtGte)
                : null);
    }

    private Specification<Delivery> updatedAtLteSpec() {
        return ((root, query, cb) -> updatedAtLte != null
                ? cb.lessThanOrEqualTo(root.get("updatedAt"), updatedAtLte)
                : null);
    }

    private Specification<Delivery> updatedAtGteSpec() {
        return ((root, query, cb) -> updatedAtGte != null
                ? cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedAtGte)
                : null);
    }
}