package com.magicworld.tfg_angular_springboot.review;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Double stars;
    private LocalDate publicationDate;
    private LocalDate visitDate;
    private String description;
    private String username;
    private Long purchaseId;
}
