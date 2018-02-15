package eu.luminis.kennissessie.cqrswebshop;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.SagaLifecycle;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.springframework.beans.factory.annotation.Autowired;

import eu.luminis.kennissessie.cqrswebshop.api.ProductAmountDto;
import eu.luminis.kennissessie.cqrswebshop.api.command.CancelPaymentCommand;
import eu.luminis.kennissessie.cqrswebshop.api.command.CreatePaymentCommand;
import eu.luminis.kennissessie.cqrswebshop.api.command.ReserveStockCommand;
import eu.luminis.kennissessie.cqrswebshop.api.command.UndoStockReservationCommand;
import eu.luminis.kennissessie.cqrswebshop.api.event.OrderFulfilledEvent;
import eu.luminis.kennissessie.cqrswebshop.api.event.OrderRequestedEvent;
import eu.luminis.kennissessie.cqrswebshop.api.event.PaymentExpiredEvent;
import eu.luminis.kennissessie.cqrswebshop.api.event.PaymentSuccessfulEvent;

public class OrderManager {

    @Autowired
    private transient EventBus eventBus;

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient EventScheduler eventScheduler;

    private UUID paymentId;

    private List<ProductAmountDto> productAmountDtos = new ArrayList<>();

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRequestedEvent orderRequested){
        this.paymentId = orderRequested.getPaymentId();

        SagaLifecycle.associateWith("paymentId", this.paymentId.toString());

        productAmountDtos = orderRequested.getProductOrderDtos().stream().map(order -> new ProductAmountDto(order.getAmount(), order.getProductId())).collect(Collectors.toList());
        productAmountDtos.forEach(productOrder -> commandGateway.send(new ReserveStockCommand(productOrder.getProductId(), productOrder.getAmount())));
        commandGateway.send(new CreatePaymentCommand(paymentId, orderRequested.getOrderTotalPrice()));

        eventScheduler.schedule(Duration.ofMinutes(10), new PaymentExpiredEvent(paymentId));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "paymentId")
    public void handle(PaymentExpiredEvent paymentExpiredEvent){
        commandGateway.send(new CancelPaymentCommand(paymentExpiredEvent.getPaymentId()));

        productAmountDtos.forEach(productOrder -> commandGateway.send(new UndoStockReservationCommand(productOrder.getProductId(), productOrder.getAmount())));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "paymentId")
    public void handle(PaymentSuccessfulEvent paymentSuccessfulEvent){
        eventBus.publish(GenericEventMessage.asEventMessage(new OrderFulfilledEvent(productAmountDtos)));
    }



}
