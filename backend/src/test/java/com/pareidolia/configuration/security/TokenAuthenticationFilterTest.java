package com.pareidolia.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

	@Mock
	private RequestMatcher requestMatcher;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	private TokenAuthenticationFilter filter;

	@BeforeEach
	void setUp() {
		filter = new TokenAuthenticationFilter(requestMatcher);
		filter.setAuthenticationManager(authenticationManager);
	}

	@Test
	void testAttemptAuthenticationWithValidToken() {
		String token = "valid-token";
		when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer " + token);

		Authentication expectedAuth = new UsernamePasswordAuthenticationToken(token, token);
		when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(expectedAuth);

		Authentication result = filter.attemptAuthentication(request, response);

		assertNotNull(result);
		assertEquals(expectedAuth, result);
		verify(authenticationManager).authenticate(any(Authentication.class));
	}

	@Test
	void testAttemptAuthenticationWithoutToken() {
		when(request.getHeader(AUTHORIZATION)).thenReturn(null);

		assertThrows(BadCredentialsException.class, () ->
			filter.attemptAuthentication(request, response)
		);

		verify(authenticationManager, never()).authenticate(any(Authentication.class));
	}

	@Test
	void testAttemptAuthenticationWithEmptyToken() {
		when(request.getHeader(AUTHORIZATION)).thenReturn(null);

		assertThrows(BadCredentialsException.class, () ->
			filter.attemptAuthentication(request, response)
		);

		verify(authenticationManager, never()).authenticate(any(Authentication.class));
	}

	@Test
	void testSuccessfulAuthentication() throws ServletException, IOException {
		Authentication auth = new UsernamePasswordAuthenticationToken("user", "pass");

		filter.successfulAuthentication(request, response, filterChain, auth);

		verify(filterChain).doFilter(request, response);
	}

	@Test
	void testAttemptAuthenticationWithInvalidToken() {
		String token = "invalid-token";
		when(request.getHeader(AUTHORIZATION)).thenReturn("Bearer " + token);
		when(authenticationManager.authenticate(any(Authentication.class)))
			.thenThrow(new BadCredentialsException("Invalid token"));

		assertThrows(BadCredentialsException.class, () ->
			filter.attemptAuthentication(request, response)
		);

		verify(authenticationManager).authenticate(any(Authentication.class));
	}
} 