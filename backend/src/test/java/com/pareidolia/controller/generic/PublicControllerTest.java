package com.pareidolia.controller.generic;

import com.pareidolia.dto.PublishedEventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.service.generic.PublicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

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
public class PublicControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private PublicService publicService;

    @BeforeEach
    public void setup() {
        // Setup if needed
    }

    @Test
    public void getEvent() {
        Long eventId = 1L;
        PublishedEventDTO eventDTO = new PublishedEventDTO();
        given(publicService.getEvent(anyLong())).willReturn(eventDTO);

        ResponseEntity<PublishedEventDTO> response = restTemplate.exchange(
                "/generic/service/event/{id}",
                HttpMethod.GET,
                null,
                PublishedEventDTO.class,
                eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getEvents() {
        PublishedEventDTO eventDTO = new PublishedEventDTO();
        Page<PublishedEventDTO> eventPage = new PageImpl<>(List.of(eventDTO), PageRequest.of(0, 10), 1);
        given(publicService.getEvents(any(), any())).willReturn(eventPage);

        ParameterizedTypeReference<Page<PublishedEventDTO>> type = new ParameterizedTypeReference<>() {};
        ResponseEntity<Page<PublishedEventDTO>> response = restTemplate.exchange(
                "/generic/service/event/list",
                HttpMethod.GET,
                null,
                type);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(eventPage, response.getBody());
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

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getPromoters() {
        PromoterDTO promoterDTO = new PromoterDTO();
        Page<PromoterDTO> promoterPage = new PageImpl<>(List.of(promoterDTO), PageRequest.of(0, 10), 1);
        given(publicService.getPromoters(any(), any())).willReturn(promoterPage);

        ParameterizedTypeReference<Page<PromoterDTO>> type = new ParameterizedTypeReference<>() {};
        ResponseEntity<Page<PromoterDTO>> response = restTemplate.exchange(
                "/generic/service/promoter/list",
                HttpMethod.GET,
                null,
                type);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(promoterPage, response.getBody());
    }

    @Test
    public void getPromoterEvents() {
        Long promoterId = 1L;
        PublishedEventDTO eventDTO = new PublishedEventDTO();
        Page<PublishedEventDTO> eventPage = new PageImpl<>(List.of(eventDTO), PageRequest.of(0, 10), 1);
        given(publicService.getPromoterEvents(anyLong(), any(), any())).willReturn(eventPage);

        ParameterizedTypeReference<Page<PublishedEventDTO>> type = new ParameterizedTypeReference<>() {};
        ResponseEntity<Page<PublishedEventDTO>> response = restTemplate.exchange(
                "/generic/service/promoter/{idPromoter}/events",
                HttpMethod.GET,
                null,
                type,
                promoterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(eventPage, response.getBody());
    }
} 