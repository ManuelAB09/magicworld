package com.magicworld.tfg_angular_springboot.review;

import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.purchase.Purchase;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseRepository;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Valoraciones")
@Feature("Servicio de Valoraciones")
public class ReviewServiceTests {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private User testUser2;
    private Purchase testPurchase;
    private Purchase testPurchase2;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        purchaseRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        testUser = userRepository.save(User.builder()
                .username("testuser")
                .firstname("John")
                .lastname("Doe")
                .email("test@example.com")
                .password("Password123!")
                .userRole(Role.USER)
                .build());

        testUser2 = userRepository.save(User.builder()
                .username("testuser2")
                .firstname("Jane")
                .lastname("Smith")
                .email("test2@example.com")
                .password("Password123!")
                .userRole(Role.USER)
                .build());

        testPurchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(testUser)
                .build());

        testPurchase2 = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now().minusDays(5))
                .buyer(testUser)
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Story("Crear Valoración")
    @Description("Verifica que se puede crear una valoración correctamente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear valoración exitosamente")
    void testCreateReviewSuccess() {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(4.5)
                .description("Excelente experiencia en el parque")
                .build();

        ReviewDTO result = reviewService.createReview(testUser, request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(4.5, result.getStars());
        assertEquals("Excelente experiencia en el parque", result.getDescription());
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    @Story("Crear Valoración")
    @Description("Verifica que no se puede crear valoración para compra ajena")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Error al crear valoración para compra ajena")
    void testCreateReviewNotOwnerThrowsException() {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(4.0)
                .description("Descripción")
                .build();

        assertThrows(InvalidOperationException.class, () ->
            reviewService.createReview(testUser2, request)
        );
    }

    @Test
    @Story("Crear Valoración")
    @Description("Verifica que no se puede crear valoración duplicada")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Error al crear valoración duplicada para misma compra")
    void testCreateReviewAlreadyExistsThrowsException() {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(4.0)
                .description("Primera valoración")
                .build();

        reviewService.createReview(testUser, request);

        ReviewRequest duplicateRequest = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(5.0)
                .description("Segunda valoración")
                .build();

        assertThrows(InvalidOperationException.class, () ->
            reviewService.createReview(testUser, duplicateRequest)
        );
    }

    @Test
    @Story("Crear Valoración")
    @Description("Verifica error cuando la compra no existe")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Error al crear valoración para compra inexistente")
    void testCreateReviewPurchaseNotFoundThrowsException() {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(99999L)
                .visitDate(LocalDate.now())
                .stars(4.0)
                .description("Descripción")
                .build();

        assertThrows(ResourceNotFoundException.class, () ->
            reviewService.createReview(testUser, request)
        );
    }

    @Test
    @Story("Listar Valoraciones")
    @Description("Verifica que se pueden listar valoraciones paginadas")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar valoraciones paginadas")
    void testFindAllPaginated() {
        ReviewRequest request1 = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(4.0)
                .description("Primera valoración")
                .build();
        reviewService.createReview(testUser, request1);

        ReviewRequest request2 = ReviewRequest.builder()
                .purchaseId(testPurchase2.getId())
                .visitDate(LocalDate.now().minusDays(5))
                .stars(5.0)
                .description("Segunda valoración")
                .build();
        reviewService.createReview(testUser, request2);

        Page<ReviewDTO> page = reviewService.findAllPaginated(PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

    @Test
    @Story("Listar Valoraciones")
    @Description("Verifica paginación con página vacía")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Paginación retorna página vacía cuando no hay datos")
    void testFindAllPaginatedEmpty() {
        Page<ReviewDTO> page = reviewService.findAllPaginated(PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    @Story("Compras Disponibles")
    @Description("Verifica que se obtienen las compras disponibles para valorar")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener compras disponibles para valorar")
    void testGetPurchasesAvailableForReview() {
        List<Long> available = reviewService.getPurchasesAvailableForReview(testUser.getId());

        assertNotNull(available);
        assertEquals(2, available.size());
        assertTrue(available.contains(testPurchase.getId()));
        assertTrue(available.contains(testPurchase2.getId()));
    }

    @Test
    @Story("Compras Disponibles")
    @Description("Verifica que compra ya valorada no aparece disponible")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Compra valorada no aparece en disponibles")
    void testGetPurchasesAvailableExcludesReviewed() {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(4.0)
                .description("Valoración")
                .build();
        reviewService.createReview(testUser, request);

        List<Long> available = reviewService.getPurchasesAvailableForReview(testUser.getId());

        assertEquals(1, available.size());
        assertFalse(available.contains(testPurchase.getId()));
        assertTrue(available.contains(testPurchase2.getId()));
    }

    @Test
    @Story("Crear Valoración")
    @Description("Verifica que la fecha de visita se guarda correctamente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Fecha de visita se guarda correctamente")
    void testCreateReviewVisitDateSaved() {
        LocalDate visitDate = LocalDate.now().plusDays(7);
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(visitDate)
                .stars(4.5)
                .description("Visita futura")
                .build();

        ReviewDTO result = reviewService.createReview(testUser, request);

        assertEquals(visitDate, result.getVisitDate());
    }

    @Test
    @Story("Crear Valoración")
    @Description("Verifica que las estrellas están en rango válido")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Estrellas mínimas (1) se guardan correctamente")
    void testCreateReviewMinStars() {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(1.0)
                .description("Experiencia mínima")
                .build();

        ReviewDTO result = reviewService.createReview(testUser, request);

        assertEquals(1.0, result.getStars());
    }

    @Test
    @Story("Crear Valoración")
    @Description("Verifica que las estrellas máximas se guardan")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Estrellas máximas (5) se guardan correctamente")
    void testCreateReviewMaxStars() {
        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(testPurchase.getId())
                .visitDate(LocalDate.now())
                .stars(5.0)
                .description("Experiencia excelente")
                .build();

        ReviewDTO result = reviewService.createReview(testUser, request);

        assertEquals(5.0, result.getStars());
    }
}
