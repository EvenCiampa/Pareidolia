package com.pareidolia.service.consumer;

import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.Review;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.ReviewRepository;
import com.pareidolia.validator.ReviewValidator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ConsumerReviewServiceTest {

    @Autowired
    private ConsumerReviewService consumerReviewService;

    @MockitoBean
    private ReviewValidator reviewValidator;

    @MockitoBean
    private EventRepository eventRepository;

    @MockitoBean
    private ConsumerService consumerService;

    @MockitoBean
    private ReviewRepository reviewRepository;

    private Review testReview;
    private Event testEvent;
    private ConsumerDTO testConsumerDTO;

    @BeforeEach
    public void setup() {
        testReview = new Review();
        testReview.setId(1L);
        testReview.setIdEvent(1L);
        testReview.setIdConsumer(1L);

        testEvent = new Event();
        testEvent.setId(1L);

        testConsumerDTO = new ConsumerDTO();
        testConsumerDTO.setId(1L);

        when(consumerService.getData()).thenReturn(testConsumerDTO);
    }

    @Test
    public void testGetEventReviews() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reviewRepository.findAllByIdEvent(1L)).thenReturn(Collections.singletonList(testReview));
        
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(testReview));
        when(reviewRepository.findAllByIdEvent(eq(1L), any(PageRequest.class))).thenReturn(reviewPage);

        Page<ReviewDTO> result = consumerReviewService.getEventReviews(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(reviewValidator).validateEventIsOver(1L);
    }

    @Test
    public void testGetEventReviews_InvalidEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> consumerReviewService.getEventReviews(1L, 0, 10));
    }

    @Test
    public void testCreate() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setIdEvent(1L);

        when(reviewRepository.save(any())).thenReturn(testReview);

        ReviewDTO result = consumerReviewService.create(reviewDTO);

        assertNotNull(result);
        verify(reviewValidator).validateEventIsOver(1L);
        verify(reviewValidator).validateNewReviewFields(reviewDTO);
        verify(reviewRepository).save(any());
    }
} 