package com.pareidolia.controller.promoter;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.service.promoter.PromoterEventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/promoter/event/draft")
@SecurityRequirement(name = "JWT_Promoter")
@Tag(name = "Promoter", description = "The Promoter APIs")
public class PromoterEventController {

	private final PromoterEventService promoterEventDraftService;

	//display
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO getEventDraft(@PathVariable("id") Long id) {
		return promoterEventDraftService.getEventDraft(id);
	}

	// add = l'evento viene creato dal promoter principale e salvato.
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO create(@RequestBody EventDTO eventDraftDTO) {
		return promoterEventDraftService.create(eventDraftDTO);
	}

	//addPromoterToEventDraft: aggiunge promoter a un evento esistente
	@PostMapping(value = "/{eventDraftId}/add-promoter/{promoterId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO addPromoterToEventDraft(
		@PathVariable Long eventDraftId,
		@PathVariable Long promoterId) {
		return promoterEventDraftService.addPromoterToEventDraft(eventDraftId, promoterId);
	}

	// update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO update(@RequestBody EventDTO eventDraftDTO) {
		return promoterEventDraftService.update(eventDraftDTO);
	}

	@PostMapping("/{id}/review")
	public EventDTO submitForReview(@PathVariable Long id) {
		return promoterEventDraftService.submitForReview(id);
	}

}
