package kg.akyl.java.inventory.domain;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "supply_chain_transactions", indexes = {
        @Index(name = "idx_transaction_number", columnList = "transaction_number", unique = true),
        @Index(name = "idx_from_supplier", columnList = "from_supplier_id"),
        @Index(name = "idx_to_supplier", columnList = "to_supplier_id"),
        @Index(name = "idx_product", columnList = "product_id"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyChainTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Transaction number is required")
    @Size(max = 50)
    @Column(name = "transaction_number", nullable = false, unique = true, length = 50)
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_supplier_id", nullable = false)
    private Supplier fromSupplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_supplier_id", nullable = false)
    private Supplier toSupplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "Total amount is required")
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionType transactionType = TransactionType.SALE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @NotNull(message = "Transaction date is required")
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @Size(max = 50)
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Size(max = 50)
    @Column(name = "purchase_order_number", length = 50)
    private String purchaseOrderNumber;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Size(max = 20)
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public enum TransactionType {
        SALE,
        PURCHASE,
        TRANSFER,
        RETURN
    }

    public enum TransactionStatus {
        PENDING,
        CONFIRMED,
        IN_TRANSIT,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }

    /**
     * Calculate total amount before persisting
     */
    @PrePersist
    @PreUpdate
    public void calculateTotalAmount() {
        if (unitPrice != null && quantity != null) {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal total = subtotal;

            if (shippingCost != null) {
                total = total.add(shippingCost);
            }
            if (taxAmount != null) {
                total = total.add(taxAmount);
            }
            if (discountAmount != null) {
                total = total.subtract(discountAmount);
            }

            this.totalAmount = total;
        }
    }
}
