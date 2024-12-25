package com.pareidolia.controller.promoter;

import com.pareidolia.dto.EventDraftDTO;
import com.pareidolia.service.promoter.PromoterEventDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/promoter/event/draft")
public class PromoterEventDraftController {

	private final PromoterEventDraftService promoterEventDraftService;

	//display
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDraftDTO getEventDraft(@PathVariable("id") Long id) {
		return promoterEventDraftService.getEventDraft(id);
	}

	// add = l'evento viene creato dal promoter principale e salvato.
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDraftDTO create(@RequestBody EventDraftDTO eventDraftDTO) {
		return promoterEventDraftService.create(eventDraftDTO);
	}

	//addPromoterToEventDraft: aggiunge promoter a un evento esistente
	@PostMapping(value = "/{eventDraftId}/add-promoter/{promoterId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDraftDTO addPromoterToEventDraft(
		@PathVariable Long eventDraftId,
		@PathVariable Long promoterId) {
		return promoterEventDraftService.addPromoterToEventDraft(eventDraftId, promoterId);
	}

	// update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDraftDTO update(@RequestBody EventDraftDTO eventDraftDTO) {
		return promoterEventDraftService.update(eventDraftDTO);
	}

}
