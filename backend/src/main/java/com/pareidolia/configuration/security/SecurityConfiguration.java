package com.pareidolia.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pareidolia.configuration.error.ErrorResponse;
import com.pareidolia.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SecurityConfiguration {
	private static final RequestMatcher PROTECTED_URLS = new OrRequestMatcher(
		new AntPathRequestMatcher("/admin/**"),
		new AntPathRequestMatcher("/consumer/**"),
		new AntPathRequestMatcher("/promoter/**"),
		new AntPathRequestMatcher("/reviewer/**")
	);
	private final ObjectMapper mapper;

	@Bean
	public AuthenticationManager authenticationManager(TokenAuthenticationProvider authenticationProvider) {
		return new ProviderManager(authenticationProvider);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, TokenAuthenticationProvider authenticationProvider, AuthenticationManager authenticationManager) throws Exception {
		return http
			.cors(it -> it.configurationSource(corsConfigurationSource())) // enable CORS
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.requiresChannel(it -> it.anyRequest().requiresInsecure())
			.headers(it -> it.cacheControl(HeadersConfigurer.CacheControlConfig::disable))
			.sessionManagement(it -> it.sessionCreationPolicy(STATELESS)) // disable session cookies bc of token JWT
			.exceptionHandling(it -> it.authenticationEntryPoint((request, response, authException) -> forbiddenEntryPoint(response))) // enable exception handling
			.authenticationProvider(authenticationProvider) // authenticator object
			.addFilterBefore(restAuthenticationFilter(authenticationManager), AnonymousAuthenticationFilter.class)
			// if not authenticated, return this (1st parameter)
			.authorizeHttpRequests(it -> {
				it.requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.TRACE.name())).denyAll();
				it.requestMatchers(PROTECTED_URLS).authenticated();
				it.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole(Account.Type.ADMIN.name());
				it.requestMatchers(new AntPathRequestMatcher("/consumer/**")).hasRole(Account.Type.CONSUMER.name());
				it.requestMatchers(new AntPathRequestMatcher("/promoter/**")).hasRole(Account.Type.PROMOTER.name());
				it.requestMatchers(new AntPathRequestMatcher("/reviewer/**")).hasRole(Account.Type.REVIEWER.name());
				it.requestMatchers(new NegatedRequestMatcher(PROTECTED_URLS)).permitAll();
			}) // require validation for the following paths
			.build();
	}

	TokenAuthenticationFilter restAuthenticationFilter(AuthenticationManager authenticationManager) {
		TokenAuthenticationFilter filter = new TokenAuthenticationFilter(PROTECTED_URLS);
		filter.setAuthenticationManager(authenticationManager);
		filter.setAuthenticationSuccessHandler(successHandler());
		filter.setAuthenticationFailureHandler((request, response, authException) -> forbiddenEntryPoint(response));
		return filter;
	}

	void forbiddenEntryPoint(HttpServletResponse response) throws IOException {
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setStatus(FORBIDDEN.value());
		mapper.writeValue(
			response.getOutputStream(),
			new ErrorResponse(
				FORBIDDEN.value(),
				"NOT_LOGGED"
			)
		);
	}

	@Bean
	SimpleUrlAuthenticationSuccessHandler successHandler() {
		SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
		successHandler.setRedirectStrategy(new NoRedirectStrategy());
		return successHandler;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("*"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowedMethods(List.of(
			HttpMethod.GET.name(),
			HttpMethod.POST.name(),
			HttpMethod.PUT.name(),
			HttpMethod.DELETE.name()
		));
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	public static class NoRedirectStrategy implements RedirectStrategy {
		@Override
		public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) {
		}
	}

}