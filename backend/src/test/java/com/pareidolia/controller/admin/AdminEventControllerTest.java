package com.pareidolia.controller.admin;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventUpdateDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.admin.AdminEventService;
import com.pareidolia.state.DraftState;
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
public class AdminEventControllerTest {

	private final HttpHeaders headers = new HttpHeaders();

	private final Account.Type accountType = Account.Type.ADMIN;
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
	private AdminEventService adminEventService;

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
	public void getEvent() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		given(adminEventService.getEvent(anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/admin/event/{id}",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			EventDTO.class,
			eventId);

		verify(adminEventService).getEvent(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	@Test
	public void getPromoterEvents() {
		Long promoterId = 1L;
		EventDTO eventDTO = new EventDTO();
		Page<EventDTO> eventPage = new PageImpl<>(List.of(eventDTO), PageRequest.of(0, 10), 1);
		given(adminEventService.getPromoterEvents(anyLong(), any(), any(), any())).willReturn(eventPage);

		ParameterizedTypeReference<Page<EventDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<EventDTO>> response = restTemplate.exchange(
			"/admin/event/{idPromoter}/list",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type,
			promoterId);

		verify(adminEventService).getPromoterEvents(anyLong(), any(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(eventPage.getContent(), response.getBody().getContent());
		assertEquals(eventPage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	@Test
	public void getEvents() {
		EventDTO eventDTO = new EventDTO();
		Page<EventDTO> eventPage = new PageImpl<>(List.of(eventDTO), PageRequest.of(0, 10), 1);
		given(adminEventService.getEvents(any(), any(), any())).willReturn(eventPage);

		ParameterizedTypeReference<Page<EventDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<EventDTO>> response = restTemplate.exchange(
			"/admin/event/list",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type);

		verify(adminEventService).getEvents(any(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(eventPage.getContent(), response.getBody().getContent());
		assertEquals(eventPage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	@Test
	public void create() {
		EventDTO eventDTO = new EventDTO();
		EventUpdateDTO eventUpdateDTO = new EventUpdateDTO();
		given(adminEventService.create(any(EventUpdateDTO.class))).willReturn(eventDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/admin/event/create",
			HttpMethod.POST,
			new HttpEntity<>(eventUpdateDTO, headers),
			EventDTO.class);

		verify(adminEventService).create(any(EventUpdateDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	@Test
	public void update() {
		EventDTO eventDTO = new EventDTO();
		EventUpdateDTO eventUpdateDTO = new EventUpdateDTO();
		given(adminEventService.update(any(EventUpdateDTO.class))).willReturn(eventDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/admin/event/update",
			HttpMethod.POST,
			new HttpEntity<>(eventUpdateDTO, headers),
			EventDTO.class);

		verify(adminEventService).update(any(EventUpdateDTO.class));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	@Test
	public void delete() {
		Long eventId = 1L;

		ResponseEntity<Void> response = restTemplate.exchange(
			"/admin/event/{id}",
			HttpMethod.DELETE,
			new HttpEntity<>(null, headers),
			Void.class,
			eventId);

		verify(adminEventService).delete(eq(eventId));
		assertEquals(HttpStatus.OK, response.getStatusCode());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	@Test
	public void moveBackwards() {
		Long eventId = 1L;
		String eventState = DraftState.name;
		EventDTO eventDTO = new EventDTO();
		given(adminEventService.moveBackwards(anyLong())).willReturn(eventDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/admin/event/{id}/backwards",
			HttpMethod.POST,
			new HttpEntity<>(eventDTO, headers),
			EventDTO.class,
			eventId,
			eventState);

		verify(adminEventService).moveBackwards(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	@Test
	public void moveForward() {
		Long eventId = 1L;
		String eventState = DraftState.name;
		EventDTO eventDTO = new EventDTO();
		given(adminEventService.moveForward(anyLong())).willReturn(eventDTO);

		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/admin/event/{id}/forward",
			HttpMethod.POST,
			new HttpEntity<>(eventDTO, headers),
			EventDTO.class,
			eventId,
			eventState);

		verify(adminEventService).moveForward(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	@Test
	public void updateEventImage() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		given(adminEventService.updateEventImage(anyLong(), any())).willReturn(eventDTO);

		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/admin/event/{id}/image",
			HttpMethod.POST,
			new HttpEntity<>(createMultipartBody(file), headers),
			EventDTO.class,
			eventId);

		verify(adminEventService).updateEventImage(eq(eventId), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	@Test
	public void deleteEventImage() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		given(adminEventService.deleteEventImage(anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/admin/event/{id}/image",
			HttpMethod.DELETE,
			new HttpEntity<>(null, headers),
			EventDTO.class,
			eventId);

		verify(adminEventService).deleteEventImage(eq(eventId));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, adminEventService);
	}

	private MultiValueMap<String, Object> createMultipartBody(MultipartFile file) {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("image", file.getResource());
		return body;
	}
}
