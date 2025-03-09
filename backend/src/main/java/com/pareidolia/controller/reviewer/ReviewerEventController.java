package com.pareidolia.controller.reviewer;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.service.reviewer.ReviewerEventService;
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
@RequestMapping(path = "/reviewer/event")
@PreAuthorize("hasAnyAuthority('REVIEWER')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Reviewer", description = "The Reviewer APIs")
public class ReviewerEventController {

	private final ReviewerEventService reviewerEventService;
	//display con stato PUBLISHED è nel generic

	//display
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO getEvent(@PathVariable("id") Long id) {
		return reviewerEventService.getEvent(id);
	}

	@GetMapping(value = "/{idPromoter}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDTO> getPromoterEvents(
		@PathVariable("idPromoter") Long idPromoter,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size,
		@RequestParam(value = "state", required = false) String state) {
		return reviewerEventService.getPromoterEvents(idPromoter, page, size, state);
	}

	@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDTO> getEvents(
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size,
		@RequestParam(value = "state", required = false) String state) {
		return reviewerEventService.getEvents(page, size, state);
	}

	@PostMapping("/{id}/backwards")
	public EventDTO moveBackwards(@PathVariable Long id) {
		return reviewerEventService.moveBackwards(id);
	}

	@PostMapping("/{id}/forward")
	public EventDTO moveForward(@PathVariable Long id) {
		return reviewerEventService.moveForward(id);
	}
}
