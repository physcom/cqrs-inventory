package kg.akyl.java.inventory.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InventoryEvent {
    private String id;
    private String eventType;
    private String aggregateId;
    private Object eventData;
    private LocalDateTime timestamp;
    private Long version;

    public InventoryEvent() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public InventoryEvent(String eventType, String aggregateId, Object eventData, Long version) {
        this();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.eventData = eventData;
        this.version = version;
    }
}
