package kg.akyl.java.inventory.web;

import kg.akyl.java.inventory.command.commands.UpdateProductQuantityCommand;
import kg.akyl.java.inventory.infra.batch.BatchInventoryProcessor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bulk")
public class BulkOperationsController {
    @Autowired
    private BatchInventoryProcessor batchProcessor;

    @PostMapping("/inventory/adjustments")
    @Async("commandExecutor")
    public CompletableFuture<ResponseEntity<BatchInventoryProcessor.BatchResult>> processBulkAdjustments(
            @RequestBody List<BulkInventoryAdjustmentRequest> requests) {

        List<BatchInventoryProcessor.InventoryAdjustment> adjustments = requests.stream()
                .map(req -> new BatchInventoryProcessor.InventoryAdjustment(
                        req.getProductId(), req.getNewQuantity(), req.getReason()))
                .collect(Collectors.toList());

        return batchProcessor.processBatchInventoryAdjustments(adjustments)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/inventory/quantity-updates")
    @Async("commandExecutor")
    public CompletableFuture<ResponseEntity<String>> processBulkQuantityUpdates(
            @RequestBody List<BulkQuantityUpdateRequest> requests) {

        List<UpdateProductQuantityCommand> commands = requests.stream()
                .map(req -> new UpdateProductQuantityCommand(req.getProductId(), req.getNewQuantity()))
                .collect(Collectors.toList());

        return batchProcessor.processBatchQuantityUpdates(commands)
                .thenApply(v -> ResponseEntity.ok("Batch processing initiated for " + commands.size() + " updates"));
    }

    @Setter
    @Getter
    public static class BulkInventoryAdjustmentRequest {
        // Getters and setters
        private Long productId;
        private Integer newQuantity;
        private String reason;

    }

    @Setter
    @Getter
    public static class BulkQuantityUpdateRequest {
        private Long productId;
        private Integer newQuantity;

    }
}
