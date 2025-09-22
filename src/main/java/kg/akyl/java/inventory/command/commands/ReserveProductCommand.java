package kg.akyl.java.inventory.command.commands;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReserveProductCommand {
    private Long productId;
    private Integer quantity;
    private String customerId;
}
