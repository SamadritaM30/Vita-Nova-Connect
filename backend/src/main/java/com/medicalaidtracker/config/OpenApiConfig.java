package com.medicalaidtracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI medicalAidOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Medical Aid Tracker API")
                        .version("v1")
                        .description("REST API for patient care discovery and hospital representative operations.")
                        .contact(new Contact().name("Medical Aid Tracker Team")))
                .components(new Components().addSecuritySchemes(
                        "sessionCookie",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Created after Google OAuth 2.0 login.")
                ));
    }
}
