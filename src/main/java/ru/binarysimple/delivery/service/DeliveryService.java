package ru.binarysimple.delivery.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.binarysimple.delivery.dto.DeliveryFullDto;
import ru.binarysimple.delivery.dto.OrderResultDto;
import ru.binarysimple.delivery.filter.DeliveryFilter;

import java.io.IOException;
import java.util.List;

public interface DeliveryService {
    Page<DeliveryFullDto> getAll(DeliveryFilter filter, Pageable pageable);

    DeliveryFullDto getOne(Long id);

//    List<DeliveryFullDto> getMany(List<Long> ids);

    DeliveryFullDto create(DeliveryFullDto dto);

    DeliveryFullDto create(OrderResultDto orderResultDto);

    DeliveryFullDto compensate(OrderResultDto orderResultDto);

    DeliveryFullDto patch(Long id, JsonNode patchNode) throws IOException;

//    List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException;

//    DeliveryFullDto delete(Long id);

//    void deleteMany(List<Long> ids);
}
