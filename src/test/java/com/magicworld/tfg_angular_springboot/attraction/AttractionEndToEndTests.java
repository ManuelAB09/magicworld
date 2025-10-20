package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
public class AttractionEndToEndTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttractionService attractionService;


    @Test
    public void testCreateAttraction() throws Exception {
        Attraction request = Attraction.builder()
                .name("New Ride")
                .intensity(Intensity.LOW)
                .minimumHeight(90)
                .minimumAge(6)
                .minimumWeight(20)
                .description("Nice ride")
                .photoUrl("https://example.com/new.jpg")
                .isActive(true)
                .build();

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

        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/attractions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/attractions/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Ride"));
    }

    @Test
    public void testGetAllAttractions() throws Exception {
        Attraction one = Attraction.builder().name("A").intensity(Intensity.LOW).minimumHeight(80).minimumAge(5).minimumWeight(20).description("d").photoUrl("u").isActive(true).build();
        one.setId(1L);
        when(attractionService.getAllAttractions()).thenReturn(List.of(one));

        mockMvc.perform(get("/api/v1/attractions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testGetAttractionById_found() throws Exception {
        Attraction one = Attraction.builder().name("B").intensity(Intensity.MEDIUM).minimumHeight(100).minimumAge(8).minimumWeight(30).description("desc").photoUrl("u").isActive(true).build();
        one.setId(2L);
        when(attractionService.getAttractionById(2L)).thenReturn(one);

        mockMvc.perform(get("/api/v1/attractions/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("B"));
    }

    @Test
    public void testGetAttractionById_notFound() throws Exception {
        when(attractionService.getAttractionById(999L)).thenThrow(new ResourceNotFoundException("error.attraction.notfound"));

        mockMvc.perform(get("/api/v1/attractions/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateAttraction() throws Exception {
        Attraction update = Attraction.builder().name("Updated").intensity(Intensity.HIGH).minimumHeight(120).minimumAge(12).minimumWeight(40).description("up").photoUrl("u").isActive(false).build();
        Attraction returned = Attraction.builder().name("Updated").intensity(Intensity.HIGH).minimumHeight(120).minimumAge(12).minimumWeight(40).description("up").photoUrl("u").isActive(false).build();
        returned.setId(3L);

        when(attractionService.updateAttraction(eq(3L), any(Attraction.class))).thenReturn(returned);

        mockMvc.perform(put("/api/v1/attractions/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    public void testDeleteAttraction() throws Exception {
        doNothing().when(attractionService).deleteAttraction(4L);

        mockMvc.perform(delete("/api/v1/attractions/4").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(attractionService).deleteAttraction(4L);
    }

    @TestConfiguration
    static class AttractionEndToEndTestConfig {
        @Bean
        public AttractionService attractionService() {
            return Mockito.mock(AttractionService.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }
    }
}
