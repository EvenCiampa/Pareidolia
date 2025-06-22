package com.pareidolia.configuration.security;

import com.pareidolia.configuration.security.jwt.JWTAuthenticationService;
import com.pareidolia.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationProviderTest {

	@Mock
	private JWTAuthenticationService jwtAuthenticationService;

	@InjectMocks
	private TokenAuthenticationProvider tokenAuthenticationProvider;

	private UsernamePasswordAuthenticationToken authenticationToken;

	@BeforeEach
	void setUp() {
		authenticationToken = new UsernamePasswordAuthenticationToken("username", "token");
	}

	@Test
	void testRetrieveUserWithValidToken() {
		Account account = new Account();
		account.setEmail("test@example.com");
		account.setPassword("password");
		account.setReferenceType(Account.Type.CONSUMER);

		when(jwtAuthenticationService.authenticateByToken("token")).thenReturn(account);

		UserDetails userDetails = tokenAuthenticationProvider.retrieveUser("username", authenticationToken);

		assertNotNull(userDetails);
		assertEquals("test@example.com", userDetails.getUsername());
		assertEquals("password", userDetails.getPassword());
		assertTrue(userDetails.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("CONSUMER")));

		verify(jwtAuthenticationService).authenticateByToken("token");
	}

	@Test
	void testRetrieveUserWithNullToken() {
		authenticationToken = new UsernamePasswordAuthenticationToken("username", null);

		assertThrows(BadCredentialsException.class, () ->
			tokenAuthenticationProvider.retrieveUser("username", authenticationToken)
		);

		verify(jwtAuthenticationService, never()).authenticateByToken(any());
	}

	@Test
	void testRetrieveUserWithInvalidToken() {
		when(jwtAuthenticationService.authenticateByToken("token"))
			.thenThrow(new BadCredentialsException("Invalid token"));

		assertThrows(BadCredentialsException.class, () ->
			tokenAuthenticationProvider.retrieveUser("username", authenticationToken)
		);

		verify(jwtAuthenticationService).authenticateByToken("token");
	}

	@Test
	void testAdditionalAuthenticationChecks() {
		// This method is empty in the implementation, but we should test it doesn't throw any exception
		assertDoesNotThrow(() ->
			tokenAuthenticationProvider.additionalAuthenticationChecks(null, null)
		);
	}
} 