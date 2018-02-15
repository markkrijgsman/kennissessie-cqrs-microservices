package eu.luminis.kennissessie.cqrswebshop.api;

import java.util.Objects;
import java.util.UUID;

public class ProductAmountDto {

    private final long amount;

    private final UUID productId;

    public ProductAmountDto(long amount, UUID productId) {
        this.amount = amount;
        this.productId = productId;
    }

    public long getAmount() {
        return amount;
    }

    public UUID getProductId() {
        return productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProductAmountDto that = (ProductAmountDto) o;
        return amount == that.amount && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(amount, productId);
    }
}
