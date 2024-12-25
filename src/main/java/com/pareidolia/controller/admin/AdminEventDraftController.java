package com.pareidolia.controller.admin;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventDraftDTO;
import com.pareidolia.service.admin.AdminEventDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/admin/event/draft")
public class AdminEventDraftController {

	private final AdminEventDraftService adminEventDraftService;

	//display
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDraftDTO getEventDraft(@PathVariable("id") Long id) {
		return adminEventDraftService.getEventDraft(id);
	}

	@GetMapping(value = "/{idPromoter}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDraftDTO> getEventDraftsPromoter(
		@PathVariable("idPromoter") Long idPromoter,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return adminEventDraftService.getPromoterEventDrafts(idPromoter, page, size);
	}

	@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDraftDTO> getEventDrafts(
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return adminEventDraftService.getEventDrafts(page, size);
	}

	// add
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDraftDTO create(@RequestBody EventDraftDTO eventDraftDTO, Long selectedPromoterId) {
		return adminEventDraftService.create(eventDraftDTO, selectedPromoterId);
	}

	// update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDraftDTO update(@RequestBody EventDraftDTO eventDraftDTO) {
		return adminEventDraftService.update(eventDraftDTO);
	}

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminEventDraftService.delete(id);
	}

	//publish
	@PostMapping(value = "/publish/{id}")
	public EventDTO publish(@PathVariable("id") Long id) {
		return adminEventDraftService.publish(id);
	}
}
