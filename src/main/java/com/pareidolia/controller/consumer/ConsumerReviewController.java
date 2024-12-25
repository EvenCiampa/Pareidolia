package com.pareidolia.controller.consumer;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.service.consumer.ConsumerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/consumer/review")
public class ConsumerReviewController {

	private final ConsumerReviewService consumerReviewService;

	//ritorna lista di commenti
	@GetMapping(value = "/{idEvent}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<ReviewDTO> getEventReviews(@PathVariable("idEvent") Long idEvent,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return consumerReviewService.getEventReviews(idEvent, page, size);
	}

	// crea una nuova review
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReviewDTO create(@RequestBody ReviewDTO createReviewDTO) {
		return consumerReviewService.create(createReviewDTO);
	}

}
