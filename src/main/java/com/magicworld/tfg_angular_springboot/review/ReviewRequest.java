package com.magicworld.tfg_angular_springboot.review;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "validation.review.purchase.required")
    private Long purchaseId;

    @NotNull(message = "validation.review.visitDate.required")
    private LocalDate visitDate;

    @NotNull(message = "validation.review.stars.required")
    @DecimalMin(value = "1.0", message = "validation.review.stars.min")
    @DecimalMax(value = "5.0", message = "validation.review.stars.max")
    private Double stars;

    @NotBlank(message = "validation.review.description.required")
    @Size(max = 255, message = "validation.review.description.size")
    private String description;
}
