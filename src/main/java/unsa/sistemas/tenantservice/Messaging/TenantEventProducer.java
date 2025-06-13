package unsa.sistemas.tenantservice.Messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import unsa.sistemas.tenantservice.DTOs.CreateDataBaseEvent;

@Slf4j
@Service
public class TenantEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    public TenantEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDatabaseCreatedEvent(CreateDataBaseEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.debug("Sending event DATABASE_CREATED for tenantId {}", event);
    }
}
