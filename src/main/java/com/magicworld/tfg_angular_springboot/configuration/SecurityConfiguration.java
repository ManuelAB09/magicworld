package com.magicworld.tfg_angular_springboot.configuration;

import com.magicworld.tfg_angular_springboot.auth.RateLimitFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.AuthEntryPointJwt;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
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
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    private static final String ADMIN_ROLE = "ADMIN";
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/v1/auth/login", "/api/v1/auth/register","/api/v1/auth/reset-password","/api/v1/auth/forgot-password","/oauth2/**")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/static/**", "/images/**", "/v3/api-docs/**", "/swagger-resources/**", "/api/v1/auth/login","/api/v1/auth/register",
                                         "/api/v1/auth/reset-password","/api/v1/auth/forgot-password", "/swagger-ui.html", "/swagger-ui/**", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/attractions", "/api/v1/attractions/**","/api/v1/ticket-types","/api/v1/ticket-types/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/discounts", "/api/v1/discounts/**").authenticated()
                        .requestMatchers("/api/v1/chatbot/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, "/api/v1/attractions/**","/api/v1/ticket-types/**","/api/v1/discounts", "/api/v1/discounts/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/attractions/**","/api/v1/ticket-types/**","/api/v1/discounts", "/api/v1/discounts/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/attractions/**","/api/v1/ticket-types/**","/api/v1/discounts", "/api/v1/discounts/**").hasRole(ADMIN_ROLE)
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
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
