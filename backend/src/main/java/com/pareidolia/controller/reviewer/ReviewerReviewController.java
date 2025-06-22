package com.pareidolia.controller.reviewer;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.service.reviewer.ReviewerReviewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@SecurityRequirement(name = "JWT_Reviewer")
@PreAuthorize("hasAnyAuthority('REVIEWER')")
@RequestMapping(path = "/reviewer/review")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Reviewer", description = "The Reviewer APIs")
public class ReviewerReviewController {

	private final ReviewerReviewService reviewerReviewService;

	@GetMapping(value = "/{idEvent}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<ReviewDTO> getEventReviews(@PathVariable("idEvent") Long idEvent,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return reviewerReviewService.getEventReviews(idEvent, page, size);
	}
}
