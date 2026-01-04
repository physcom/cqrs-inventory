package kg.akyl.java.inventory.domain.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import kg.akyl.java.inventory.domain.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;

    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal price;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.00", message = "Cost must be non-negative")
    private BigDecimal cost;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private Integer minimumStock;
    private Integer maximumStock;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private Integer reorderPoint;
    private Integer reorderQuantity;

    private String stockStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK

    private String unit; // pcs, kg, liters, etc.
    private String barcode;
    private String location;
    private String manufacturer;
    private String brand;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long version;

    // Calculated fields
    private BigDecimal totalValue; // quantity * price
    private BigDecimal profitMargin; // ((price - cost) / price) * 100
    private Integer daysUntilReorder; // estimated based on sales velocity

    private String status;
}
