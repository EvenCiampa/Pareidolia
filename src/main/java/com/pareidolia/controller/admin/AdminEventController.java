package com.pareidolia.controller.admin;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.entity.Event;
import com.pareidolia.service.admin.AdminEventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/admin/event")
@SecurityRequirement(name = "JWT_Admin")
@Tag(name = "Admin", description = "The Admin APIs")
public class AdminEventController {

	private final AdminEventService adminEventService;
	//display con stato PUBLISHED è nel generic

	//display
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO getEvent(@PathVariable("id") Long id) {
		return adminEventService.getEvent(id);
	}

	@GetMapping(value = "/{idPromoter}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDTO> getPromoterEvents(
		@PathVariable("idPromoter") Long idPromoter,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size,
		@RequestParam(value = "state", required = false) Event.EventState state) {
		return adminEventService.getPromoterEvents(idPromoter, page, size, state);
	}

	@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDTO> getEvents(
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size,
		@RequestParam(value = "state", required = false) Event.EventState state) {
		return adminEventService.getEvents(page, size, state);
	}

	// add
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO create(@RequestBody EventDTO eventDraftDTO) {
		return adminEventService.create(eventDraftDTO);
	}

	// update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO update(@RequestBody EventDTO eventDTO) {
		return adminEventService.update(eventDTO);
	}

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminEventService.delete(id);
	}

	// moveToState
	@PostMapping("/{id}/{state}")
	public EventDTO moveToState(@PathVariable Long id, @PathVariable Event.EventState state) {
		return adminEventService.moveToState(id, state);
	}
}
