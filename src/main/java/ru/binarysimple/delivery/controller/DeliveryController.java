package ru.binarysimple.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;
import ru.binarysimple.delivery.dto.DeliveryFullDto;
import ru.binarysimple.delivery.filter.DeliveryFilter;
import ru.binarysimple.delivery.service.DeliveryService;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public PagedModel<DeliveryFullDto> getAll(@ParameterObject @ModelAttribute DeliveryFilter filter, @ParameterObject Pageable pageable) {
        Page<DeliveryFullDto> deliveryFullDtos = deliveryService.getAll(filter, pageable);
        return new PagedModel<>(deliveryFullDtos);
    }

    @GetMapping("/{id}")
    public DeliveryFullDto getOne(@PathVariable Long id) {
        return deliveryService.getOne(id);
    }

//    @GetMapping("/by-ids")
//    public List<DeliveryFullDto> getMany(@RequestParam List<Long> ids) {
//        return deliveryService.getMany(ids);
//    }

    @PostMapping
    public DeliveryFullDto create(@RequestBody DeliveryFullDto dto) {
        return deliveryService.create(dto);
    }

//    @PatchMapping("/{id}")
//    public DeliveryFullDto patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
//        return deliveryService.patch(id, patchNode);
//    }
//
//    @PatchMapping
//    public List<Long> patchMany(@RequestParam List<Long> ids, @RequestBody JsonNode patchNode) throws IOException {
//        return deliveryService.patchMany(ids, patchNode);
//    }
//
//    @DeleteMapping("/{id}")
//    public DeliveryFullDto delete(@PathVariable Long id) {
//        return deliveryService.delete(id);
//    }
//
//    @DeleteMapping
//    public void deleteMany(@RequestParam List<Long> ids) {
//        deliveryService.deleteMany(ids);
//    }
}
