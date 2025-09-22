package kg.gns.java.inventorysystem.command.handlers;

import jakarta.transaction.Transactional;
import kg.gns.java.inventorysystem.command.commands.CreateProductCommand;
import kg.gns.java.inventorysystem.command.commands.ReserveProductCommand;
import kg.gns.java.inventorysystem.command.commands.UpdateProductQuantityCommand;
import kg.gns.java.inventorysystem.domain.InventoryEvent;
import kg.gns.java.inventorysystem.domain.Product;
import kg.gns.java.inventorysystem.domain.ProductStatus;
import kg.gns.java.inventorysystem.infra.events.EventStore;
import kg.gns.java.inventorysystem.infra.repositories.ProductRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductCommandHandler {

    private final ProductRepository productRepository;
    private final EventStore eventStore;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void handle(CreateProductCommand command) {
        Product product = new Product(
                command.getSku(),
                command.getName(),
                command.getDescription(),
                command.getPrice(),
                command.getQuantity(),
                command.getCategory(),
                ProductStatus.ACTIVE
        );

        Product savedProduct = productRepository.save(product);

        // Create and store event
        InventoryEvent event = new InventoryEvent(
                "ProductCreated",
                savedProduct.getId().toString(),
                savedProduct,
                1L
        );
        eventStore.saveEvent(event);

        kafkaTemplate.send("product-events", event);
    }

    @Transactional
    public void handle(UpdateProductQuantityCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int oldQuantity = product.getQuantity();
        product.setQuantity(command.getNewQuantity());

        productRepository.save(product);

        // Create event
        QuantityUpdateEvent eventData = new QuantityUpdateEvent(
                command.getProductId(), oldQuantity, command.getNewQuantity()
        );

        InventoryEvent event = new InventoryEvent(
                "ProductQuantityUpdated",
                product.getId().toString(),
                eventData,
                product.getVersion()
        );

        eventStore.saveEvent(event);
        kafkaTemplate.send("inventory-updates", event);
    }

    @Transactional
    public void handle(ReserveProductCommand command) {
        Product product = productRepository.findByIdWithLock(command.getProductId());

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        int availableQuantity = product.getQuantity() - product.getReservedQuantity();
        if (availableQuantity < command.getQuantity()) {
            throw new RuntimeException("Insufficient inventory");
        }

        product.setReservedQuantity(product.getReservedQuantity() + command.getQuantity());
        productRepository.save(product);

        // Create event
        ReservationEvent eventData = new ReservationEvent(
                command.getProductId(), command.getQuantity(), "RESERVED"
        );

        InventoryEvent event = new InventoryEvent(
                "ProductReserved",
                product.getId().toString(),
                eventData,
                product.getVersion()
        );

        eventStore.saveEvent(event);
        kafkaTemplate.send("reservation-events", event);
    }

    // Event data classes
    @Setter
    @Getter
    public static class QuantityUpdateEvent {
        // Getters and setters
        private Long productId;
        private int oldQuantity;
        private int newQuantity;

        public QuantityUpdateEvent(Long productId, int oldQuantity, int newQuantity) {
            this.productId = productId;
            this.oldQuantity = oldQuantity;
            this.newQuantity = newQuantity;
        }

    }

    @Setter
    @Getter
    public static class ReservationEvent {
        // Getters and setters
        private Long productId;
        private int quantity;
        private String status;

        public ReservationEvent(Long productId, int quantity, String status) {
            this.productId = productId;
            this.quantity = quantity;
            this.status = status;
        }

    }
}
