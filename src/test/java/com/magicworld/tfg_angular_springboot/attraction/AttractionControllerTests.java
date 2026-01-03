package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.storage.ImageStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AttractionController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Epic("Gestión de Atracciones")
@Feature("API REST de Atracciones")
public class AttractionControllerTests {

    private static final String API_ATTRACTIONS = "/api/v1/attractions";
    private static final String API_ATTRACTIONS_ID = "/api/v1/attractions/";
    private static final String NEW_RIDE_NAME = "New Ride";
    private static final String NICE_RIDE_DESC = "Nice ride";
    private static final String UPDATED_NAME = "Updated";
    private static final String UPDATED_DESC = "up";
    private static final String PHOTO_URL = "https://example.com/new.jpg";
    private static final String ERROR_ATTRACTION_NOT_FOUND = "error.attraction.notfound";
    private static final int MIN_HEIGHT_90 = 90;
    private static final int MIN_HEIGHT_80 = 80;
    private static final int MIN_HEIGHT_100 = 100;
    private static final int MIN_HEIGHT_120 = 120;
    private static final int MIN_AGE_6 = 6;
    private static final int MIN_AGE_5 = 5;
    private static final int MIN_AGE_8 = 8;
    private static final int MIN_AGE_12 = 12;
    private static final int MIN_WEIGHT_20 = 20;
    private static final int MIN_WEIGHT_30 = 30;
    private static final int MIN_WEIGHT_40 = 40;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttractionService attractionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @BeforeEach
    void setUp() {
        Mockito.reset(attractionService);
    }

    private Attraction buildNewRideRequest() {
        return Attraction.builder()
                .name(NEW_RIDE_NAME)
                .intensity(Intensity.LOW)
                .minimumHeight(MIN_HEIGHT_90)
                .minimumAge(MIN_AGE_6)
                .minimumWeight(MIN_WEIGHT_20)
                .description(NICE_RIDE_DESC)
                .photoUrl(PHOTO_URL)
                .isActive(true)
                .build();
    }

    private Attraction buildSavedAttraction(Attraction request) {
        Attraction saved = Attraction.builder()
                .name(request.getName())
                .intensity(request.getIntensity())
                .minimumHeight(request.getMinimumHeight())
                .minimumAge(request.getMinimumAge())
                .minimumWeight(request.getMinimumWeight())
                .description(request.getDescription())
                .photoUrl(request.getPhotoUrl())
                .isActive(request.getIsActive())
                .build();
        saved.setId(1L);
        return saved;
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear atracción retorna 201 Created con header Location")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear atracción retorna 201 Created")
    public void testCreateAttractionReturnsCreated() throws Exception {
        Attraction request = buildNewRideRequest();
        Attraction saved = buildSavedAttraction(request);

        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(saved);

        var result = mockMvc.perform(post(API_ATTRACTIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", API_ATTRACTIONS_ID + "1"))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear atracción retorna ID y nombre")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear atracción retorna ID y nombre")
    public void testCreateAttractionReturnsId() throws Exception {
        Attraction request = buildNewRideRequest();
        Attraction saved = buildSavedAttraction(request);

        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(saved);

        var result = mockMvc.perform(post(API_ATTRACTIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(NEW_RIDE_NAME))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("id"));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que obtener atracciones retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener atracciones retorna 200 OK")
    public void testGetAllAttractionsReturnsOk() throws Exception {
        Attraction one = Attraction.builder()
                .name("A")
                .intensity(Intensity.LOW)
                .minimumHeight(MIN_HEIGHT_80)
                .minimumAge(MIN_AGE_5)
                .minimumWeight(MIN_WEIGHT_20)
                .description("d")
                .photoUrl("u")
                .isActive(true)
                .build();
        one.setId(1L);

        when(attractionService.getAllAttractions(any(), any(), any())).thenReturn(List.of(one));

        var result = mockMvc.perform(get(API_ATTRACTIONS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Atracción por ID")
    @Description("Verifica que obtener atracción por ID retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener atracción por ID retorna 200 OK")
    public void testGetAttractionByIdFoundReturnsOk() throws Exception {
        Attraction one = Attraction.builder().name("B").intensity(Intensity.MEDIUM).minimumHeight(MIN_HEIGHT_100).minimumAge(MIN_AGE_8).minimumWeight(MIN_WEIGHT_30).description("desc").photoUrl("u").isActive(true).build();
        one.setId(2L);
        when(attractionService.getAttractionById(2L)).thenReturn(one);

        var result = mockMvc.perform(get(API_ATTRACTIONS_ID + "2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Obtener Atracción por ID")
    @Description("Verifica que obtener atracción inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracción inexistente retorna 404")
    public void testGetAttractionByIdNotFound() throws Exception {
        when(attractionService.getAttractionById(999L)).thenThrow(new ResourceNotFoundException(ERROR_ATTRACTION_NOT_FOUND));

        var result = mockMvc.perform(get(API_ATTRACTIONS_ID + "999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar atracción retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar atracción retorna 200 OK")
    public void testUpdateAttractionReturnsOk() throws Exception {
        Attraction update = Attraction.builder().name(UPDATED_NAME).intensity(Intensity.HIGH).minimumHeight(MIN_HEIGHT_120).minimumAge(MIN_AGE_12).minimumWeight(MIN_WEIGHT_40).description(UPDATED_DESC).photoUrl("u").isActive(false).build();
        Attraction returned = Attraction.builder().name(UPDATED_NAME).intensity(Intensity.HIGH).minimumHeight(MIN_HEIGHT_120).minimumAge(MIN_AGE_12).minimumWeight(MIN_WEIGHT_40).description(UPDATED_DESC).photoUrl("u").isActive(false).build();
        returned.setId(3L);

        when(attractionService.updateAttraction(eq(3L), any(Attraction.class))).thenReturn(returned);

        var result = mockMvc.perform(put(API_ATTRACTIONS_ID + "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Eliminar Atracción")
    @Description("Verifica que eliminar atracción retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar atracción retorna 204 No Content")
    public void testDeleteAttraction() throws Exception {
        doNothing().when(attractionService).deleteAttraction(4L);

        mockMvc.perform(delete(API_ATTRACTIONS_ID + "4").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(attractionService).deleteAttraction(4L);
    }
}
