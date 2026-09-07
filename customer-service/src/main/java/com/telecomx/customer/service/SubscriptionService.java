package com.telecomx.customer.service;

import com.telecomx.customer.domain.*;
import com.telecomx.customer.dto.SubscribeRequest;
import com.telecomx.customer.dto.SubscriptionResponse;
import com.telecomx.customer.event.SubscriptionEventPublisher;
import com.telecomx.customer.event.SubscriptionRequestedEvent;
import com.telecomx.customer.exception.ResourceNotFoundException;
import com.telecomx.customer.repository.CustomerRepository;
import com.telecomx.customer.repository.PlanRepository;
import com.telecomx.customer.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionEventPublisher eventPublisher;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, PlanRepository planRepository,
                                CustomerRepository customerRepository, SubscriptionEventPublisher eventPublisher) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SubscriptionResponse subscribe(Long customerId, SubscribeRequest req) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Plan plan = planRepository.findByCode(req.planCode())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + req.planCode()));

        Subscription subscription = Subscription.builder()
                .customerId(customer.getId())
                .planId(plan.getId())
                .status(SubscriptionStatus.PENDING)
                .build();
        subscription = subscriptionRepository.save(subscription);

        // Fire-and-forget async event -> Provisioning Service picks this up and
        // does the actual (potentially slow) SIM/number activation work.
        eventPublisher.publish(new SubscriptionRequestedEvent(
                subscription.getId(), customer.getId(), plan.getCode(), customer.getPhoneNumber()));

        return toResponse(subscription, plan.getCode());
    }

    public List<SubscriptionResponse> getForCustomer(Long customerId) {
        Map<Long, String> planCodes = planRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Plan::getId, Plan::getCode));
        return subscriptionRepository.findByCustomerId(customerId).stream()
                .map(s -> toResponse(s, planCodes.get(s.getPlanId())))
                .toList();
    }

    private SubscriptionResponse toResponse(Subscription s, String planCode) {
        return new com.telecomx.customer.dto.SubscriptionResponse(
                s.getId(), s.getCustomerId(), planCode, s.getStatus(), s.getMsisdn(), s.getRequestedAt());
    }
}
