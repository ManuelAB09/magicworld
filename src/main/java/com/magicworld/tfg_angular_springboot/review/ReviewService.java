package com.magicworld.tfg_angular_springboot.review;

import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.purchase.Purchase;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseRepository;
import com.magicworld.tfg_angular_springboot.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional(readOnly = true)
    public Page<ReviewDTO> findAllPaginated(Pageable pageable) {
        return reviewRepository.findAllByOrderByPublicationDateDesc(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<Long> getPurchasesAvailableForReview(Long userId) {
        List<Purchase> userPurchases = purchaseRepository.findByBuyerId(userId);
        List<Long> alreadyReviewed = reviewRepository.findReviewedPurchaseIdsByUserId(userId);

        return userPurchases.stream()
                .map(Purchase::getId)
                .filter(id -> !alreadyReviewed.contains(id))
                .toList();
    }

    @Transactional
    public ReviewDTO createReview(User user, ReviewRequest request) {
        Purchase purchase = purchaseRepository.findById(request.getPurchaseId())
                .orElseThrow(() -> new ResourceNotFoundException("error.purchase.not_found"));

        if (!purchase.getBuyer().getId().equals(user.getId())) {
            throw new InvalidOperationException("error.review.not.owner");
        }

        if (reviewRepository.existsByPurchaseId(request.getPurchaseId())) {
            throw new InvalidOperationException("error.review.already.exists");
        }

        Review review = Review.builder()
                .stars(request.getStars())
                .description(request.getDescription())
                .publicationDate(LocalDate.now())
                .visitDate(request.getVisitDate())
                .purchase(purchase)
                .build();

        Review saved = reviewRepository.save(review);
        return toDTO(saved);
    }

    private ReviewDTO toDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .stars(review.getStars())
                .publicationDate(review.getPublicationDate())
                .visitDate(review.getVisitDate())
                .description(review.getDescription())
                .username(review.getPurchase().getBuyer().getUsername())
                .purchaseId(review.getPurchase().getId())
                .build();
    }
}
