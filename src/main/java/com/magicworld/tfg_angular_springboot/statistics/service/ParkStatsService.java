package com.magicworld.tfg_angular_springboot.statistics.service;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventRepository;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.statistics.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ParkStatsService {

    private final PurchaseLineRepository purchaseLineRepository;
    private final ParkEventRepository parkEventRepository;
    private final AttractionRepository attractionRepository;

    @Transactional(readOnly = true)
    public TicketSalesDTO getTicketSales(LocalDate from, LocalDate to, String locale) {
        int totalSold = purchaseLineRepository.sumQuantityByDateRange(from, to);
        BigDecimal totalRevenue = purchaseLineRepository.sumRevenueByDateRange(from, to);

        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        return TicketSalesDTO.builder()
                .totalTicketsSold(totalSold)
                .totalRevenue(CurrencyConverter.convert(totalRevenue, locale))
                .currency(CurrencyConverter.getCurrency(locale))
                .build();
    }

    @Transactional(readOnly = true)
    public List<MonthlySalesDTO> getSeasonalBreakdown(int year, String locale) {
        List<Object[]> rawData = purchaseLineRepository.findMonthlySalesByYear(year);

        Map<Integer, MonthlySalesDTO> monthMap = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            String monthName = Month.of(m).getDisplayName(
                    TextStyle.FULL,
                    isEnglish(locale) ? Locale.ENGLISH : Locale.of("es", "ES"));

            monthMap.put(m, MonthlySalesDTO.builder()
                    .month(m)
                    .monthName(monthName)
                    .ticketsSold(0)
                    .revenue(BigDecimal.ZERO)
                    .build());
        }

        for (Object[] row : rawData) {
            int month = ((Number) row[0]).intValue();
            int tickets = ((Number) row[1]).intValue();
            BigDecimal revenue = row[2] instanceof BigDecimal bd ? bd : new BigDecimal(row[2].toString());

            MonthlySalesDTO dto = monthMap.get(month);
            dto.setTicketsSold(tickets);
            dto.setRevenue(CurrencyConverter.convert(revenue, locale));
        }

        return new ArrayList<>(monthMap.values());
    }

    @Transactional(readOnly = true)
    public List<AttractionPerformanceDTO> getAttractionPerformance(LocalDate from, LocalDate to) {
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toTime = to.plusDays(1).atStartOfDay();

        List<Object[]> rawData = parkEventRepository.findAttractionPerformanceStats(fromTime, toTime);

        Map<Long, Attraction> attractionMap = new HashMap<>();
        attractionRepository.findAll().forEach(a -> attractionMap.put(a.getId(), a));

        return rawData.stream()
                .map(row -> {
                    Long attractionId = ((Number) row[0]).longValue();
                    long totalEvents = ((Number) row[1]).longValue();
                    int maxQueue = ((Number) row[2]).intValue();
                    double avgQueue = ((Number) row[3]).doubleValue();

                    Attraction attraction = attractionMap.get(attractionId);
                    String name = attraction != null ? attraction.getName() : "Unknown (#" + attractionId + ")";

                    return AttractionPerformanceDTO.builder()
                            .attractionId(attractionId)
                            .attractionName(name)
                            .totalQueueEvents(totalEvents)
                            .maxQueueSize(maxQueue)
                            .avgQueueSize(Math.round(avgQueue * 100.0) / 100.0)
                            .build();
                })
                .toList();
    }

    private boolean isEnglish(String locale) {
        return locale != null && locale.toLowerCase().startsWith("en");
    }
}

