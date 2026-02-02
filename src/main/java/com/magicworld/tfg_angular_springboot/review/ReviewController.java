package com.magicworld.tfg_angular_springboot.review;

import com.magicworld.tfg_angular_springboot.exceptions.InvalidTokenException;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.magicworld.tfg_angular_springboot.user.UserController.getUser;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "The reviews management API")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @Operation(summary = "Get all reviews paginated")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews retrieved"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping
    public ResponseEntity<Page<ReviewDTO>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDTO> reviews = reviewService.findAllPaginated(pageable);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get purchases available for review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/available-purchases")
    public ResponseEntity<List<Long>> getAvailablePurchases() {
        User user = getUserFromContext();
        List<Long> purchaseIds = reviewService.getPurchasesAvailableForReview(user.getId());
        return ResponseEntity.ok(purchaseIds);
    }

    @Operation(summary = "Create a review")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created"),
            @ApiResponse(responseCode = "400", description = "Invalid data or already reviewed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @Valid @RequestBody ReviewRequest request) {
        User user = getUserFromContext();
        ReviewDTO created = reviewService.createReview(user, request);
        return ResponseEntity.created(URI.create("/api/v1/reviews/" + created.getId()))
                .body(created);
    }


    private User getUserFromContext() {
        return getUser(userRepository);
    }
}
