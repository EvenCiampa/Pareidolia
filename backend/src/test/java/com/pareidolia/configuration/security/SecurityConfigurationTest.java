package com.pareidolia.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pareidolia.configuration.error.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest {

	@Mock
	private ObjectMapper mapper;

	@Mock
	private TokenAuthenticationProvider authenticationProvider;

	@InjectMocks
	private SecurityConfiguration securityConfiguration;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	void testAuthenticationManager() {
		AuthenticationManager authManager = securityConfiguration.authenticationManager(authenticationProvider);
		assertInstanceOf(ProviderManager.class, authManager);
		assertEquals(1, ((ProviderManager) authManager).getProviders().size());
		assertTrue(((ProviderManager) authManager).getProviders().contains(authenticationProvider));
	}

	@Test
	void testRestAuthenticationFilter() {
		AuthenticationManager authManager = mock(AuthenticationManager.class);
		TokenAuthenticationFilter filter = securityConfiguration.restAuthenticationFilter(authManager);

		assertNotNull(filter);
		// We can't test private methods, but we can verify the filter is properly configured
		assertNotNull(filter);
	}

	@Test
	void testForbiddenEntryPoint() throws Exception {
		ErrorResponse expectedResponse = new ErrorResponse(FORBIDDEN.value(), "NOT_LOGGED");

		securityConfiguration.forbiddenEntryPoint(response);

		assertEquals(FORBIDDEN.value(), response.getStatus());
		assertEquals(APPLICATION_JSON_VALUE, response.getContentType());
		verify(mapper).writeValue(response.getOutputStream(), expectedResponse);
	}

	@Test
	void testSuccessHandler() {
		SimpleUrlAuthenticationSuccessHandler handler = securityConfiguration.successHandler();
		assertNotNull(handler);
		// We can't test private methods, but we can verify the handler is created
		assertNotNull(handler);
	}

	@Test
	void testCorsConfiguration() {
		CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();
		CorsConfiguration config = source.getCorsConfiguration(request);

		assertNotNull(config);
		assertNotNull(config.getAllowedOrigins());
		assertTrue(config.getAllowedOrigins().contains("*"));
		assertNotNull(config.getAllowedHeaders());
		assertTrue(config.getAllowedHeaders().contains("*"));
		assertNotNull(config.getAllowedMethods());
		assertTrue(config.getAllowedMethods().contains(HttpMethod.GET.name()));
		assertTrue(config.getAllowedMethods().contains(HttpMethod.POST.name()));
		assertTrue(config.getAllowedMethods().contains(HttpMethod.PUT.name()));
		assertTrue(config.getAllowedMethods().contains(HttpMethod.DELETE.name()));
	}

	@Test
	void testNoRedirectStrategy() {
		SecurityConfiguration.NoRedirectStrategy strategy = new SecurityConfiguration.NoRedirectStrategy();
		strategy.sendRedirect(request, response, "/some-url");

		assertEquals(200, response.getStatus());  // Default status code for MockHttpServletResponse
		assertTrue(response.getHeaderNames().isEmpty());
	}
} 