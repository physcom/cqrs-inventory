package kg.akyl.java.inventory.domain.request;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a new supply chain transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionRequest {
    @NotNull(message = "From supplier is required")
    private Long fromSupplierId;

    @NotNull(message = "To supplier is required")
    private Long toSupplierId;

    @NotNull(message = "Product is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @NotNull(message = "Transaction type is required")
    private String transactionType; // SALE, PURCHASE, TRANSFER, RETURN

    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;

    private LocalDateTime expectedDeliveryDate;

    @Size(max = 50)
    private String invoiceNumber;

    @Size(max = 50)
    private String purchaseOrderNumber;

    @DecimalMin(value = "0.00")
    private BigDecimal shippingCost;

    @DecimalMin(value = "0.00")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal discountAmount;

    @Size(max = 20)
    private String paymentMethod;

    @Size(max = 1000)
    private String notes;
}

/**
 * Request DTO for updating a transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UpdateTransactionRequest {

    private String status; // PENDING, CONFIRMED, IN_TRANSIT, DELIVERED, CANCELLED

    private LocalDateTime expectedDeliveryDate;

    private LocalDateTime actualDeliveryDate;

    @Size(max = 50)
    private String invoiceNumber;

    @DecimalMin(value = "0.00")
    private BigDecimal shippingCost;

    @DecimalMin(value = "0.00")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal discountAmount;

    @Size(max = 20)
    private String paymentMethod;

    private LocalDateTime paymentDate;

    @Size(max = 1000)
    private String notes;
}