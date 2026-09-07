package com.telecomx.notification.kafka;

import com.telecomx.notification.domain.NotificationChannel;
import com.telecomx.notification.event.InvoiceGeneratedEvent;
import com.telecomx.notification.event.PaymentFailedEvent;
import com.telecomx.notification.event.SubscriberProvisionedEvent;
import com.telecomx.notification.service.NotificationDeliveryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * A single consumer class for all three inbound topics, since this service's whole
 * job is "react to an event by sending a message" - splitting into more classes
 * wouldn't add clarity. Each of the three producers (Provisioning, Billing) is
 * completely decoupled from whether/how notification delivery succeeds.
 */
@Component
public class NotificationEventListener {

    private final NotificationDeliveryService deliveryService;

    public NotificationEventListener(NotificationDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @KafkaListener(topics = "${topics.subscriber-provisioned}", groupId = "notification-service")
    public void onSubscriberProvisioned(SubscriberProvisionedEvent event) {
        deliveryService.send(event.customerId(), NotificationChannel.SMS,
                "Service Activated",
                "Your " + event.planCode() + " plan is now active on " + event.msisdn() + ". Welcome to TelecomX!");
    }

    @KafkaListener(topics = "${topics.invoice-generated}", groupId = "notification-service")
    public void onInvoiceGenerated(InvoiceGeneratedEvent event) {
        deliveryService.send(event.customerId(), NotificationChannel.EMAIL,
                "Your TelecomX Invoice",
                "Invoice #" + event.invoiceId() + " for ₹" + event.totalAmount() + " has been generated.");
    }

    @KafkaListener(topics = "${topics.payment-failed}", groupId = "notification-service")
    public void onPaymentFailed(PaymentFailedEvent event) {
        deliveryService.send(event.customerId(), NotificationChannel.SMS,
                "Payment Failed",
                "We couldn't process payment for invoice #" + event.invoiceId() +
                " (attempt " + event.consecutiveFailures() + "). Please update your payment method.");
    }
}
