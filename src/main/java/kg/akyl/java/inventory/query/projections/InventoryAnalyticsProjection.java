package kg.akyl.java.inventory.query.projections;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryAnalyticsProjection {
    private String category;
    private Integer totalProducts;
    private Long totalQuantity;
    private Long totalReserved;
    private Long availableQuantity;
    private BigDecimal totalValue;
    private BigDecimal avgPrice;
    private Long salesLast30Days;
    private Long quantitySold30Days;
    private Double daysOfInventory;

    public InventoryAnalyticsProjection(String category, Integer totalProducts, Long totalQuantity,
                                        Long totalReserved, Long availableQuantity, BigDecimal totalValue,
                                        BigDecimal avgPrice, Long salesLast30Days, Long quantitySold30Days,
                                        Double daysOfInventory) {
        this.category = category;
        this.totalProducts = totalProducts;
        this.totalQuantity = totalQuantity;
        this.totalReserved = totalReserved;
        this.availableQuantity = availableQuantity;
        this.totalValue = totalValue;
        this.avgPrice = avgPrice;
        this.salesLast30Days = salesLast30Days;
        this.quantitySold30Days = quantitySold30Days;
        this.daysOfInventory = daysOfInventory;
    }
}
