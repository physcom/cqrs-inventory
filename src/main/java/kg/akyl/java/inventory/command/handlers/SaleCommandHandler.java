package kg.akyl.java.inventory.command.handlers;

import jakarta.transaction.Transactional;
import kg.akyl.java.inventory.command.commands.ProcessSaleCommand;
import kg.akyl.java.inventory.domain.InventoryEvent;
import kg.akyl.java.inventory.domain.Product;
import kg.akyl.java.inventory.domain.Sale;
import kg.akyl.java.inventory.domain.SaleStatus;
import kg.akyl.java.inventory.infra.events.EventStore;
import kg.akyl.java.inventory.infra.repositories.ProductRepository;
import kg.akyl.java.inventory.infra.repositories.SaleRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SaleCommandHandler {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final EventStore eventStore;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void handle(ProcessSaleCommand command) {
        // Lock product for update
        Product product = productRepository.findByIdWithLock(command.getProductId());
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        // Check inventory
        int availableQuantity = product.getQuantity() - product.getReservedQuantity();
        if (availableQuantity < command.getQuantity()) {
            throw new RuntimeException("Insufficient inventory");
        }

        // Create sale record
        Sale sale = new Sale(
                command.getProductId(),
                command.getQuantity(),
                command.getUnitPrice(),
                command.getCustomerId(),
                SaleStatus.CONFIRMED
        );

        Sale savedSale = saleRepository.save(sale);

        // Update product inventory
        product.setQuantity(product.getQuantity() - command.getQuantity());
        productRepository.save(product);

        // Create events
        SaleEvent saleEvent = new SaleEvent(
                savedSale.getId(),
                command.getProductId(),
                command.getQuantity(),
                command.getUnitPrice(),
                savedSale.getTotalAmount()
        );

        InventoryEvent event = new InventoryEvent(
                "SaleProcessed",
                savedSale.getId().toString(),
                saleEvent,
                1L
        );

        eventStore.saveEvent(event);
        kafkaTemplate.send("sale-events", event);

        // Send inventory update event
        InventoryEvent inventoryEvent = new InventoryEvent(
                "InventoryReduced",
                product.getId().toString(),
                new ProductCommandHandler.QuantityUpdateEvent(
                        product.getId(),
                        product.getQuantity() + command.getQuantity(),
                        product.getQuantity()
                ),
                product.getVersion()
        );

        eventStore.saveEvent(inventoryEvent);
        kafkaTemplate.send("inventory-updates", inventoryEvent);
    }

    @Setter
    @Getter
    public static class SaleEvent {
        // Getters and setters
        private Long saleId;
        private Long productId;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;

        public SaleEvent(Long saleId, Long productId, int quantity, BigDecimal unitPrice, BigDecimal totalAmount) {
            this.saleId = saleId;
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalAmount = totalAmount;
        }

    }
}
