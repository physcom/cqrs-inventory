package kg.gns.java.inventorysystem.command.commands;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProcessSaleCommand {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String customerId;
}
