package com.telecomx.notification.controller;

import com.telecomx.notification.domain.NotificationRecord;
import com.telecomx.notification.service.NotificationDeliveryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationDeliveryService deliveryService;

    public NotificationController(NotificationDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // Powers the admin dashboard's "recent notifications" feed.
    @GetMapping("/recent")
    public List<NotificationRecord> recent() {
        return deliveryService.getRecent();
    }
}
