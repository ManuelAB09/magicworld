package com.magicworld.tfg_angular_springboot.purchase;

import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLine;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineService;
import com.magicworld.tfg_angular_springboot.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseLineService purchaseLineService;

    @Transactional(readOnly = true)
    public Purchase findById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.purchase.not_found"));
    }

    @Transactional(readOnly = true)
    public List<Purchase> findByBuyerId(Long buyerId) {
        return purchaseRepository.findByBuyerId(buyerId);
    }

    @Transactional
    public Purchase createPurchase(User buyer, List<PurchaseLine> lines) {
        Purchase purchase = Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(buyer)
                .build();

        Purchase savedPurchase = purchaseRepository.save(purchase);

        lines.forEach(line -> line.setPurchase(savedPurchase));
        purchaseLineService.saveAll(lines);

        return savedPurchase;
    }
}

