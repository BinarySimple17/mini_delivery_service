package ru.binarysimple.delivery.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.binarysimple.delivery.dto.OrderResultDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryCompensationResponseEvent {

    private final UUID eventId = UUID.randomUUID();
    private UUID sagaId;
    private Boolean success;
    private String message;
    private OrderResultDto order;

    private LocalDateTime timestamp = LocalDateTime.now();
}
