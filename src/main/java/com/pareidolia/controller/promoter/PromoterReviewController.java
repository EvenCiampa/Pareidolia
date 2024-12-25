package com.pareidolia.controller.promoter;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.service.promoter.PromoterReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/promoter/review")
public class PromoterReviewController {

	private final PromoterReviewService promoterReviewService;

	//ritorna lista di commenti riferiti al singolo evento(concluso)
	@GetMapping(value = "/{idEvent}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<ReviewDTO> getEventReviews(@PathVariable("idEvent") Long idEvent,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return promoterReviewService.getEventReviews(idEvent, page, size);
	}

	// crea una nuova review (idEvent è contenuto nella requestBody del DTO) QUINDI HO DECISO CHE PUò COMMENTARE IL PROMOTER
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReviewDTO create(@RequestBody ReviewDTO createReviewDTO) {return promoterReviewService.create(createReviewDTO);}

}
