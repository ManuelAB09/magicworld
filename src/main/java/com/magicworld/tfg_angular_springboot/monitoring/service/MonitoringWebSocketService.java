package com.magicworld.tfg_angular_springboot.monitoring.service;

import com.magicworld.tfg_angular_springboot.monitoring.dto.AlertDTO;
import com.magicworld.tfg_angular_springboot.monitoring.dto.AttractionStatus;
import com.magicworld.tfg_angular_springboot.monitoring.dto.DashboardSnapshot;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonitoringWebSocketService {

    private static final String TOPIC_DASHBOARD = "/topic/dashboard";
    private static final String TOPIC_EVENTS = "/topic/events";
    private static final String TOPIC_ALERTS = "/topic/alerts";
    private static final String TOPIC_ATTRACTION = "/topic/attraction/";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastDashboard(DashboardSnapshot snapshot) {
        messagingTemplate.convertAndSend(TOPIC_DASHBOARD, snapshot);
    }

    public void broadcastEvent(ParkEvent event) {
        messagingTemplate.convertAndSend(TOPIC_EVENTS, event);
    }

    public void broadcastAlert(AlertDTO alert) {
        messagingTemplate.convertAndSend(TOPIC_ALERTS, alert);
    }

    public void broadcastAttractionUpdate(Long attractionId, AttractionStatus status) {
        messagingTemplate.convertAndSend(TOPIC_ATTRACTION + attractionId, status);
    }
}
