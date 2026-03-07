package com.magicworld.tfg_angular_springboot.monitoring.simulator;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.monitoring.dto.EventRequest;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import com.magicworld.tfg_angular_springboot.monitoring.service.AlertService;
import com.magicworld.tfg_angular_springboot.monitoring.service.DashboardService;
import com.magicworld.tfg_angular_springboot.monitoring.service.EventIngestionService;
import com.magicworld.tfg_angular_springboot.monitoring.service.MonitoringWebSocketService;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkSimulatorService {

    private final EventIngestionService eventService;
    private final DashboardService dashboardService;
    private final AttractionRepository attractionRepository;
    private final MonitoringWebSocketService webSocketService;
    private final AlertService alertService;
    private final PurchaseLineService purchaseLineService;

    @Value("${park.max-capacity:500}")
    private int parkMaxCapacity;

    private final Random random = new Random();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Map<Long, AtomicInteger> simulatedQueues = new ConcurrentHashMap<>();
    private final AtomicInteger simulatedVisitors = new AtomicInteger(0);
    private volatile int maxVisitorsForSession = 0;
    private int tickCount = 0;

    @PostConstruct
    public void init() {
        dashboardService.initializeAttractionStates();
    }

    public void start() {
        running.set(true);
        int ticketsSold = purchaseLineService.getTotalSoldForDate(java.time.LocalDate.now());
        maxVisitorsForSession = ticketsSold > 0 ? Math.min(ticketsSold, parkMaxCapacity) : parkMaxCapacity;
        int initialVisitors = ticketsSold > 0 ? Math.min(ticketsSold, parkMaxCapacity) : 50 + random.nextInt(100);
        initialVisitors = Math.min(initialVisitors, maxVisitorsForSession);
        simulatedVisitors.set(initialVisitors);
        initializeQueues();
        log.info("Simulador iniciado con {} visitantes (tickets vendidos: {}, máximo sesión: {})",
                simulatedVisitors.get(), ticketsSold, maxVisitorsForSession);
    }

    public void stop() {
        running.set(false);
        log.info("Simulador detenido");
    }

    public boolean isRunning() {
        return running.get();
    }

    private void initializeQueues() {
        List<Attraction> activeAttractions = attractionRepository.findAll().stream()
                .filter(Attraction::getIsActive)
                .toList();

        // Distribute visitors across active queues without exceeding the ticket cap
        int budget = maxVisitorsForSession > 0 ? maxVisitorsForSession : simulatedVisitors.get();
        int remaining = budget;

        for (Attraction a : activeAttractions) {
            int maxForThis = Math.max(0, remaining);
            int initialQueue = maxForThis > 0 ? Math.min(5 + random.nextInt(Math.min(25, maxForThis)), maxForThis) : 0;
            simulatedQueues.put(a.getId(), new AtomicInteger(initialQueue));
            dashboardService.updateAttractionState(a.getId(), ParkEventType.ATTRACTION_OPEN, null);
            dashboardService.updateAttractionState(a.getId(), ParkEventType.ATTRACTION_QUEUE_JOIN, initialQueue);
            remaining -= initialQueue;
        }

        // Inactive attractions get zero
        attractionRepository.findAll().stream()
                .filter(a -> !a.getIsActive())
                .forEach(a -> {
                    simulatedQueues.put(a.getId(), new AtomicInteger(0));
                    dashboardService.updateAttractionState(a.getId(), ParkEventType.ATTRACTION_CLOSE, null);
                    dashboardService.updateAttractionState(a.getId(), ParkEventType.ATTRACTION_QUEUE_JOIN, 0);
                });
    }

    @Scheduled(fixedRate = 2000)
    public void simulateParkActivity() {
        if (!running.get())
            return;

        tickCount++;
        simulateVisitorFlow();
        simulateAllQueues();

        if (tickCount % 5 == 0 && random.nextInt(100) < 50) {
            generateRandomAlert();
        }

        broadcastUpdate();
    }

    private void generateRandomAlert() {
        List<Attraction> attractions = attractionRepository.findAll();
        if (attractions.isEmpty())
            return;
        Attraction target = attractions.get(random.nextInt(attractions.size()));
        alertService.generateRandomAlert(target.getId());
    }

    private void simulateVisitorFlow() {
        int hour = LocalDateTime.now().getHour();
        int entryRate = calculateEntryRate(hour);
        int exitRate = calculateExitRate(hour);

        int currentVisitors = simulatedVisitors.get();

        // Only allow entries if below the ticket cap
        if (currentVisitors < maxVisitorsForSession) {
            int maxEntries = maxVisitorsForSession - currentVisitors;
            int entries = Math.min(random.nextInt(entryRate) + 1, maxEntries);
            for (int i = 0; i < entries; i++) {
                recordEvent(ParkEventType.PARK_ENTRY, null, null);
                simulatedVisitors.incrementAndGet();
            }
        }

        if (simulatedVisitors.get() > 20) {
            int exits = random.nextInt(exitRate);
            for (int i = 0; i < exits && simulatedVisitors.get() > 10; i++) {
                recordEvent(ParkEventType.PARK_EXIT, null, null);
                simulatedVisitors.decrementAndGet();
            }
        }
    }

    private int calculateEntryRate(int hour) {
        if (hour >= 10 && hour <= 14)
            return 8;
        if (hour >= 15 && hour <= 18)
            return 5;
        return 3;
    }

    private int calculateExitRate(int hour) {
        if (hour >= 18)
            return 6;
        if (hour >= 15)
            return 4;
        return 2;
    }

    private void simulateAllQueues() {
        List<Attraction> attractions = attractionRepository.findAll();
        int totalInQueues = simulatedQueues.values().stream().mapToInt(AtomicInteger::get).sum();

        for (Attraction attraction : attractions) {
            if (!attraction.getIsActive())
                continue;

            AtomicInteger queueCounter = simulatedQueues.computeIfAbsent(
                    attraction.getId(), k -> new AtomicInteger(10));

            int currentQueue = queueCounter.get();
            int change = calculateQueueChange(attraction, currentQueue);

            // If change is positive, ensure total queues don't exceed ticket cap
            if (change > 0 && maxVisitorsForSession > 0) {
                int headroom = maxVisitorsForSession - totalInQueues;
                change = Math.min(change, Math.max(0, headroom));
            }

            int newQueue = Math.max(0, currentQueue + change);
            queueCounter.set(newQueue);
            totalInQueues += (newQueue - currentQueue);

            ParkEventType eventType = change > 0
                    ? ParkEventType.ATTRACTION_QUEUE_JOIN
                    : ParkEventType.ATTRACTION_QUEUE_LEAVE;

            recordEvent(eventType, attraction.getId(), newQueue);
            dashboardService.updateAttractionState(attraction.getId(), eventType, newQueue);
        }
    }

    private int calculateQueueChange(Attraction attraction, int currentQueue) {
        int baseChange = random.nextInt(7) - 3;

        String intensity = attraction.getIntensity().name();
        if ("HIGH".equals(intensity) && currentQueue < 60) {
            baseChange += random.nextInt(3);
        }

        if (currentQueue > 80) {
            baseChange -= random.nextInt(4);
        } else if (currentQueue < 10) {
            baseChange += random.nextInt(3);
        }

        return baseChange;
    }

    private void recordEvent(ParkEventType type, Long attractionId, Integer queueSize) {
        EventRequest req = new EventRequest();
        req.setEventType(type);
        req.setAttractionId(attractionId);
        req.setQueueSize(queueSize);
        req.setVisitorCount(1);
        eventService.recordEvent(req);
    }

    private void broadcastUpdate() {
        webSocketService.broadcastDashboard(dashboardService.getSnapshot());
    }

    public Map<String, Object> getSimulatorStatus() {
        return Map.of(
                "running", running.get(),
                "simulatedVisitors", simulatedVisitors.get(),
                "activeQueues", simulatedQueues.size(),
                "totalInQueues", simulatedQueues.values().stream().mapToInt(AtomicInteger::get).sum());
    }

    public void closeAttraction(Long attractionId) {
        AtomicInteger closedQueue = simulatedQueues.get(attractionId);
        int redistributeCount = closedQueue != null ? closedQueue.getAndSet(0) : 0;

        if (redistributeCount > 0) {
            List<Long> openIds = attractionRepository.findByIsActiveTrue().stream()
                    .map(Attraction::getId)
                    .filter(id -> !id.equals(attractionId))
                    .toList();

            if (!openIds.isEmpty()) {
                int perAttraction = redistributeCount / openIds.size();
                int remainder = redistributeCount % openIds.size();

                for (int i = 0; i < openIds.size(); i++) {
                    int extra = i < remainder ? 1 : 0;
                    AtomicInteger q = simulatedQueues.computeIfAbsent(openIds.get(i),
                            k -> new AtomicInteger(0));
                    q.addAndGet(perAttraction + extra);
                    dashboardService.updateAttractionState(openIds.get(i),
                            ParkEventType.ATTRACTION_QUEUE_JOIN, q.get());
                }
            }
        }

        if (running.get()) {
            broadcastUpdate();
        }
    }

    public void reopenAttraction(Long attractionId) {
        int totalInQueues = simulatedQueues.values().stream().mapToInt(AtomicInteger::get).sum();
        int headroom = maxVisitorsForSession > 0 ? Math.max(0, maxVisitorsForSession - totalInQueues) : 20;
        int initialQueue = Math.min(5 + random.nextInt(15), headroom);
        simulatedQueues.computeIfAbsent(attractionId, k -> new AtomicInteger(0)).set(initialQueue);
        dashboardService.updateAttractionState(attractionId, ParkEventType.ATTRACTION_QUEUE_JOIN, initialQueue);

        if (running.get()) {
            broadcastUpdate();
        }
    }
}
