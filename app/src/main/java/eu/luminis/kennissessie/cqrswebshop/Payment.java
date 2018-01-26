package eu.luminis.kennissessie.cqrswebshop;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

import eu.luminis.kennissessie.cqrswebshop.api.command.CreatePaymentCommand;
import eu.luminis.kennissessie.cqrswebshop.api.command.DepositAmountCommand;

@Aggregate
@Entity
public class Payment {

    @Autowired
    private transient EventBus eventBus;

    @Id
    @AggregateIdentifier
    private UUID id;

    private boolean isPayed;

    private long amount;

    // JPA happy :)
    protected Payment(){

    }

    @CommandHandler
    public Payment(CreatePaymentCommand createPaymentCommand){
        id = createPaymentCommand.getPaymentId();
        amount = createPaymentCommand.getAmount();
        isPayed = false;
    }

    @CommandHandler
    public void handle(DepositAmountCommand addCreditCommand){
        // TODO implement
        if(addCreditCommand.getAmount() == 5){
            throw new IllegalStateException();
        }
    }

}