package com.pareidolia.configuration.error;

import com.pareidolia.configuration.security.jwt.JWTService;
import org.apache.catalina.connector.ClientAbortException;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.context.request.WebRequest;

import java.security.InvalidParameterException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ErrorHandlerTest {

	private ErrorHandler errorHandler;
	private WebRequest webRequest;

	@BeforeEach
	void setUp() {
		errorHandler = new ErrorHandler();
		webRequest = mock(WebRequest.class);
		SecurityContextHolder.clearContext();
	}

	@Test
	void testClientAbortException() {
		ClientAbortException ex = new ClientAbortException("Client aborted");
		ResponseEntity<Object> response = errorHandler.exception(ex, webRequest);
		assertNull(response);
	}

	@Test
	void testAssertionFailure() {
		AssertionFailure ex = new AssertionFailure("Bad request");
		ResponseEntity<Object> response = errorHandler.exception(ex, webRequest);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		ErrorResponse errorResponse = (ErrorResponse) response.getBody();
		assertNotNull(errorResponse);
		assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status);
	}

	@Test
	void testIllegalArgumentException() {
		IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
		ResponseEntity<Object> response = errorHandler.exception(ex, webRequest);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		ErrorResponse errorResponse = (ErrorResponse) response.getBody();
		assertNotNull(errorResponse);
		assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status);
	}

	@Test
	void testJWTTokenVerificationException() {
		JWTService.TokenVerificationException ex = new JWTService.TokenVerificationException("Invalid token");
		ResponseEntity<Object> response = errorHandler.exception(ex, webRequest);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		ErrorResponse errorResponse = (ErrorResponse) response.getBody();
		assertNotNull(errorResponse);
		assertEquals(HttpStatus.FORBIDDEN.value(), errorResponse.status);
	}

	@Test
	void testSpringException() {
		HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException("Unsupported media type");
		ResponseEntity<Object> response = errorHandler.exception(ex, webRequest);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		ErrorResponse errorResponse = (ErrorResponse) response.getBody();
		assertNotNull(errorResponse);
		assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status);
	}

	@Test
	void testInternalServerError() {
		Exception ex = new Exception("Unexpected error");
		ResponseEntity<Object> response = errorHandler.exception(ex, webRequest);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		ErrorResponse errorResponse = (ErrorResponse) response.getBody();
		assertNotNull(errorResponse);
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.status);
		verify(webRequest).setAttribute(anyString(), any(), anyInt());
	}

	@Test
	void testWithAuthenticatedUser() {
		User user = new User("testuser", "password",
			Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
		TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
		SecurityContextHolder.getContext().setAuthentication(auth);

		InvalidParameterException ex = new InvalidParameterException("Invalid parameter");
		ResponseEntity<Object> response = errorHandler.exception(ex, webRequest);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		ErrorResponse errorResponse = (ErrorResponse) response.getBody();
		assertNotNull(errorResponse);
		assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status);
	}

	@Test
	void testBadCredentialsException() {
		BadCredentialsException ex = new BadCredentialsException("Invalid credentials");
		ResponseEntity<Object> response = errorHandler.exception(ex, webRequest);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		ErrorResponse errorResponse = (ErrorResponse) response.getBody();
		assertNotNull(errorResponse);
		assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status);
	}
} 