package com.pareidolia.controller.generic;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.service.generic.PublicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PublicControllerTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@MockitoBean
	private JWTService jwtService;
	@MockitoBean
	private PublicService publicService;
	@MockitoBean
	private AccountRepository accountRepository;

	@Test
	public void getEvent() {
		Long eventId = 1L;
		EventDTO eventDTO = new EventDTO();
		given(publicService.getEvent(anyLong())).willReturn(eventDTO);

		ResponseEntity<EventDTO> response = restTemplate.exchange(
			"/generic/service/event/{id}",
			HttpMethod.GET,
			null,
			EventDTO.class,
			eventId);

		verify(publicService).getEvent(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verifyNoMoreInteractions(jwtService, accountRepository, publicService);
	}

	@Test
	public void getEvents() {
		EventDTO eventDTO = new EventDTO();
		Page<EventDTO> eventPage = new PageImpl<>(List.of(eventDTO), PageRequest.of(0, 10), 1);
		given(publicService.getEvents(any(), any())).willReturn(eventPage);

		ParameterizedTypeReference<Page<EventDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<EventDTO>> response = restTemplate.exchange(
			"/generic/service/event/list",
			HttpMethod.GET,
			null,
			type);

		verify(publicService).getEvents(any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(eventPage.getContent(), response.getBody().getContent());
		assertEquals(eventPage.getPageable(), response.getBody().getPageable());

		verifyNoMoreInteractions(jwtService, accountRepository, publicService);
	}

	@Test
	public void getPromoter() {
		Long promoterId = 1L;
		PromoterDTO promoterDTO = new PromoterDTO();
		given(publicService.getPromoter(anyLong())).willReturn(promoterDTO);

		ResponseEntity<PromoterDTO> response = restTemplate.exchange(
			"/generic/service/promoter/{id}",
			HttpMethod.GET,
			null,
			PromoterDTO.class,
			promoterId);

		verify(publicService).getPromoter(anyLong());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		verifyNoMoreInteractions(jwtService, accountRepository, publicService);
	}

	@Test
	public void getPromoters() {
		PromoterDTO promoterDTO = new PromoterDTO();
		Page<PromoterDTO> promoterPage = new PageImpl<>(List.of(promoterDTO), PageRequest.of(0, 10), 1);
		given(publicService.getPromoters(any(), any())).willReturn(promoterPage);

		ParameterizedTypeReference<Page<PromoterDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<PromoterDTO>> response = restTemplate.exchange(
			"/generic/service/promoter/list",
			HttpMethod.GET,
			null,
			type);

		verify(publicService).getPromoters(any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(promoterPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(promoterPage.getContent(), response.getBody().getContent());
		assertEquals(promoterPage.getPageable(), response.getBody().getPageable());

		verifyNoMoreInteractions(jwtService, accountRepository, publicService);
	}

	@Test
	public void getPromoterEvents() {
		Long promoterId = 1L;
		EventDTO eventDTO = new EventDTO();
		Page<EventDTO> eventPage = new PageImpl<>(List.of(eventDTO), PageRequest.of(0, 10), 1);
		given(publicService.getPromoterEvents(anyLong(), any(), any())).willReturn(eventPage);

		ParameterizedTypeReference<Page<EventDTO>> type = new ParameterizedTypeReference<>() {
		};
		ResponseEntity<Page<EventDTO>> response = restTemplate.exchange(
			"/generic/service/promoter/{idPromoter}/events",
			HttpMethod.GET,
			null,
			type,
			promoterId);

		verify(publicService).getPromoterEvents(anyLong(), any(), any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(eventPage.getTotalElements(), response.getBody().getTotalElements());
		assertEquals(eventPage.getContent(), response.getBody().getContent());
		assertEquals(eventPage.getPageable(), response.getBody().getPageable());

		verifyNoMoreInteractions(jwtService, accountRepository, publicService);
	}

	@Test
	void testGetImage() {
		// Setup
		String imageName = "test.jpg";
		ResponseEntity<Resource> expectedResponse = ResponseEntity.ok(new ByteArrayResource(new byte[1]));
		when(publicService.getImage(imageName)).thenReturn(expectedResponse);

		// Test
		ResponseEntity<Resource> response = restTemplate.exchange(
			"/generic/service/image/{imageName}",
			HttpMethod.GET,
			null,
			Resource.class,
			imageName);

		// Verify
		verify(publicService).getImage(any());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedResponse.getBody(), response.getBody());

		verifyNoMoreInteractions(jwtService, accountRepository, publicService);
	}
}
