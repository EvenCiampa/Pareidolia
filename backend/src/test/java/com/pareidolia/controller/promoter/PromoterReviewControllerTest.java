package com.pareidolia.controller.promoter;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.promoter.PromoterReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PromoterReviewControllerTest {

	private final HttpHeaders headers = new HttpHeaders();

	private final Account.Type accountType = Account.Type.PROMOTER;
	private final String accountEmail = "account@mail.it";
	private final String accountPassword = "TestPassword123#";
	private final String authToken = "TEST_TOKEN";

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private JWTService jwtService;
	@MockitoBean
	private AccountRepository accountRepository;
	@MockitoBean
	private PromoterReviewService promoterReviewService;

	@BeforeEach
	public void setup() {
		headers.clear();
		headers.setBearerAuth(authToken);

		doReturn(Map.of(
			"referenceType", accountType.name(),
			"username", accountEmail,
			"password", accountPassword
		)).when(jwtService).verify(eq(authToken));

		doReturn(Optional.of(
			Account.builder()
				.email(accountEmail)
				.password(accountPassword)
				.referenceType(accountType)
				.build()
		)).when(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
	}

	@Test
	public void getEventReviews() {
		Long idEvent = 1L;
		ReviewDTO reviewDTO = new ReviewDTO();
		Page<ReviewDTO> reviewPage = new PageImpl<>(List.of(reviewDTO), PageRequest.of(0, 10), 1);
		given(promoterReviewService.getEventReviews(any(), any(), any())).willReturn(reviewPage);

		ParameterizedTypeReference<Page<ReviewDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<ReviewDTO>> response = restTemplate.exchange(
			"/promoter/review/{idEvent}/list",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type,
			idEvent);

		verify(promoterReviewService).getEventReviews(any(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(reviewPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(reviewPage.getContent(), response.getBody().getContent());
		assertEquals(reviewPage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterReviewService);
	}

	@Test
	public void create() {
		ReviewDTO reviewDT = new ReviewDTO();
		given(promoterReviewService.create(any(ReviewDTO.class))).willReturn(reviewDT);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<ReviewDTO> response = restTemplate.exchange(
			"/promoter/review/create",
			HttpMethod.POST,
			new HttpEntity<>(reviewDT, headers),
			ReviewDTO.class);

		verify(promoterReviewService).create(any(ReviewDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterReviewService);
	}

}
