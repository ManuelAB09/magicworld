package com.magicworld.tfg_angular_springboot.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.exceptions.EmailAlreadyExistsException;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Usuarios")
@Feature("Controlador de Usuarios")
public class UserControllerTests {

    private static final String API_USERS_PROFILE = "/api/v1/users/profile";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User sampleUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("test@example.com");
        user.setUserRole(Role.USER);
        return user;
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Actualizar Perfil")
    @Description("Verifica que actualizar perfil retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar perfil retorna 200 OK")
    void testUpdateProfileReturnsOk() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userService.updateProfile(any(User.class), any(UpdateProfileRequest.class))).thenReturn(user);

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Updated")
                .lastname("Name")
                .email("updated@example.com")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Actualizar Perfil")
    @Description("Verifica que actualizar perfil retorna datos actualizados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar perfil retorna datos actualizados")
    void testUpdateProfileReturnsUpdatedData() throws Exception {
        User user = sampleUser();
        user.setFirstname("Updated");
        user.setLastname("Name");
        user.setEmail("updated@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser()));
        when(userService.updateProfile(any(User.class), any(UpdateProfileRequest.class))).thenReturn(user);

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Updated")
                .lastname("Name")
                .email("updated@example.com")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.firstname").value("Updated"))
                .andExpect(jsonPath("$.lastname").value("Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Actualizar Perfil")
    @Description("Verifica que email duplicado retorna 409 Conflict")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Email duplicado retorna 409 Conflict")
    void testUpdateProfileDuplicateEmailReturnsConflict() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userService.updateProfile(any(User.class), any(UpdateProfileRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("other@example.com"));

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("other@example.com")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Eliminar Perfil")
    @Description("Verifica que eliminar perfil retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar perfil retorna 204 No Content")
    void testDeleteProfileReturnsNoContent() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doNothing().when(userService).deleteUserWithRelatedData(any(User.class));

        mockMvc.perform(delete(API_USERS_PROFILE))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Eliminar Perfil")
    @Description("Verifica que eliminar perfil llama al servicio")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Eliminar perfil llama al servicio correctamente")
    void testDeleteProfileCallsService() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doNothing().when(userService).deleteUserWithRelatedData(any(User.class));

        mockMvc.perform(delete(API_USERS_PROFILE));

        verify(userService, atLeastOnce()).deleteUserWithRelatedData(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Actualizar Perfil")
    @Description("Verifica validación de firstname vacío")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Firstname vacío retorna 400 Bad Request")
    void testUpdateProfileEmptyFirstnameReturnsBadRequest() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("")
                .lastname("User")
                .email("test@example.com")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Actualizar Perfil")
    @Description("Verifica validación de email inválido")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Email inválido retorna 400 Bad Request")
    void testUpdateProfileInvalidEmailReturnsBadRequest() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Test")
                .lastname("User")
                .email("invalid-email")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Actualizar Perfil")
    @Description("Verifica que se puede actualizar con contraseña")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar perfil con contraseña funciona")
    void testUpdateProfileWithPasswordWorks() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userService.updateProfile(any(User.class), any(UpdateProfileRequest.class))).thenReturn(user);

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstname("Test")
                .lastname("User")
                .email("test@example.com")
                .password("NewPassword123!")
                .build();

        mockMvc.perform(put(API_USERS_PROFILE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class UserControllerTestConfig {
        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
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
