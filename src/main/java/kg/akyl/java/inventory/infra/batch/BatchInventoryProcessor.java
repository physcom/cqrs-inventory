package kg.gns.java.inventorysystem.infra.batch;

import kg.gns.java.inventorysystem.command.commands.UpdateProductQuantityCommand;
import kg.gns.java.inventorysystem.command.handlers.ProductCommandHandler;
import kg.gns.java.inventorysystem.infra.locking.DistributedLockManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BatchInventoryProcessor {
    @Autowired
    private ProductCommandHandler productCommandHandler;

    @Autowired
    private DistributedLockManager lockManager;

    @Async("commandExecutor")
    public CompletableFuture<Void> processBatchQuantityUpdates(List<UpdateProductQuantityCommand> commands) {
        return CompletableFuture.runAsync(() -> {
            // Group commands by product ID to avoid conflicts
            Map<Long, List<UpdateProductQuantityCommand>> groupedCommands = commands.stream()
                    .collect(Collectors.groupingBy(UpdateProductQuantityCommand::getProductId));

            groupedCommands.forEach((productId, productCommands) -> {
                String lockKey = "product:" + productId;

                try {
                    lockManager.executeWithLock(lockKey, () -> {
                        // Process all commands for this product sequentially
                        for (UpdateProductQuantityCommand command : productCommands) {
                            try {
                                productCommandHandler.handle(command);
                            } catch (Exception e) {
                                log.error("Error processing batch command for product {}: {}",
                                        productId, e.getMessage(), e);
                            }
                        }
                    });
                } catch (Exception e) {
                    log.error("Could not acquire lock for product {}: {}", productId, e.getMessage());
                }
            });

            log.info("Completed batch processing of {} quantity updates", commands.size());
        });
    }

    @Async("commandExecutor")
    public CompletableFuture<BatchResult> processBatchInventoryAdjustments(
            List<InventoryAdjustment> adjustments) {
        return CompletableFuture.supplyAsync(() -> {
            BatchResult result = new BatchResult();

            for (InventoryAdjustment adjustment : adjustments) {
                try {
                    String lockKey = "product:" + adjustment.getProductId();

                    lockManager.executeWithLock(lockKey, () -> {
                        UpdateProductQuantityCommand command = new UpdateProductQuantityCommand(
                                adjustment.getProductId(),
                                adjustment.getNewQuantity()
                        );
                        productCommandHandler.handle(command);
                    });

                    result.addSuccess(adjustment.getProductId());
                } catch (Exception e) {
                    log.error("Failed to process adjustment for product {}: {}",
                            adjustment.getProductId(), e.getMessage());
                    result.addFailure(adjustment.getProductId(), e.getMessage());
                }
            }

            log.info("Batch adjustment completed: {} success, {} failures",
                    result.getSuccessCount(), result.getFailureCount());

            return result;
        });
    }

    @Getter
    public static class InventoryAdjustment {
        // Getters
        private Long productId;
        private Integer newQuantity;
        private String reason;

        public InventoryAdjustment(Long productId, Integer newQuantity, String reason) {
            this.productId = productId;
            this.newQuantity = newQuantity;
            this.reason = reason;
        }

    }

    @Getter
    public static class BatchResult {
        private java.util.List<Long> successfulProducts = new java.util.ArrayList<>();
        private java.util.Map<Long, String> failedProducts = new java.util.HashMap<>();

        public void addSuccess(Long productId) {
            successfulProducts.add(productId);
        }

        public void addFailure(Long productId, String error) {
            failedProducts.put(productId, error);
        }

        public int getSuccessCount() { return successfulProducts.size(); }
        public int getFailureCount() { return failedProducts.size(); }
    }
}
