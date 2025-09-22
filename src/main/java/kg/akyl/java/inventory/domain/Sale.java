package kg.gns.java.inventorysystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sale_product", columnList = "productId"),
        @Index(name = "idx_sale_date", columnList = "saleDate"),
        @Index(name = "idx_sale_status", columnList = "status")
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sale_seq")
    @SequenceGenerator(name = "sale_seq", sequenceName = "sale_sequence", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 100)
    private String customerId;

    @Enumerated(EnumType.STRING)
    private SaleStatus status;

    @Column(nullable = false)
    private LocalDateTime saleDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (saleDate == null) {
            saleDate = LocalDateTime.now();
        }
        totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Sale(Long productId, Integer quantity, BigDecimal unitPrice,
                String customerId, SaleStatus status) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.customerId = customerId;
        this.status = status;
    }
}
