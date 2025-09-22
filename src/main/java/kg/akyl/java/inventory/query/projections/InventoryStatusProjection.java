package kg.akyl.java.inventory.query.projections;

import lombok.Data;

@Data
public class InventoryStatusProjection {
    private Long id;
    private String sku;
    private String name;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;

    public InventoryStatusProjection(Long id, String sku, String name,
                                     Integer quantity, Integer reservedQuantity, Integer availableQuantity) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
    }
}
