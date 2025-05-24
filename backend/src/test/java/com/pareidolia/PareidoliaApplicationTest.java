package com.pareidolia;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PareidoliaApplicationTest {

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads successfully
	}

	@Test
	void testTimeZoneIsUTC() {
		// Call the main method to set the timezone
		PareidoliaApplication.main(new String[]{});

		// Verify that the default timezone is set to UTC
		assertEquals("UTC", TimeZone.getDefault().getID());
	}

	@Test
	void testSwaggerConfiguration() {
		// Verify that the class has the required Swagger annotations
		OpenAPIDefinition apiDef = PareidoliaApplication.class.getAnnotation(OpenAPIDefinition.class);
		assertNotNull(apiDef);
		assertEquals("Pareidolia API", apiDef.info().title());
		assertEquals("1.0.0", apiDef.info().version());

		SecuritySchemes securitySchemes = PareidoliaApplication.class.getAnnotation(SecuritySchemes.class);
		assertNotNull(securitySchemes);
		assertEquals(3, securitySchemes.value().length);

		// Verify each security scheme
		SecurityScheme[] schemes = securitySchemes.value();
		assertTrue(containsSecurityScheme(schemes, "JWT_Consumer"));
		assertTrue(containsSecurityScheme(schemes, "JWT_Promoter"));
		assertTrue(containsSecurityScheme(schemes, "JWT_Admin"));
	}

	private boolean containsSecurityScheme(SecurityScheme[] schemes, String name) {
		for (SecurityScheme scheme : schemes) {
			if (scheme.name().equals(name)) {
				assertEquals(SecuritySchemeType.HTTP, scheme.type());
				assertEquals("bearer", scheme.scheme());
				assertEquals("Bearer [token]", scheme.bearerFormat());
				return true;
			}
		}
		return false;
	}
} 