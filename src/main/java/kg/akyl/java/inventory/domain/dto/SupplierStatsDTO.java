package kg.akyl.java.inventory.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierStatsDTO {
    private Long totalSuppliers;
    private Long activeSuppliers;
    private Long inactiveSuppliers;
    private Long suspendedSuppliers;
    private Long pendingApprovalSuppliers;

    private BigDecimal averageSupplierRating;
    private Double averagePaymentTerms; // in days
    private BigDecimal totalCreditLimit;

    // Supplier type distribution
    private Map<String, Long> suppliersByType;
    private Map<String, BigDecimal> transactionValueByType;

    // Geographic distribution
    private Map<String, Long> suppliersByCountry;
    private Map<String, Long> suppliersByCity;

    // Performance metrics
    private List<SupplierPerformanceDTO> topRatedSuppliers;
    private List<SupplierPerformanceDTO> topVolumeSuppliers;
    private List<SupplierPerformanceDTO> fastestDeliverySuppliers;

    // Risk metrics
    private Integer suppliersWithNoBackup;
    private Integer suppliersWithOverduePayments;
    private Integer suppliersWithQualityIssues;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SupplierPerformanceDTO {
    private Long supplierId;
    private String supplierCode;
    private String name;
    private String supplierType;
    private BigDecimal rating;
    private BigDecimal totalTransactionValue;
    private Integer totalTransactions;
    private Double averageLeadTime; // in days
    private Double onTimeDeliveryRate; // percentage
    private Integer productCount;
}