package com.pareidolia.configuration.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.pareidolia.entity.Account;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JWTService {
	private final Algorithm algorithm;

	public JWTService(@Value("jwt.secret") String secret) {
		this.algorithm = Algorithm.HMAC256(secret);
	}

	public String create(Account.Type referenceType, String username, String password) {
		LocalDateTime issuedAt = LocalDateTime.now();
		return JWT.create()
			.withIssuedAt(Date.from(issuedAt.atZone(ZoneOffset.systemDefault()).toInstant()))
			.withClaim("referenceType", referenceType.name())
			.withClaim("username", username)
			.withClaim("password", password)
			.sign(algorithm);
	}

	public Map<String, Object> verify(String token) throws TokenVerificationException {
		JWTVerifier verifier = JWT.require(algorithm).build();
		try {
			DecodedJWT jwt = verifier.verify(token);
			return jwt.getClaims().entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().as(Object.class)));
		} catch (Exception e) {
			throw new TokenVerificationException(e);
		}
	}

	@NoArgsConstructor
	public static class TokenVerificationException extends RuntimeException {
		public TokenVerificationException(Throwable t) {
			super(t);
		}

		public TokenVerificationException(String msg) {
			super(msg);
		}
	}
}