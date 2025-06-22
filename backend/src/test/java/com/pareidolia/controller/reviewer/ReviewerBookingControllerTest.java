package com.pareidolia.controller.reviewer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.BookingDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.reviewer.ReviewerBookingService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewerBookingControllerTest {

	private final HttpHeaders headers = new HttpHeaders();
	private final Account.Type accountType = Account.Type.REVIEWER;
	private final String accountEmail = "reviewer@mail.it";
	private final String accountPassword = "TestPassword123#";
	private final String authToken = "TEST_TOKEN";

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private JWTService jwtService;
	@MockitoBean
	private AccountRepository accountRepository;
	@MockitoBean
	private ReviewerBookingService reviewerBookingService;

	@BeforeEach
	void setUp() {
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
	void testGetBooking() {
		Long bookingId = 1L;
		BookingDTO bookingDTO = new BookingDTO();
		given(reviewerBookingService.getBooking(anyLong())).willReturn(bookingDTO);

		ResponseEntity<BookingDTO> response = restTemplate.exchange(
			"/reviewer/booking/{id}",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			BookingDTO.class,
			bookingId);

		verify(reviewerBookingService).getBooking(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerBookingService);
	}

	@Test
	void testGetBookings() {
		Long eventId = 1L;
		BookingDTO bookingDTO = new BookingDTO();
		Page<BookingDTO> bookingPage = new PageImpl<>(List.of(bookingDTO), PageRequest.of(0, 10), 1);
		given(reviewerBookingService.getBookings(anyLong(), any(), any())).willReturn(bookingPage);

		ParameterizedTypeReference<Page<BookingDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<BookingDTO>> response = restTemplate.exchange(
			"/reviewer/booking/{idEvent}/list",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type,
			eventId);

		verify(reviewerBookingService).getBookings(anyLong(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(bookingPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(bookingPage.getContent(), response.getBody().getContent());
		assertEquals(bookingPage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerBookingService);
	}
} 