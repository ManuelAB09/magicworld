package com.magicworld.tfg_angular_springboot.purchase;

import com.magicworld.tfg_angular_springboot.exceptions.InvalidTokenException;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLine;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.magicworld.tfg_angular_springboot.user.UserController.getUser;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "The purchases management API")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseLineRepository purchaseLineRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get current user's purchases")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchases retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-purchases")
    public ResponseEntity<List<PurchaseDTO>> getMyPurchases() {
        User user = getUserFromContext();
        List<Purchase> purchases = purchaseService.findByBuyerId(user.getId());

        List<PurchaseDTO> dtos = purchases.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private PurchaseDTO toDTO(Purchase purchase) {
        List<PurchaseLine> lines = purchaseLineRepository.findByPurchaseId(purchase.getId());

        List<PurchaseLineDTO> lineDTOs = lines.stream()
                .map(line -> PurchaseLineDTO.builder()
                        .id(line.getId())
                        .validDate(line.getValidDate())
                        .quantity(line.getQuantity())
                        .totalCost(line.getTotalCost())
                        .ticketTypeName(line.getTicketTypeName())
                        .build())
                .collect(Collectors.toList());

        return PurchaseDTO.builder()
                .id(purchase.getId())
                .purchaseDate(purchase.getPurchaseDate())
                .lines(lineDTOs)
                .build();
    }

    private User getUserFromContext() {
        return getUser(userRepository);
    }
}
