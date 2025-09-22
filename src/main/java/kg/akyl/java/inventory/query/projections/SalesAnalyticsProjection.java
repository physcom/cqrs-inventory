package kg.gns.java.inventorysystem.query.projections;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalesAnalyticsProjection {
    private String category;
    private Long totalSales;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
    private BigDecimal avgSaleAmount;
    private LocalDate saleDate;

    public SalesAnalyticsProjection(String category, Long totalSales, Long totalQuantity,
                                    BigDecimal totalRevenue, BigDecimal avgSaleAmount, LocalDate saleDate) {
        this.category = category;
        this.totalSales = totalSales;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
        this.avgSaleAmount = avgSaleAmount;
        this.saleDate = saleDate;
    }
}
