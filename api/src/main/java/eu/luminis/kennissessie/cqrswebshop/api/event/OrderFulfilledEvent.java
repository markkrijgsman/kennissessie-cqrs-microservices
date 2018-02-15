package eu.luminis.kennissessie.cqrswebshop.api.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.util.Assert;

import eu.luminis.kennissessie.cqrswebshop.api.ProductAmountDto;

public class OrderFulfilledEvent {

    private final List<ProductAmountDto> products;

    public OrderFulfilledEvent(List<ProductAmountDto> products) {
        Assert.isTrue(!products.isEmpty(), "Products cannot be empty");
        this.products = products;
    }

    public List<ProductAmountDto> getProducts() {
        return new ArrayList<>(products);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrderFulfilledEvent that = (OrderFulfilledEvent) o;
        return Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {

        return Objects.hash(products);
    }
}
