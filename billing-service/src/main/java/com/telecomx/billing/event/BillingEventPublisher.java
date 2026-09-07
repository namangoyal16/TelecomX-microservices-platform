package com.telecomx.billing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BillingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BillingEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String invoiceGeneratedTopic;
    private final String paymentFailedTopic;

    public BillingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${topics.invoice-generated}") String invoiceGeneratedTopic,
                                  @Value("${topics.payment-failed}") String paymentFailedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.invoiceGeneratedTopic = invoiceGeneratedTopic;
        this.paymentFailedTopic = paymentFailedTopic;
    }

    public void publishInvoiceGenerated(InvoiceGeneratedEvent event) {
        kafkaTemplate.send(invoiceGeneratedTopic, String.valueOf(event.customerId()), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish invoice.generated for customer {}", event.customerId(), ex);
                });
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send(paymentFailedTopic, String.valueOf(event.customerId()), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish payment.failed for customer {}", event.customerId(), ex);
                });
    }
}
