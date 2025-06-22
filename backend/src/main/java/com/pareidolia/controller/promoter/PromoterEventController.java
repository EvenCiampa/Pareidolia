package com.pareidolia.controller.promoter;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventUpdateDTO;
import com.pareidolia.service.promoter.PromoterEventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@SecurityRequirement(name = "JWT_Promoter")
@PreAuthorize("hasAnyAuthority('PROMOTER')")
@RequestMapping(path = "/promoter/event")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Promoter", description = "The Promoter APIs")
public class PromoterEventController {

	private final PromoterEventService promoterEventService;

	//display
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO getEventDraft(@PathVariable("id") Long id) {
		return promoterEventService.getEventDraft(id);
	}

	//display
	@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDTO> getEvents(
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size,
		@RequestParam(value = "state", required = false) String state) {
		return promoterEventService.getEvents(page, size, state);
	}

	// add = l'evento viene creato dal promoter principale e salvato.
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO create(@RequestBody EventUpdateDTO eventDraftDTO) {
		return promoterEventService.create(eventDraftDTO);
	}

	//addPromoterToEventDraft: aggiunge promoter a un evento esistente
	@PostMapping(value = "/{eventDraftId}/add-promoter/{promoterId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO addPromoterToEventDraft(
		@PathVariable Long eventDraftId,
		@PathVariable Long promoterId) {
		return promoterEventService.addPromoterToEventDraft(eventDraftId, promoterId);
	}

	// update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO update(@RequestBody EventUpdateDTO eventDraftDTO) {
		return promoterEventService.update(eventDraftDTO);
	}

	@PostMapping("/{id}/review")
	public EventDTO submitForReview(@PathVariable Long id) {
		return promoterEventService.submitForReview(id);
	}

	@PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public EventDTO updateEventImage(@PathVariable Long id, @RequestParam("image") MultipartFile file) {
		return promoterEventService.updateEventImage(id, file);
	}

	@DeleteMapping(value = "/{id}/image")
	public EventDTO deleteEventImage(@PathVariable Long id) {
		return promoterEventService.deleteEventImage(id);
	}

}
