package ru.binarysimple.delivery.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.binarysimple.delivery.dto.DeliveryFullDto;
import ru.binarysimple.delivery.model.Delivery;
import ru.binarysimple.delivery.model.EventType;
import ru.binarysimple.delivery.model.ParentType;
import ru.binarysimple.delivery.model.ProcessedEventId;
import ru.binarysimple.delivery.repository.ProcessedEventIdRepository;
import ru.binarysimple.delivery.service.DeliveryService;
import ru.binarysimple.delivery.service.OutboxService;

@RequiredArgsConstructor
@Slf4j
@Component
public class KafkaListener {

    private final ObjectMapper objectMapper;

    private final DeliveryService deliveryService;

    private final OutboxService outboxService;

    private final ProcessedEventIdRepository processedEventIdRepository;

    @org.springframework.kafka.annotation.KafkaListener(
            id = "deliveryListenerReserve",
            topics = "delivery.reserve.request")
    @Transactional
    public void handleDeliveryRequest(String message) {

        log.debug("handleDeliveryRequest from kafka {}", message);

        try {
            DeliveryReservationRequestEvent event = objectMapper.readValue(message, DeliveryReservationRequestEvent.class);

            ProcessedEventId processedEventId = new ProcessedEventId();
            processedEventId.setEventId(event.getEventId().toString());
            processedEventIdRepository.save(processedEventId);

            DeliveryFullDto deliveryFullDto = deliveryService.create(event.getOrder());
            DeliveryReservationResponseEvent responseEvent = new DeliveryReservationResponseEvent();
            responseEvent.setOrder(event.getOrder());
            responseEvent.setSagaId(event.getSagaId());
            responseEvent.setSuccess(deliveryFullDto.getStatus() != Delivery.Status.FAILED);
            outboxService.saveEvent(
                    responseEvent.getSuccess() ? EventType.DELIVERY_RESERVED : EventType.DELIVERY_RESERVATION_FAILED,
                    event.getSagaId().toString(),
                    ParentType.SAGA,
                    responseEvent,
                    "delivery.reserve.response"
            );

            log.info("Request processed: {}", message);
        } catch (Exception e) {
            log.error("Failed to process request: {}", DeliveryReservationRequestEvent.class, e);
        }

    }

    @org.springframework.kafka.annotation.KafkaListener(
            id = "deliveryListenerCompensate",
            topics = "delivery.compensate.request")
    @Transactional
    public void handleDeliveryCompensateRequest(String message) {

        log.debug("handleDeliveryCompensateRequest from kafka {}", message);

        try {
            DeliveryCompensationRequestEvent event = objectMapper.readValue(message, DeliveryCompensationRequestEvent.class);

            ProcessedEventId processedEventId = new ProcessedEventId();
            processedEventId.setEventId(event.getEventId().toString());
            processedEventIdRepository.save(processedEventId);

            deliveryService.compensate(event.getOrder());
            DeliveryCompensationRequestEvent responseEvent = new DeliveryCompensationRequestEvent();
            responseEvent.setOrder(event.getOrder());
            responseEvent.setSagaId(event.getSagaId());

            outboxService.saveEvent(
                    EventType.DELIVERY_COMPENSATION_COMPLETED,
                    event.getSagaId().toString(),
                    ParentType.SAGA,
                    responseEvent,
                    "delivery.compensate.response"
            );

            log.info("Request compensation processed: {}", message);

        } catch (Exception e) {
            log.error("Failed to process compensation request: {}", DeliveryReservationRequestEvent.class, e);
        }
    }
}
