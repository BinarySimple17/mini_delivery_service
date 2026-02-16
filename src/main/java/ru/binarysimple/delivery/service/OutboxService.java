package ru.binarysimple.delivery.service;

import ru.binarysimple.delivery.model.EventType;
import ru.binarysimple.delivery.model.ParentType;

public interface OutboxService {

    void saveEvent(EventType eventType, String parentId, ParentType parentType, Object payload, String topic);

    void processOutbox();
}
