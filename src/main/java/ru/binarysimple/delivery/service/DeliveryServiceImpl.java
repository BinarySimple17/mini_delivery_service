package ru.binarysimple.delivery.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.binarysimple.delivery.dto.DeliveryFullDto;
import ru.binarysimple.delivery.dto.OrderResultDto;
import ru.binarysimple.delivery.filter.DeliveryFilter;
import ru.binarysimple.delivery.mapper.DeliveryMapper;
import ru.binarysimple.delivery.model.Delivery;
import ru.binarysimple.delivery.repository.DeliveryRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import static ru.binarysimple.delivery.model.Delivery.Status.CREATED;

@RequiredArgsConstructor
@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryMapper deliveryMapper;

    private final DeliveryRepository deliveryRepository;

    private final ObjectMapper objectMapper;

    @Override
    public Page<DeliveryFullDto> getAll(DeliveryFilter filter, Pageable pageable) {
        Specification<Delivery> spec = filter.toSpecification();
        Page<Delivery> deliveries = deliveryRepository.findAll(spec, pageable);
        return deliveries.map(deliveryMapper::toDeliveryFullDto);
    }

    @Override
    public DeliveryFullDto getOne(Long id) {
        Optional<Delivery> deliveryOptional = deliveryRepository.findById(id);
        return deliveryMapper.toDeliveryFullDto(deliveryOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
    }

//    @Override
//    public List<DeliveryFullDto> getMany(List<Long> ids) {
//        List<Delivery> deliveries = deliveryRepository.findAllById(ids);
//        return deliveries.stream()
//                .map(deliveryMapper::toDeliveryFullDto)
//                .toList();
//    }

    @Override
    public DeliveryFullDto create(DeliveryFullDto dto) {
        Delivery delivery = deliveryMapper.toEntity(dto);
        Delivery resultDelivery = deliveryRepository.save(delivery);
        return deliveryMapper.toDeliveryFullDto(resultDelivery);
    }

    @Override
    public DeliveryFullDto create(OrderResultDto order) {
        // Проверяем, существует ли уже доставка с таким orderId
        Optional<Delivery> existingDelivery = deliveryRepository.findByOrderId(order.getId());
        if (existingDelivery.isPresent()) {
            return deliveryMapper.toDeliveryFullDto(existingDelivery.get());
        }

        Delivery newDelivery = new Delivery();
        newDelivery.setOrderId(order.getId());
        
        //todo REST запрос во внешнюю службу доставки и оттуда цена, срок и т.д.
        //через webhook? наверно внешняя служба обновляет статус доставки здесь
        
        newDelivery.setPrice(order.getTotalCost().multiply(BigDecimal.valueOf(0.1)));
        newDelivery.setStatus(CREATED);

        Random random = new Random();
        if (random.nextBoolean()) {
            newDelivery.setStatus(Delivery.Status.FAILED);
        }
        newDelivery.setExpiresAt(LocalDateTime.now().plusDays(3));
        newDelivery.setUsername(order.getUsername());

        Delivery resultDelivery = deliveryRepository.save(newDelivery);
        return deliveryMapper.toDeliveryFullDto(resultDelivery);
    }

    @Override
    public DeliveryFullDto compensate(OrderResultDto orderResultDto) {
        return null;
    }

    
    @Override
    public DeliveryFullDto patch(Long id, JsonNode patchNode) throws IOException {
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        DeliveryFullDto deliveryFullDto = deliveryMapper.toDeliveryFullDto(delivery);
        objectMapper.readerForUpdating(deliveryFullDto).readValue(patchNode);
        deliveryMapper.updateWithNull(deliveryFullDto, delivery);

        Delivery resultDelivery = deliveryRepository.save(delivery);
        return deliveryMapper.toDeliveryFullDto(resultDelivery);
    }
//
//    @Override
//    public List<Long> patchMany(List<Long> ids, JsonNode patchNode) throws IOException {
//        Collection<Delivery> deliveries = deliveryRepository.findAllById(ids);
//
//        for (Delivery delivery : deliveries) {
//            DeliveryFullDto deliveryFullDto = deliveryMapper.toDeliveryFullDto(delivery);
//            objectMapper.readerForUpdating(deliveryFullDto).readValue(patchNode);
//            deliveryMapper.updateWithNull(deliveryFullDto, delivery);
//        }
//
//        List<Delivery> resultDeliveries = deliveryRepository.saveAll(deliveries);
//        return resultDeliveries.stream()
//                .map(Delivery::getId)
//                .toList();
//    }
//
//    @Override
//    public DeliveryFullDto delete(Long id) {
//        Delivery delivery = deliveryRepository.findById(id).orElse(null);
//        if (delivery != null) {
//            deliveryRepository.delete(delivery);
//        }
//        return deliveryMapper.toDeliveryFullDto(delivery);
//    }
//
//    @Override
//    public void deleteMany(List<Long> ids) {
//        deliveryRepository.deleteAllById(ids);
//    }
}
