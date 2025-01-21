package com.pareidolia.controller.promoter;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.service.promoter.PromoterEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PromoterEventControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private PromoterEventService promoterEventService;

    private final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    public void setup() {
        headers.clear();
        headers.add("Authorization", "Bearer test-token");
    }

    @Test
    public void getEventDraft() {
        Long eventId = 1L;
        EventDTO eventDTO = new EventDTO();
        given(promoterEventService.getEventDraft(anyLong())).willReturn(eventDTO);

        ResponseEntity<EventDTO> response = restTemplate.exchange(
                "/promoter/event/draft/{id}",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                EventDTO.class,
                eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void create() {
        EventDTO eventDTO = new EventDTO();
        given(promoterEventService.create(any(EventDTO.class))).willReturn(eventDTO);

        ResponseEntity<EventDTO> response = restTemplate.exchange(
                "/promoter/event/draft/create",
                HttpMethod.POST,
                new HttpEntity<>(eventDTO, headers),
                EventDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void addPromoterToEventDraft() {
        Long eventDraftId = 1L;
        Long promoterId = 2L;
        EventDTO eventDTO = new EventDTO();
        given(promoterEventService.addPromoterToEventDraft(anyLong(), anyLong())).willReturn(eventDTO);

        ResponseEntity<EventDTO> response = restTemplate.exchange(
                "/promoter/event/draft/{eventDraftId}/add-promoter/{promoterId}",
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                EventDTO.class,
                eventDraftId,
                promoterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void update() {
        EventDTO eventDTO = new EventDTO();
        given(promoterEventService.update(any(EventDTO.class))).willReturn(eventDTO);

        ResponseEntity<EventDTO> response = restTemplate.exchange(
                "/promoter/event/draft/update",
                HttpMethod.POST,
                new HttpEntity<>(eventDTO, headers),
                EventDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void submitForReview() {
        Long eventId = 1L;
        EventDTO eventDTO = new EventDTO();
        given(promoterEventService.submitForReview(anyLong())).willReturn(eventDTO);

        ResponseEntity<EventDTO> response = restTemplate.exchange(
                "/promoter/event/draft/{id}/review",
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                EventDTO.class,
                eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
} 