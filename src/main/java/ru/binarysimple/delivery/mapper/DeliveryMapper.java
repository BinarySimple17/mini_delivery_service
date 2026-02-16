package ru.binarysimple.delivery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.binarysimple.delivery.dto.DeliveryFullDto;
import ru.binarysimple.delivery.model.Delivery;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeliveryMapper {
    Delivery toEntity(DeliveryFullDto deliveryFullDto);

    DeliveryFullDto toDeliveryFullDto(Delivery delivery);

    Delivery updateWithNull(DeliveryFullDto deliveryFullDto, @MappingTarget Delivery delivery);
}