package com.magicworld.tfg_angular_springboot.purchase_line;

import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseLineService {

    private final PurchaseLineRepository purchaseLineRepository;
    private final TicketTypeService ticketTypeService;

    @Transactional(readOnly = true)
    public List<PurchaseLine> findByPurchaseId(Long purchaseId) {
        return purchaseLineRepository.findByPurchaseId(purchaseId);
    }

    @Transactional(readOnly = true)
    public int getSoldQuantity(String ticketTypeName, LocalDate date) {
        return purchaseLineRepository.sumQuantityByTicketTypeNameAndValidDate(ticketTypeName, date);
    }

    @Transactional(readOnly = true)
    public int getAvailableQuantity(String ticketTypeName, LocalDate date) {
        TicketType ticketType = ticketTypeService.findByTypeName(ticketTypeName);
        int sold = getSoldQuantity(ticketTypeName, date);
        return Math.max(0, ticketType.getMaxPerDay() - sold);
    }

    @Transactional
    public PurchaseLine save(PurchaseLine purchaseLine) {
        return purchaseLineRepository.save(purchaseLine);
    }

    @Transactional
    public List<PurchaseLine> saveAll(List<PurchaseLine> lines) {
        return purchaseLineRepository.saveAll(lines);
    }
}

