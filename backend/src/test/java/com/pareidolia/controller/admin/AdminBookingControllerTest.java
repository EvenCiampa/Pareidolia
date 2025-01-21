package com.pareidolia.controller.admin;

import com.pareidolia.dto.BookingDTO;
import com.pareidolia.service.admin.AdminBookingService;
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
public class AdminBookingControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private AdminBookingService adminBookingService;

    private final HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    public void setup() {
        headers.clear();
        headers.add("Authorization", "Bearer test-token");
    }

    @Test
    public void getBooking() {
        Long bookingId = 1L;
        BookingDTO bookingDTO = new BookingDTO();
        given(adminBookingService.getBooking(anyLong())).willReturn(bookingDTO);

        ResponseEntity<BookingDTO> response = restTemplate.exchange(
                "/admin/booking/{id}",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                BookingDTO.class,
                bookingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getBookings() {
        Long eventId = 1L;
        BookingDTO bookingDTO = new BookingDTO();
        Page<BookingDTO> bookingPage = new PageImpl<>(List.of(bookingDTO), PageRequest.of(0, 10), 1);
        given(adminBookingService.getBookings(anyLong(), any(), any())).willReturn(bookingPage);

        ParameterizedTypeReference<Page<BookingDTO>> type = new ParameterizedTypeReference<>() {};
        ResponseEntity<Page<BookingDTO>> response = restTemplate.exchange(
                "/admin/booking/{idEvent}/list",
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                type,
                eventId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(bookingPage, response.getBody());
    }

    @Test
    public void delete() {
        Long bookingId = 1L;

        ResponseEntity<Void> response = restTemplate.exchange(
                "/admin/booking/{id}",
                HttpMethod.DELETE,
                new HttpEntity<>(null, headers),
                Void.class,
                bookingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
} 