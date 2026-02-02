package com.magicworld.tfg_angular_springboot.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByOrderByPublicationDateDesc(Pageable pageable);

    boolean existsByPurchaseId(Long purchaseId);

    @Query("SELECT r.purchase.id FROM Review r WHERE r.purchase.buyer.id = :userId")
    List<Long> findReviewedPurchaseIdsByUserId(@Param("userId") Long userId);
}
