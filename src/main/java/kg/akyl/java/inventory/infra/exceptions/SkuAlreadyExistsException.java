package kg.akyl.java.inventory.infra.exceptions;

public class SkuAlreadyExistsException extends RuntimeException {
    public SkuAlreadyExistsException() {
    }

    public SkuAlreadyExistsException(String message) {
        super(message);
    }
}
