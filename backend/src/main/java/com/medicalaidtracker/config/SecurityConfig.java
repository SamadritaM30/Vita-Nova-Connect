package com.medicalaidtracker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalaidtracker.common.api.ApiResponse;
import com.medicalaidtracker.security.OAuthGrantedAuthoritiesMapper;
import com.medicalaidtracker.security.RestAccessDeniedHandler;
import com.medicalaidtracker.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppProperties properties;
    private final OAuthGrantedAuthoritiesMapper authoritiesMapper;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final ObjectMapper objectMapper;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var csrfRepository = new HttpSessionCsrfTokenRepository();
        var csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(csrfHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/", "/error", "/oauth2/**", "/login/**",
                                "/actuator/health", "/actuator/health/**",
                                "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
                        ).permitAll()
                        .requestMatchers("/actuator/**").denyAll()
                        .requestMatchers("/api/v1/representative/**").hasRole("REPRESENTATIVE")
                        .requestMatchers("/api/v1/appointments/**", "/api/v1/ambulance-requests/**")
                        .hasRole("PATIENT")
                        .requestMatchers("/api/v1/**").hasAnyRole("PATIENT", "REPRESENTATIVE")
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(info -> info.userAuthoritiesMapper(authoritiesMapper))
                        .successHandler((request, response, authentication) ->
                                response.sendRedirect(frontendUrl(properties.oauth().successPath())))
                        .failureHandler((request, response, exception) ->
                                response.sendRedirect(frontendUrl(properties.oauth().failurePath()))))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            objectMapper.writeValue(response.getOutputStream(),
                                    ApiResponse.ok(Map.of("message", "Signed out successfully.")));
                        }))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.corsAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Content-Type", "Accept", "Authorization", "X-CSRF-TOKEN", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("X-CSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String frontendUrl(String path) {
        String base = properties.frontendUrl().endsWith("/")
                ? properties.frontendUrl().substring(0, properties.frontendUrl().length() - 1)
                : properties.frontendUrl();
        return base + (path.startsWith("/") ? path : "/" + path);
    }
}
