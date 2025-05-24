package com.pareidolia.controller.reviewer;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.MessageDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.reviewer.ReviewerMessageService;
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

import java.time.LocalDateTime;
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
class ReviewerMessageControllerTest {

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
	private ReviewerMessageService reviewerMessageService;

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
	void testGetEventDraftMessages() {
		Long eventId = 1L;
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setId(1L);
		messageDTO.setMessage("Test message");
		messageDTO.setCreationTime(LocalDateTime.now());

		Page<MessageDTO> messagePage = new PageImpl<>(List.of(messageDTO), PageRequest.of(0, 10), 1);
		given(reviewerMessageService.getEventDraftMessages(anyLong(), any(), any())).willReturn(messagePage);

		ParameterizedTypeReference<Page<MessageDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<MessageDTO>> response = restTemplate.exchange(
			"/reviewer/message/{idEventDraft}/list",
			HttpMethod.GET,
			new HttpEntity<>(null, headers),
			type,
			eventId);

		verify(reviewerMessageService).getEventDraftMessages(anyLong(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().getTotalElements());
		assertEquals(messagePage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(messagePage.getContent().get(0).getId(), response.getBody().getContent().get(0).getId());
		assertEquals(messagePage.getContent().get(0).getMessage(), response.getBody().getContent().get(0).getMessage());
		assertEquals(messagePage.getPageable(), response.getBody().getPageable());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerMessageService);
	}

	@Test
	void testCreate() {
		Long eventId = 1L;
		String message = "New message";
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setId(1L);
		messageDTO.setMessage(message);
		messageDTO.setCreationTime(LocalDateTime.now());

		given(reviewerMessageService.create(anyLong(), anyString())).willReturn(messageDTO);

		headers.setContentType(MediaType.TEXT_PLAIN);
		ResponseEntity<MessageDTO> response = restTemplate.exchange(
			"/reviewer/message/{idEventDraft}/create",
			HttpMethod.POST,
			new HttpEntity<>(message, headers),
			MessageDTO.class,
			eventId);

		verify(reviewerMessageService).create(anyLong(), eq(message));
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(messageDTO.getId(), response.getBody().getId());
		assertEquals(messageDTO.getMessage(), response.getBody().getMessage());

		verify(jwtService).verify(eq(authToken));
		verify(accountRepository).findByEmailAndPassword(eq(accountEmail), eq(accountPassword));
		verifyNoMoreInteractions(jwtService, accountRepository, reviewerMessageService);
	}
} 