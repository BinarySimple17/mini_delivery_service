package ru.binarysimple.delivery.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.binarysimple.delivery.dto.OrderPositionDto;
import ru.binarysimple.delivery.dto.OrderResultDto;
import ru.binarysimple.delivery.model.EventType;
import ru.binarysimple.delivery.model.ParentType;
import ru.binarysimple.delivery.service.CatalogServiceImpl;
import ru.binarysimple.delivery.service.OutboxService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KafkaListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CatalogServiceImpl catalogService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private KafkaListener kafkaListener;

    @Captor
    private ArgumentCaptor<EventType> eventTypeCaptor;

    @Captor
    private ArgumentCaptor<String> parentIdCaptor;

    @Captor
    private ArgumentCaptor<ParentType> parentTypeCaptor;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    private UUID sagaId;
    private OrderResultDto order;
    private DeliveryReservationRequestEvent reservationRequest;
    private DeliveryCompensationRequestEvent compensationRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        sagaId = UUID.randomUUID();
        order = OrderResultDto.builder()
                .id(1L)
                .username("testUser")
                .orderPositions(List.of(
                    OrderPositionDto.builder()
                            .productId(1L)
                            .quantity(2)
                            .price(BigDecimal.valueOf(100))
                            .build()
                ))
                .totalCost(BigDecimal.valueOf(200))
                .createdAt(LocalDateTime.now())
                .shopId(1L)
                .build();
                
        reservationRequest = DeliveryReservationRequestEvent.builder()
                .eventId(UUID.randomUUID())
                .sagaId(sagaId)
                .order(order)
                .timestamp(LocalDateTime.now())
                .build();
                
        compensationRequest = DeliveryCompensationRequestEvent.builder()
                .eventId(UUID.randomUUID())
                .sagaId(sagaId)
                .order(order)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void handleDeliveryRequest_SuccessfulReservation() throws Exception {
        // Given
        String message = "test message";
        DeliveryReservationResponseEvent successResponse = DeliveryReservationResponseEvent.builder()
                .sagaId(sagaId)
                .success(true)
                .order(order)
                .build();
        
        when(objectMapper.readValue(message, DeliveryReservationRequestEvent.class))
                .thenReturn(reservationRequest);
        when(catalogService.reserveOrder(order, sagaId)).thenReturn(successResponse);

        // When
        kafkaListener.handleDeliveryRequest(message);

        // Then
        verify(outboxService).saveEvent(
                eventTypeCaptor.capture(),
                parentIdCaptor.capture(),
                parentTypeCaptor.capture(),
                payloadCaptor.capture(),
                topicCaptor.capture()
        );
        
        assertEquals(EventType.DELIVERY_RESERVED, eventTypeCaptor.getValue());
        assertEquals(sagaId.toString(), parentIdCaptor.getValue());
        assertEquals(ParentType.SAGA, parentTypeCaptor.getValue());
        assertEquals("delivery.reserve.response", topicCaptor.getValue());
        
        Object capturedPayload = payloadCaptor.getValue();
        assertTrue(capturedPayload instanceof DeliveryReservationResponseEvent);
        DeliveryReservationResponseEvent capturedEvent = (DeliveryReservationResponseEvent) capturedPayload;
        assertTrue(capturedEvent.getSuccess());
        assertEquals(sagaId, capturedEvent.getSagaId());
        assertEquals(order, capturedEvent.getOrder());
    }

    @Test
    void handleDeliveryRequest_FailedReservation() throws Exception {
        // Given
        String message = "test message";
        DeliveryReservationResponseEvent failureResponse = DeliveryReservationResponseEvent.builder()
                .sagaId(sagaId)
                .success(false)
                .message("Not enough stock")
                .order(order)
                .build();
        
        when(objectMapper.readValue(message, DeliveryReservationRequestEvent.class))
                .thenReturn(reservationRequest);
        when(catalogService.reserveOrder(order, sagaId)).thenReturn(failureResponse);

        // When
        kafkaListener.handleDeliveryRequest(message);

        // Then
        verify(outboxService).saveEvent(
                eventTypeCaptor.capture(),
                parentIdCaptor.capture(),
                parentTypeCaptor.capture(),
                payloadCaptor.capture(),
                topicCaptor.capture()
        );
        
        assertEquals(EventType.DELIVERY_RESERVATION_FAILED, eventTypeCaptor.getValue());
        assertEquals(sagaId.toString(), parentIdCaptor.getValue());
        assertEquals(ParentType.SAGA, parentTypeCaptor.getValue());
        assertEquals("delivery.reserve.response", topicCaptor.getValue());
        
        Object capturedPayload = payloadCaptor.getValue();
        assertTrue(capturedPayload instanceof DeliveryReservationResponseEvent);
        DeliveryReservationResponseEvent capturedEvent = (DeliveryReservationResponseEvent) capturedPayload;
        assertFalse(capturedEvent.getSuccess());
        assertEquals(sagaId, capturedEvent.getSagaId());
        assertEquals(order, capturedEvent.getOrder());
        assertEquals("Not enough stock", capturedEvent.getMessage());
    }

    @Test
    void handleDeliveryRequest_JsonParsingError() throws Exception {
        // Given
        String message = "invalid json";
        
        when(objectMapper.readValue(message, DeliveryReservationRequestEvent.class))
                .thenThrow(new RuntimeException("JSON parse error"));

        // When
        kafkaListener.handleDeliveryRequest(message);

        // Then
        verify(outboxService, never()).saveEvent(any(), any(), any(), any(), any());
    }

    @Test
    void handleDeliveryCompensateRequest_SuccessfulCompensation() throws Exception {
        // Given
        String message = "test message";
        DeliveryCompensationResponseEvent successResponse = DeliveryCompensationResponseEvent.builder()
                .sagaId(sagaId)
                .success(true)
                .order(order)
                .build();
        
        when(objectMapper.readValue(message, DeliveryCompensationRequestEvent.class))
                .thenReturn(compensationRequest);
        when(catalogService.compensateOrder(order, sagaId)).thenReturn(successResponse);

        // When
        kafkaListener.handleDeliveryCompensateRequest(message);

        // Then
        verify(outboxService).saveEvent(
                eventTypeCaptor.capture(),
                parentIdCaptor.capture(),
                parentTypeCaptor.capture(),
                payloadCaptor.capture(),
                topicCaptor.capture()
        );
        
        assertEquals(EventType.DELIVERY_COMPENSATION_COMPLETED, eventTypeCaptor.getValue());
        assertEquals(sagaId.toString(), parentIdCaptor.getValue());
        assertEquals(ParentType.SAGA, parentTypeCaptor.getValue());
        assertEquals("delivery.compensate.response", topicCaptor.getValue());
        
        Object capturedPayload = payloadCaptor.getValue();
        assertTrue(capturedPayload instanceof DeliveryCompensationResponseEvent);
        DeliveryCompensationResponseEvent capturedEvent = (DeliveryCompensationResponseEvent) capturedPayload;
        assertTrue(capturedEvent.getSuccess());
        assertEquals(sagaId, capturedEvent.getSagaId());
        assertEquals(order, capturedEvent.getOrder());
    }

    @Test
    void handleDeliveryCompensateRequest_FailedCompensation() throws Exception {
        // Given
        String message = "test message";
        DeliveryCompensationResponseEvent failureResponse = DeliveryCompensationResponseEvent.builder()
                .sagaId(sagaId)
                .success(false)
                .message("Compensation failed")
                .order(order)
                .build();
        
        when(objectMapper.readValue(message, DeliveryCompensationRequestEvent.class))
                .thenReturn(compensationRequest);
        when(catalogService.compensateOrder(order, sagaId)).thenReturn(failureResponse);

        // When
        kafkaListener.handleDeliveryCompensateRequest(message);

        // Then
        verify(outboxService).saveEvent(
                eventTypeCaptor.capture(),
                parentIdCaptor.capture(),
                parentTypeCaptor.capture(),
                payloadCaptor.capture(),
                topicCaptor.capture()
        );
        
        assertEquals(EventType.DELIVERY_COMPENSATION_FAILED, eventTypeCaptor.getValue());
        assertEquals(sagaId.toString(), parentIdCaptor.getValue());
        assertEquals(ParentType.SAGA, parentTypeCaptor.getValue());
        assertEquals("delivery.compensate.response", topicCaptor.getValue());
        
        Object capturedPayload = payloadCaptor.getValue();
        assertTrue(capturedPayload instanceof DeliveryCompensationResponseEvent);
        DeliveryCompensationResponseEvent capturedEvent = (DeliveryCompensationResponseEvent) capturedPayload;
        assertFalse(capturedEvent.getSuccess());
        assertEquals(sagaId, capturedEvent.getSagaId());
        assertEquals(order, capturedEvent.getOrder());
        assertEquals("Compensation failed", capturedEvent.getMessage());
    }

    @Test
    void handleDeliveryCompensateRequest_JsonParsingError() throws Exception {
        // Given
        String message = "invalid json";
        
        when(objectMapper.readValue(message, DeliveryCompensationRequestEvent.class))
                .thenThrow(new RuntimeException("JSON parse error"));

        // When
        kafkaListener.handleDeliveryCompensateRequest(message);

        // Then
        verify(outboxService, never()).saveEvent(any(), any(), any(), any(), any());
    }
}