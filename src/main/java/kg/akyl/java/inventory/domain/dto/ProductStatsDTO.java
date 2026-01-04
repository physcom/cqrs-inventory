package kg.akyl.java.inventory.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStatsDTO {
    private Long totalProducts;
    private Long inStockProducts;
    private Long lowStockProducts;
    private Long outOfStockProducts;

    private BigDecimal totalInventoryValue;
    private BigDecimal averageProductPrice;
    private BigDecimal totalCost;
    private BigDecimal totalRevenue;

    private Integer totalQuantity;
    private Long lowStockCount;
    private Long outOfStockCount;
    private Long categoriesCount;
    private Double stockTurnoverRate;
    private Double averageProfitMargin;

    // Category breakdown
    private Map<String, Long> productsByCategory;
    private Map<String, BigDecimal> valueByCategory;

    // Top performers
    private java.util.List<ProductPerformanceDTO> topSellingProducts;
    private java.util.List<ProductPerformanceDTO> topRevenueProducts;
    private java.util.List<ProductPerformanceDTO> lowStockAlerts;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProductPerformanceDTO {
    private Long productId;
    private String sku;
    private String name;
    private Integer quantity;
    private BigDecimal revenue;
    private BigDecimal profitMargin;
    private Integer unitsSold;
}