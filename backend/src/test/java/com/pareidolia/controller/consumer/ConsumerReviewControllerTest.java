package com.pareidolia.controller.consumer;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.service.consumer.ConsumerReviewService;
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
import org.springframework.http.*;
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
public class ConsumerReviewControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private ConsumerReviewService consumerReviewService;

    private final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    public void setup() {
        headers.clear();
        headers.add("Authorization", "Bearer test-token");
    }

    @Test
    public void getEventReviews() {
        Long eventId = 1L;
        ReviewDTO reviewDTO = new ReviewDTO();
        Page<ReviewDTO> reviewPage = new PageImpl<>(List.of(reviewDTO), PageRequest.of(0, 10), 1);
        given(consumerReviewService.getEventReviews(anyLong(), any(), any())).willReturn(reviewPage);

        ParameterizedTypeReference<Page<ReviewDTO>> type = new ParameterizedTypeReference<>() {};
        ResponseEntity<Page<ReviewDTO>> response = restTemplate.exchange(
                "/consumer/review/{idEvent}/list",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                type,
                eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(reviewPage, response.getBody());
    }

    @Test
    public void create() {
        ReviewDTO reviewDTO = new ReviewDTO();
        given(consumerReviewService.create(any(ReviewDTO.class))).willReturn(reviewDTO);

        ResponseEntity<ReviewDTO> response = restTemplate.exchange(
                "/consumer/review/create",
                HttpMethod.POST,
                new HttpEntity<>(reviewDTO, headers),
                ReviewDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
} 