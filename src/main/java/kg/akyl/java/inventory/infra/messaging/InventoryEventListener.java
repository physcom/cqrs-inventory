package kg.akyl.java.inventory.infra.messaging;

import kg.akyl.java.inventory.domain.InventoryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class InventoryEventListener {
    @Autowired
    private CacheManager cacheManager;

    @KafkaListener(topics = "product-events", groupId = "inventory-group")
    public void handleProductEvents(@Payload InventoryEvent event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset,
                                    Acknowledgment acknowledgment) {
        try {
            log.info("Received product event: {} for aggregate: {}",
                    event.getEventType(), event.getAggregateId());

            switch (event.getEventType()) {
                case "ProductCreated":
                    handleProductCreated(event);
                    break;
                case "ProductQuantityUpdated":
                    handleProductQuantityUpdated(event);
                    break;
                case "ProductReserved":
                    handleProductReserved(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing product event: {}", e.getMessage(), e);
            // In production, you might want to send to DLQ or implement retry logic
        }
    }

    @KafkaListener(topics = "sale-events", groupId = "inventory-group")
    public void handleSaleEvents(@Payload InventoryEvent event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 Acknowledgment acknowledgment) {
        try {
            log.info("Received sale event: {} for aggregate: {}",
                    event.getEventType(), event.getAggregateId());

            switch (event.getEventType()) {
                case "SaleProcessed":
                    handleSaleProcessed(event);
                    break;
                case "SaleCancelled":
                    handleSaleCancelled(event);
                    break;
                default:
                    log.warn("Unknown sale event type: {}", event.getEventType());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing sale event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "inventory-updates", groupId = "analytics-group")
    public void handleInventoryUpdates(@Payload InventoryEvent event,
                                       Acknowledgment acknowledgment) {
        try {
            log.info("Processing inventory update for analytics: {}", event.getAggregateId());

            // Invalidate related caches
            invalidateProductCaches(event.getAggregateId());

            // Update analytics materialized views if needed
            // This could trigger a background refresh of analytics data

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing inventory update: {}", e.getMessage(), e);
        }
    }

    private void handleProductCreated(InventoryEvent event) {
        // Product created - no cache invalidation needed as it's new
        log.debug("Product created: {}", event.getAggregateId());
    }

    private void handleProductQuantityUpdated(InventoryEvent event) {
        // Invalidate product caches
        invalidateProductCaches(event.getAggregateId());
        log.debug("Product quantity updated: {}", event.getAggregateId());
    }

    private void handleProductReserved(InventoryEvent event) {
        // Invalidate product caches
        invalidateProductCaches(event.getAggregateId());
        log.debug("Product reserved: {}", event.getAggregateId());
    }

    private void handleSaleProcessed(InventoryEvent event) {
        // Invalidate analytics caches
        if (cacheManager.getCache("sales-analytics") != null) {
            Objects.requireNonNull(cacheManager.getCache("sales-analytics")).clear();
        }
        if (cacheManager.getCache("inventory-analytics") != null) {
            Objects.requireNonNull(cacheManager.getCache("inventory-analytics")).clear();
        }
        log.debug("Sale processed: {}", event.getAggregateId());
    }

    private void handleSaleCancelled(InventoryEvent event) {
        // Handle sale cancellation - might need to restore inventory
        log.debug("Sale cancelled: {}", event.getAggregateId());
    }

    private void invalidateProductCaches(String productId) {
        if (cacheManager.getCache("products") != null) {
            Objects.requireNonNull(cacheManager.getCache("products")).evict(productId);
        }
    }
}
