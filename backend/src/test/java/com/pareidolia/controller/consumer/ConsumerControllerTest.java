package com.pareidolia.controller.consumer;

import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.service.consumer.ConsumerService;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ConsumerControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private ConsumerService consumerService;

    private final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    public void setup() {
        headers.clear();
        headers.add("Authorization", "Bearer test-token");
    }

    @Test
    public void getData() {
        ConsumerDTO consumerDTO = new ConsumerDTO();
        given(consumerService.getData()).willReturn(consumerDTO);

        ResponseEntity<ConsumerDTO> response = restTemplate.exchange(
                "/consumer/data",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                ConsumerDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void update() {
        ConsumerDTO consumerDTO = new ConsumerDTO();
        given(consumerService.update(any(ConsumerDTO.class))).willReturn(consumerDTO);

        ResponseEntity<ConsumerDTO> response = restTemplate.exchange(
                "/consumer/update",
                HttpMethod.POST,
                new HttpEntity<>(consumerDTO, headers),
                ConsumerDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
} 