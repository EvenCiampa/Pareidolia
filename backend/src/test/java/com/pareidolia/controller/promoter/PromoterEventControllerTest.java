package com.pareidolia.controller.promoter;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventUpdateDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.promoter.PromoterEventService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

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
public class PromoterEventControllerTest {

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
	private PromoterEventService promoterEventService;

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
	public void getEventDraft() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		given(promoterEventService.getEventDraft(anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/promoter/event/{id}",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			EventDTO.class,
			eventId);

		verify(promoterEventService).getEventDraft(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterEventService);
	}

	@Test
	public void getEvents() {
		EventDTO eventDTO = new EventDTO();
		Page<EventDTO> eventPage = new PageImpl<>(List.of(eventDTO), PageRequest.of(0, 10), 1);
		given(promoterEventService.getEvents(any(), any(), any())).willReturn(eventPage);

		ParameterizedTypeReference<Page<EventDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<EventDTO>> response = restTemplate.exchange(
			"/promoter/event/list",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type);

		verify(promoterEventService).getEvents(any(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(eventPage.getContent(), response.getBody().getContent());
		assertEquals(eventPage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterEventService);
	}

	@Test
	public void create() {
		EventDTO eventDTO = new EventDTO();
		EventUpdateDTO eventUpdateDTO = new EventUpdateDTO();
		given(promoterEventService.create(any(EventUpdateDTO.class))).willReturn(eventDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/promoter/event/create",
			HttpMethod.POST,
			new HttpEntity<>(eventUpdateDTO, headers),
			EventDTO.class);

		verify(promoterEventService).create(any(EventUpdateDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterEventService);
	}

	@Test
	public void addPromoterToEventDraft() {
		Long eventDraftId = 1L;
		Long promoterId = 2L;
		EventDTO eventDTO = new EventDTO();
		given(promoterEventService.addPromoterToEventDraft(anyLong(), anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/promoter/event/{eventDraftId}/add-promoter/{promoterId}",
			HttpMethod.POST,
			new HttpEntity<>(null, headers),
			EventDTO.class,
			eventDraftId,
			promoterId);

		verify(promoterEventService).addPromoterToEventDraft(anyLong(), anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterEventService);
	}

	@Test
	public void update() {
		EventDTO eventDTO = new EventDTO();
		EventUpdateDTO eventUpdateDTO = new EventUpdateDTO();
		given(promoterEventService.update(any(EventUpdateDTO.class))).willReturn(eventDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/promoter/event/update",
			HttpMethod.POST,
			new HttpEntity<>(eventUpdateDTO, headers),
			EventDTO.class);

		verify(promoterEventService).update(any(EventUpdateDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterEventService);
	}

	@Test
	public void submitForReview() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		given(promoterEventService.submitForReview(anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/promoter/event/{id}/review",
			HttpMethod.POST,
			new HttpEntity<>(null, headers),
			EventDTO.class,
			eventId);

		verify(promoterEventService).submitForReview(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterEventService);
	}

	@Test
	public void updateEventImage() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		given(promoterEventService.updateEventImage(anyLong(), any())).willReturn(eventDTO);

		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/promoter/event/{id}/image",
			HttpMethod.POST,
			new HttpEntity<>(createMultipartBody(file), headers),
			EventDTO.class,
			eventId);

		verify(promoterEventService).updateEventImage(eq(eventId), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterEventService);
	}

	@Test
	public void deleteEventImage() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		given(promoterEventService.deleteEventImage(anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/promoter/event/{id}/image",
			HttpMethod.DELETE,
			new HttpEntity<>(null, headers),
			EventDTO.class,
			eventId);

		verify(promoterEventService).deleteEventImage(eq(eventId));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, promoterEventService);
	}

	private MultiValueMap<String, Object> createMultipartBody(MultipartFile file) {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("image", file.getResource());
		return body;
	}

}
