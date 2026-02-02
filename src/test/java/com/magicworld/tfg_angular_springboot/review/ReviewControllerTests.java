package com.magicworld.tfg_angular_springboot.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Valoraciones")
@Feature("Controlador de Valoraciones")
public class ReviewControllerTests {

    private static final String API_REVIEWS = "/api/v1/reviews";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    private ReviewDTO sampleReviewDTO() {
        return ReviewDTO.builder()
                .id(1L)
                .stars(4.5)
                .publicationDate(LocalDate.now())
                .visitDate(LocalDate.now().plusDays(1))
                .description("Excelente experiencia")
                .username("testuser")
                .purchaseId(1L)
                .build();
    }

    private User sampleUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setEmail("test@example.com");
        return user;
    }

    @Test
    @Story("Listar Valoraciones")
    @Description("Verifica que listar valoraciones retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar valoraciones retorna 200 OK")
    void testGetAllReviewsReturnsOk() throws Exception {
        PageImpl<ReviewDTO> page = new PageImpl<>(
            List.of(sampleReviewDTO()),
            PageRequest.of(0, 10),
            1
        );
        when(reviewService.findAllPaginated(any())).thenReturn(page);

        mockMvc.perform(get(API_REVIEWS)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Listar Valoraciones")
    @Description("Verifica que listar valoraciones retorna contenido paginado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar valoraciones retorna contenido paginado")
    void testGetAllReviewsReturnsPaginatedContent() throws Exception {
        PageImpl<ReviewDTO> page = new PageImpl<>(
            List.of(sampleReviewDTO()),
            PageRequest.of(0, 10),
            1
        );
        when(reviewService.findAllPaginated(any())).thenReturn(page);

        mockMvc.perform(get(API_REVIEWS)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].stars").value(4.5))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @Story("Listar Valoraciones")
    @Description("Verifica que listar valoraciones vacías retorna array vacío")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Listar valoraciones vacías retorna array vacío")
    void testGetAllReviewsEmptyReturnsEmptyArray() throws Exception {
        PageImpl<ReviewDTO> emptyPage = new PageImpl<>(
            List.of(),
            PageRequest.of(0, 10),
            0
        );
        when(reviewService.findAllPaginated(any())).thenReturn(emptyPage);

        mockMvc.perform(get(API_REVIEWS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Crear Valoración")
    @Description("Verifica que crear valoración retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear valoración retorna 201 Created")
    void testCreateReviewReturnsCreated() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(reviewService.createReview(any(User.class), any(ReviewRequest.class)))
                .thenReturn(sampleReviewDTO());

        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(1L)
                .visitDate(LocalDate.now())
                .stars(4.5)
                .description("Excelente experiencia")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Crear Valoración")
    @Description("Verifica que crear valoración retorna header Location")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear valoración retorna header Location")
    void testCreateReviewReturnsLocationHeader() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(reviewService.createReview(any(User.class), any(ReviewRequest.class)))
                .thenReturn(sampleReviewDTO());

        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(1L)
                .visitDate(LocalDate.now())
                .stars(4.5)
                .description("Excelente experiencia")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(header().exists("Location"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Crear Valoración")
    @Description("Verifica que crear valoración retorna body con datos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear valoración retorna body con datos")
    void testCreateReviewReturnsBody() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(reviewService.createReview(any(User.class), any(ReviewRequest.class)))
                .thenReturn(sampleReviewDTO());

        ReviewRequest request = ReviewRequest.builder()
                .purchaseId(1L)
                .visitDate(LocalDate.now())
                .stars(4.5)
                .description("Excelente experiencia")
                .build();

        mockMvc.perform(post(API_REVIEWS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stars").value(4.5))
                .andExpect(jsonPath("$.description").value("Excelente experiencia"));
    }

    @TestConfiguration
    static class ReviewControllerTestConfig {
        @Bean
        public ReviewService reviewService() {
            return Mockito.mock(ReviewService.class);
        }

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }

        @Bean
        public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
            return Mockito.mock(OAuth2AuthenticationSuccessHandler.class);
        }

        @Bean
        public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
            return Mockito.mock(OAuth2AuthenticationFailureHandler.class);
        }
    }
}
