package eu.luminis.kennissessie.cqrswebshop;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

import eu.luminis.kennissessie.cqrswebshop.api.command.CreateProductCommand;
import eu.luminis.kennissessie.cqrswebshop.api.command.ReserveStockCommand;
import eu.luminis.kennissessie.cqrswebshop.api.command.UndoStockReservationCommand;
import eu.luminis.kennissessie.cqrswebshop.api.event.ProductFreedEvent;
import eu.luminis.kennissessie.cqrswebshop.api.event.ProductReservedEvent;

@Aggregate
@Entity
public class Product {

    @Autowired
    private transient EventBus eventBus;

    @Id
    @AggregateIdentifier
    private UUID id;

    private long amount;

    // JPA happy :)
    protected Product(){

    }

    @CommandHandler
    public Product(CreateProductCommand createProductCommand){
        id = createProductCommand.getProductId();
        amount = createProductCommand.getInitialAmountInStock();
    }

    @CommandHandler
    public void handle(ReserveStockCommand reserveStockCommand){
        if(amount - reserveStockCommand.getAmount() < 0) {
            throw new IllegalStateException("Not enough stock available");
        }
        amount -= reserveStockCommand.getAmount();
        eventBus.publish(GenericEventMessage.asEventMessage(new ProductReservedEvent(id)));
    }

    @CommandHandler
    public void handle(UndoStockReservationCommand undoStockReservationCommand){
        amount += undoStockReservationCommand.getAmount();
        eventBus.publish(GenericEventMessage.asEventMessage(new ProductFreedEvent(id)));
    }
}
