package kg.akyl.java.inventory.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatsDTO {
    private Long totalTransactions;
    private BigDecimal totalTransactionValue;
    private BigDecimal averageTransactionValue;

    // Status breakdown
    private Long pendingTransactions;
    private Long confirmedTransactions;
    private Long inTransitTransactions;
    private Long deliveredTransactions;
    private Long cancelledTransactions;
    private Long refundedTransactions;

    // Type breakdown
    private Map<String, Long> transactionsByType;
    private Map<String, BigDecimal> valueByType;

    // Time-based metrics
    private List<MonthlyTransactionDTO> monthlyTransactions;
    private List<DailyTransactionDTO> dailyTransactions;

    // Performance metrics
    private Double averageLeadTime; // in days
    private Double onTimeDeliveryRate; // percentage
    private Integer overdueDeliveries;

    // Financial metrics
    private BigDecimal totalShippingCost;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalDiscountAmount;
    private BigDecimal netTransactionValue;

    // Top connections
    private List<SupplierConnectionDTO> topSupplierConnections;
    private List<ProductTransactionDTO> topTransactedProducts;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class MonthlyTransactionDTO {
    private String month; // YYYY-MM
    private Long transactionCount;
    private BigDecimal totalValue;
    private Double averageValue;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DailyTransactionDTO {
    private LocalDateTime date;
    private Long transactionCount;
    private BigDecimal totalValue;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SupplierConnectionDTO {
    private Long fromSupplierId;
    private String fromSupplierName;
    private Long toSupplierId;
    private String toSupplierName;
    private Integer transactionCount;
    private BigDecimal totalValue;
    private Double averageValue;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProductTransactionDTO {
    private Long productId;
    private String productSku;
    private String productName;
    private Integer totalQuantity;
    private BigDecimal totalValue;
    private Integer transactionCount;
}