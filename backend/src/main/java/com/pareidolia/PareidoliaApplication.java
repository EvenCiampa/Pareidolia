package com.pareidolia;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@OpenAPIDefinition(
	info = @Info(
		title = "Pareidolia API",
		version = "1.0.0"
	)
)
@SecuritySchemes({
	@SecurityScheme(
		name = "JWT_Consumer",
		description = "JWT authentication for Consumer with bearer token",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "Bearer [token]"),
	@SecurityScheme(
		name = "JWT_Promoter",
		description = "JWT authentication for Promoter with bearer token",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "Bearer [token]"),
	@SecurityScheme(
		name = "JWT_Admin",
		description = "JWT authentication for Admin with bearer token",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "Bearer [token]")
})
public class PareidoliaApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(PareidoliaApplication.class, args);
	}

}
