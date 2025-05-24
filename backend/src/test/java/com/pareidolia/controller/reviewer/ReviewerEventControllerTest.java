package com.pareidolia.controller.reviewer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.reviewer.ReviewerEventService;
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
class ReviewerEventControllerTest {

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
	private ReviewerEventService reviewerEventService;

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
	void testGetEvent() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		eventDTO.setId(eventId);
		eventDTO.setTitle("Test Event");
		eventDTO.setDescription("Test Description");

		given(reviewerEventService.getEvent(anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/reviewer/event/{id}",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			EventDTO.class,
			eventId);

		verify(reviewerEventService).getEvent(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventDTO.getId(), response.getBody().getId());
		assertEquals(eventDTO.getTitle(), response.getBody().getTitle());
		assertEquals(eventDTO.getDescription(), response.getBody().getDescription());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerEventService);
	}

	@Test
	void testGetPromoterEvents() {
		Long promoterId = 1L;
		EventDTO eventDTO = new EventDTO();
		eventDTO.setId(1L);
		eventDTO.setTitle("Test Event");

		Page<EventDTO> eventPage = new PageImpl<>(List.of(eventDTO), PageRequest.of(0, 10), 1);
		given(reviewerEventService.getPromoterEvents(anyLong(), any(), any(), any())).willReturn(eventPage);

		ParameterizedTypeReference<Page<EventDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<EventDTO>> response = restTemplate.exchange(
			"/reviewer/event/{idPromoter}/list?state=PUBLISHED",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type,
			promoterId);

		verify(reviewerEventService).getPromoterEvents(anyLong(), any(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(eventPage.getContent(), response.getBody().getContent());
		assertEquals(eventPage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerEventService);
	}

	@Test
	void testGetEvents() {
		EventDTO eventDTO = new EventDTO();
		eventDTO.setId(1L);
		eventDTO.setTitle("Test Event");

		Page<EventDTO> eventPage = new PageImpl<>(List.of(eventDTO), PageRequest.of(0, 10), 1);
		given(reviewerEventService.getEvents(any(), any(), any())).willReturn(eventPage);

		ParameterizedTypeReference<Page<EventDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<EventDTO>> response = restTemplate.exchange(
			"/reviewer/event/list?state=PUBLISHED",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type);

		verify(reviewerEventService).getEvents(any(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(eventPage.getContent(), response.getBody().getContent());
		assertEquals(eventPage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerEventService);
	}

	@Test
	void testMoveBackwards() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		eventDTO.setId(eventId);
		eventDTO.setTitle("Test Event");
		eventDTO.setState("DRAFT");

		given(reviewerEventService.moveBackwards(anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/reviewer/event/{id}/backwards",
			HttpMethod.POST,
			new HttpEntity<>(null, headers),
			EventDTO.class,
			eventId);

		verify(reviewerEventService).moveBackwards(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventDTO.getId(), response.getBody().getId());
		assertEquals(eventDTO.getTitle(), response.getBody().getTitle());
		assertEquals(eventDTO.getState(), response.getBody().getState());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerEventService);
	}

	@Test
	void testMoveForward() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		eventDTO.setId(eventId);
		eventDTO.setTitle("Test Event");
		eventDTO.setState("PUBLISHED");

		given(reviewerEventService.moveForward(anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/reviewer/event/{id}/forward",
			HttpMethod.POST,
			new HttpEntity<>(null, headers),
			EventDTO.class,
			eventId);

		verify(reviewerEventService).moveForward(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventDTO.getId(), response.getBody().getId());
		assertEquals(eventDTO.getTitle(), response.getBody().getTitle());
		assertEquals(eventDTO.getState(), response.getBody().getState());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerEventService);
	}
} 