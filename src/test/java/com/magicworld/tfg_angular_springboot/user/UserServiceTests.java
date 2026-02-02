package com.magicworld.tfg_angular_springboot.user;

import com.magicworld.tfg_angular_springboot.exceptions.EmailAlreadyExistsException;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
import com.magicworld.tfg_angular_springboot.purchase.Purchase;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseRepository;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.review.Review;
import com.magicworld.tfg_angular_springboot.review.ReviewRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Usuarios")
@Feature("Servicio de Usuarios")
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseLineRepository purchaseLineRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        reviewRepository.deleteAll();
        purchaseLineRepository.deleteAll();
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
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Story("Establecer Usuario Actual")
    @Description("Verifica que setCurrentUser establece correctamente el contexto de seguridad")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("setCurrentUser establece el contexto de seguridad correctamente")
    void testSetCurrentUser() {
        User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getUsername()).thenReturn("testUser");
        Mockito.when(mockUser.getPassword()).thenReturn("testPassword");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(mockUser).getAuthorities();

        userService.setCurrentUser(mockUser);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testUser", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que se puede actualizar el perfil correctamente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar perfil exitosamente")
    void testUpdateProfileSuccess() {
        User user = userRepository.findByUsername("testuser").orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Updated")
                .lastname("Name")
                .email("updated@example.com")
                .build();

        User updated = userService.updateProfile(user, request);

        assertEquals("Updated", updated.getFirstname());
        assertEquals("Name", updated.getLastname());
        assertEquals("updated@example.com", updated.getEmail());
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que se puede cambiar la contraseña")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Cambiar contraseña exitosamente")
    void testUpdateProfileWithPassword() {
        User user = userRepository.findByUsername("testuser").orElseThrow();
        String oldPassword = user.getPassword();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("test@example.com")
                .password("NewPassword123!")
                .build();

        User updated = userService.updateProfile(user, request);

        assertNotEquals(oldPassword, updated.getPassword());
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que contraseña inválida lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contraseña inválida lanza excepción")
    void testUpdateProfileInvalidPasswordThrowsException() {
        User user = userRepository.findByUsername("testuser").orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("test@example.com")
                .password("weak")
                .build();

        assertThrows(InvalidOperationException.class, () ->
            userService.updateProfile(user, request)
        );
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que email duplicado lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Email duplicado lanza excepción")
    void testUpdateProfileDuplicateEmailThrowsException() {
        userRepository.save(User.builder()
                .username("otheruser")
                .firstname("Jane")
                .lastname("Smith")
                .email("other@example.com")
                .password("Password123!")
                .userRole(Role.USER)
                .build());

        User user = userRepository.findByUsername("testuser").orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("other@example.com")
                .build();

        assertThrows(EmailAlreadyExistsException.class, () ->
            userService.updateProfile(user, request)
        );
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que mismo email no causa error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Mismo email no causa error")
    void testUpdateProfileSameEmailNoError() {
        User user = userRepository.findByUsername("testuser").orElseThrow();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Updated")
                .lastname("Name")
                .email("test@example.com")
                .build();

        User updated = userService.updateProfile(user, request);

        assertEquals("test@example.com", updated.getEmail());
        assertEquals("Updated", updated.getFirstname());
    }

    @Test
    @Story("Eliminar Usuario")
    @Description("Verifica que se puede eliminar usuario sin datos relacionados")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar usuario sin datos relacionados")
    void testDeleteUserWithoutRelatedData() {
        User user = userRepository.findByUsername("testuser").orElseThrow();
        Long userId = user.getId();

        userService.deleteUserWithRelatedData(user);

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    @Story("Eliminar Usuario")
    @Description("Verifica que se eliminan compras al eliminar usuario")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar usuario con compras elimina compras")
    void testDeleteUserWithPurchasesDeletesPurchases() {
        User user = userRepository.findByUsername("testuser").orElseThrow();
        Purchase purchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(user)
                .build());
        entityManager.flush();

        userService.deleteUserWithRelatedData(user);

        assertFalse(purchaseRepository.existsById(purchase.getId()));
    }

    @Test
    @Story("Eliminar Usuario")
    @Description("Verifica que se eliminan reviews al eliminar usuario")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar usuario con reviews elimina reviews")
    void testDeleteUserWithReviewsDeletesReviews() {
        User user = userRepository.findByUsername("testuser").orElseThrow();
        Purchase purchase = purchaseRepository.save(Purchase.builder()
                .purchaseDate(LocalDate.now())
                .buyer(user)
                .build());
        Review review = reviewRepository.save(Review.builder()
                .stars(4.5)
                .publicationDate(LocalDate.now())
                .visitDate(LocalDate.now())
                .description("Test review")
                .purchase(purchase)
                .build());
        entityManager.flush();

        userService.deleteUserWithRelatedData(user);

        assertFalse(reviewRepository.existsById(review.getId()));
        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que password vacío no cambia contraseña")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Password vacío no cambia contraseña")
    void testUpdateProfileEmptyPasswordNoChange() {
        User user = userRepository.findByUsername("testuser").orElseThrow();
        String oldPassword = user.getPassword();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Updated")
                .lastname("Name")
                .email("test@example.com")
                .password("")
                .build();

        User updated = userService.updateProfile(user, request);

        assertEquals(oldPassword, updated.getPassword());
    }

    @Test
    @Story("Actualizar Perfil")
    @Description("Verifica que password null no cambia contraseña")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Password null no cambia contraseña")
    void testUpdateProfileNullPasswordNoChange() {
        User user = userRepository.findByUsername("testuser").orElseThrow();
        String oldPassword = user.getPassword();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Updated")
                .lastname("Name")
                .email("test@example.com")
                .password(null)
                .build();

        User updated = userService.updateProfile(user, request);

        assertEquals(oldPassword, updated.getPassword());
    }
}
