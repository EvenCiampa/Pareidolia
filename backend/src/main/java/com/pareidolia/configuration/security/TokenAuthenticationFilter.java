package com.pareidolia.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.Optional;

public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER = "Bearer";

	public TokenAuthenticationFilter(RequestMatcher requiresAuth) {
		super(requiresAuth);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		String token = Optional.ofNullable(request.getHeader(AUTHORIZATION))
			.map(v -> v.replace(BEARER, "").strip())
			.orElseThrow(() -> new BadCredentialsException("Missing authentication token"));

		Authentication auth = new UsernamePasswordAuthenticationToken(token, token);
		return getAuthenticationManager().authenticate(auth);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		FilterChain chain, Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
		chain.doFilter(request, response);
	}
}