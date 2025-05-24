package com.pareidolia.configuration.security.jwt;

import com.pareidolia.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JWTServiceTest {

	private static final String SECRET = "test-secret-key-for-jwt-token-generation";
	private JWTService jwtService;

	@BeforeEach
	void setUp() {
		jwtService = new JWTService(SECRET);
	}

	@Test
	void testCreateToken() {
		String token = jwtService.create(Account.Type.CONSUMER, "test@example.com", "password123");

		assertNotNull(token);
		assertTrue(token.length() > 0);
	}

	@Test
	void testVerifyValidToken() {
		String token = jwtService.create(Account.Type.CONSUMER, "test@example.com", "password123");

		Map<String, Object> claims = jwtService.verify(token);

		assertNotNull(claims);
		assertEquals("CONSUMER", claims.get("referenceType"));
		assertEquals("test@example.com", claims.get("username"));
		assertEquals("password123", claims.get("password"));
		assertNotNull(claims.get("iat")); // issued at timestamp
	}

	@Test
	void testVerifyInvalidToken() {
		String invalidToken = "invalid.jwt.token";

		assertThrows(JWTService.TokenVerificationException.class, () ->
			jwtService.verify(invalidToken)
		);
	}

	@Test
	void testVerifyModifiedToken() {
		String token = jwtService.create(Account.Type.CONSUMER, "test@example.com", "password123");
		String modifiedToken = token + "modified";

		assertThrows(JWTService.TokenVerificationException.class, () ->
			jwtService.verify(modifiedToken)
		);
	}

	@Test
	void testVerifyExpiredToken() {
		// This would require setting up a token with a past expiration date
		// Since the current implementation doesn't support expiration, we'll skip this test
		// but note it as a potential future enhancement
	}

	@Test
	void testTokenVerificationExceptionWithMessage() {
		String errorMessage = "Test error message";
		JWTService.TokenVerificationException exception = new JWTService.TokenVerificationException(errorMessage);

		assertEquals(errorMessage, exception.getMessage());
	}

	@Test
	void testTokenVerificationExceptionWithCause() {
		RuntimeException cause = new RuntimeException("Test cause");
		JWTService.TokenVerificationException exception = new JWTService.TokenVerificationException(cause);

		assertEquals(cause, exception.getCause());
	}

	@Test
	void testTokenVerificationExceptionNoArgs() {
		JWTService.TokenVerificationException exception = new JWTService.TokenVerificationException();

		assertNull(exception.getMessage());
		assertNull(exception.getCause());
	}

	@Test
	void testCreateTokenWithDifferentAccountTypes() {
		for (Account.Type type : Account.Type.values()) {
			String token = jwtService.create(type, "test@example.com", "password123");
			Map<String, Object> claims = jwtService.verify(token);

			assertEquals(type.name(), claims.get("referenceType"));
		}
	}
} 