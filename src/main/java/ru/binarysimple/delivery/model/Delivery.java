package ru.binarysimple.delivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

//    @Column(name = "saga_id", nullable = false)
//    private UUID sagaId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;



//    @Column(name = "saga_step", nullable = false)
//    @Enumerated(EnumType.STRING)
//    private SagaStep sagaStep;

//    @Column(name = "compensated_by")
//    private UUID compensatedBy; // ID операции компенсации
//
//    @Column(name = "compensates")
//    private UUID compensates; // ID исходной операции, которую компенсируем

    @CreationTimestamp
    @Column(
            name = "created_at",
            updatable = false,
            nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Integer version;

    public enum Status {
        CREATED,
        COMPLETED,
        FAILED,
        COMPENSATED
    }

    public enum SagaStep {
        RESERVATION, // Основная операция
        COMPENSATION // Компенсирующая операция
    }
}