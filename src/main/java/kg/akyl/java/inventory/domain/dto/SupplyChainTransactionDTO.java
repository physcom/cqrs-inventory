package kg.akyl.java.inventory.domain.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyChainTransactionDTO {
    private Long id;

    @NotBlank(message = "Transaction number is required")
    private String transactionNumber;

    @NotNull(message = "From supplier is required")
    private Long fromSupplierId;

    private String fromSupplierName;
    private String fromSupplierCode;

    @NotNull(message = "To supplier is required")
    private Long toSupplierId;

    private String toSupplierName;
    private String toSupplierCode;

    @NotNull(message = "Product is required")
    private Long productId;

    private String productName;
    private String productSku;

    @NotNull(message = "Quantity is required")
    @Min(value = 1)
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01")
    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private String transactionType;
    private String status;

    @NotNull(message = "Transaction date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime transactionDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expectedDeliveryDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime actualDeliveryDate;

    private String invoiceNumber;
    private String purchaseOrderNumber;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paymentDate;

    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long version;
}


// Network Node DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class NetworkNode {
    private Long id;
    private String name;
    private String code;
    private String type;
    private String status;
    private BigDecimal rating;
    private Integer outgoingTransactions;
    private Integer incomingTransactions;
    private BigDecimal totalVolume;
}

// Network Edge DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class NetworkEdge {
    private Long fromId;
    private Long toId;
    private String fromName;
    private String toName;
    private Integer transactionCount;
    private BigDecimal totalValue;
}

// Network Statistics DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class NetworkStatistics {
    private Integer totalSuppliers;
    private Integer totalConnections;
    private Integer totalTransactions;
    private BigDecimal totalVolume;
    private BigDecimal averageTransactionValue;
    private Integer activeSuppliers;
    private Integer isolatedSuppliers;
}
