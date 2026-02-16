package ru.binarysimple.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.binarysimple.delivery.model.ProcessedEventId;

public interface ProcessedEventIdRepository extends JpaRepository<ProcessedEventId, String> {
}
