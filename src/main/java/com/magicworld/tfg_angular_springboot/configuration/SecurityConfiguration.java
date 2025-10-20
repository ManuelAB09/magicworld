package com.magicworld.tfg_angular_springboot.configuration;

import com.magicworld.tfg_angular_springboot.auth.RateLimitFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.AuthEntryPointJwt;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthEntryPointJwt unauthorizedHandler;

    private static final String ADMIN_ROLE = "ADMIN";
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/v1/auth/login", "/api/v1/auth/register","/api/v1/auth/reset-password","/api/v1/auth/forgot-password")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/static/**", "/v3/api-docs/**", "/swagger-resources/**", "/api/v1/auth/login","/api/v1/auth/register",
                                         "/api/v1/auth/reset-password","/api/v1/auth/forgot-password", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/attractions", "/api/v1/attractions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/attractions/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/attractions/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/attractions/**").hasRole(ADMIN_ROLE)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter rateLimitFilter) {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(rateLimitFilter);
        registrationBean.addUrlPatterns("/api/v1/auth/login");
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
