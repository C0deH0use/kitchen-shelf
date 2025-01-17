package pl.codehouse.restaurant.shelf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
class ShelfEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(ShelfEventPublisher.class);

    private final KafkaTemplate<String, ShelfEventDto> kafkaTemplate;
    private final ShelfKafkaProperties kafkaProperties;

    ShelfEventPublisher(
            KafkaTemplate<String, ShelfEventDto> kafkaTemplate,
            ShelfKafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    void publishNewMenuItemAddedEvent(int menuItemId, int quantity) {
        publish(new ShelfEventDto(EventType.ADD, menuItemId, quantity));
    }

    void publishMenuItemTakenEvent(int menuItemId, int quantity) {
        publish(new ShelfEventDto(EventType.TAKE, menuItemId, quantity));
    }

    void publishMenuItemCreatedEvent(int menuItemId, int quantity) {
        publish(new ShelfEventDto(EventType.NEW, menuItemId, quantity));
    }

    private void publish(ShelfEventDto eventDto) {
        logger.info("Shelf Event:{} for menu item {} with quantity:{} about to be emitted", eventDto.eventType(), eventDto.menuItemId(), eventDto.quantity());
        Message<ShelfEventDto> message = new GenericMessage<>(eventDto, kafkaProperties.kafkaHeaders());
        kafkaTemplate.send(message);
    }
}
