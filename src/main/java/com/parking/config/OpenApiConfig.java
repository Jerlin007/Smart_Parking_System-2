package com.parking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartParkingOpenAPI() {
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token obtained from /auth/login endpoint");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("Smart Parking Management System API")
                        .description("Enterprise Parking Management Platform built using Spring Boot, JWT Authentication, Parking Reservation, Vehicle Entry/Exit Tracking, Billing and Payment Processing.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Smart Parking Team")
                                .email("support@smartparking.com")
                                .url("https://smartparking.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .schemaRequirement("Bearer JWT", jwtScheme)
                .addSecurityItem(securityRequirement);
    }

    @Bean
    public WebMvcConfigurer swaggerResourceHandler() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/swagger-ui/**")
                        .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                        .resourceChain(false);
            }
        };
    }

}
