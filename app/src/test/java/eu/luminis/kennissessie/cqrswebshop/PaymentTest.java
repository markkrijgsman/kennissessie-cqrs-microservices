package eu.luminis.kennissessie.cqrswebshop;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.luminis.kennissessie.cqrswebshop.api.command.CreatePaymentCommand;
import eu.luminis.kennissessie.cqrswebshop.api.command.DepositAmountCommand;
import eu.luminis.kennissessie.cqrswebshop.api.event.PaymentSuccessfulEvent;

@RunWith(MockitoJUnitRunner.class)
public class PaymentTest {

    @Mock
    private EventBus eventBusMock;

    @Captor
    private ArgumentCaptor<EventMessage<?>> eventMessageArgumentCaptor;

    @Test
    public void testPaymentSuccessfulInOneDepositWithoutTestFixture() {
        UUID paymentId = UUID.randomUUID();
        long orderTotal = 10;
        Payment payment = new Payment(new CreatePaymentCommand(paymentId, orderTotal));
        injectEventBusMock(payment);

        payment.handle(new DepositAmountCommand(paymentId, 10));

        verify(eventBusMock).publish(eventMessageArgumentCaptor.capture());

        assertEquals(new PaymentSuccessfulEvent(paymentId), eventMessageArgumentCaptor.getValue().getPayload());
    }

    @Test
    @Ignore
    public void testPaymentSuccessfulInOneDeposit() {
        UUID paymentId = UUID.randomUUID();
        long orderTotal = 10;


        new AggregateTestFixture<>(Payment.class)
                .givenCommands(new CreatePaymentCommand(paymentId, orderTotal))
                .when(new DepositAmountCommand(paymentId, 10))
                .expectEvents(new PaymentSuccessfulEvent(paymentId));
    }

    @Test
    public void testPaymentSuccessfulInMultipleDeposits() {
        UUID paymentId = UUID.randomUUID();
        long orderTotal = 10;
        Payment payment = new Payment(new CreatePaymentCommand(paymentId, orderTotal));
        injectEventBusMock(payment);

        payment.handle(new DepositAmountCommand(paymentId, 4));
        payment.handle(new DepositAmountCommand(paymentId, 4));
        payment.handle(new DepositAmountCommand(paymentId, 2));

        verify(eventBusMock).publish(eventMessageArgumentCaptor.capture());

        assertEquals(new PaymentSuccessfulEvent(paymentId), eventMessageArgumentCaptor.getValue().getPayload());
    }

    @Test(expected = IllegalStateException.class)
    public void testFailureInOverDeposit() {
        UUID paymentId = UUID.randomUUID();
        long orderTotal = 10;
        Payment payment = new Payment(new CreatePaymentCommand(paymentId, orderTotal));
        injectEventBusMock(payment);

        payment.handle(new DepositAmountCommand(paymentId, 4));
        payment.handle(new DepositAmountCommand(paymentId, 4));
        payment.handle(new DepositAmountCommand(paymentId, 5));
    }

    private void injectEventBusMock(Payment payment) {
        ReflectionTestUtils.setField(payment, "eventBus", eventBusMock);
    }

}