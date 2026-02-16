package ru.binarysimple.delivery.dto;

import lombok.Value;
import ru.binarysimple.delivery.model.Delivery;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link ru.binarysimple.delivery.model.Delivery}
 */
@Value
public class DeliveryFullDto {
    Long id;
    Long orderId;
    String username;
    BigDecimal price;
    Delivery.Status status;
    LocalDateTime expiresAt;
}