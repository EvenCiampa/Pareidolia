package com.pareidolia.configuration.security;

import com.pareidolia.configuration.security.jwt.JWTAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	private final JWTAuthenticationService jwtAuthenticationService;

	@Override
	protected void additionalAuthenticationChecks(UserDetails details, UsernamePasswordAuthenticationToken credentials) {
	}

	@Override
	public UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) {
		Object token = authentication.getCredentials();
		return Optional
			.ofNullable(token)
			.flatMap(t ->
				Optional.of(jwtAuthenticationService.authenticateByToken(String.valueOf(t)))
					.map(u -> User.builder()
						.username(u.getEmail())
						.password(u.getPassword())
						.authorities(u.getReferenceType().name())
						.build()))
			.orElseThrow(() -> new BadCredentialsException("Invalid authentication token=" + token));
	}
}