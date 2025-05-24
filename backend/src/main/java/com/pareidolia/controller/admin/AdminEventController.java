package com.pareidolia.controller.admin;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventUpdateDTO;
import com.pareidolia.service.admin.AdminEventService;
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
@SecurityRequirement(name = "JWT_Admin")
@RequestMapping(path = "/admin/event")
@PreAuthorize("hasAnyAuthority('ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
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
		@RequestParam(value = "state", required = false) String state) {
		return adminEventService.getPromoterEvents(idPromoter, page, size, state);
	}

	@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDTO> getEvents(
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size,
		@RequestParam(value = "state", required = false) String state) {
		return adminEventService.getEvents(page, size, state);
	}

	// add
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO create(@RequestBody EventUpdateDTO eventDraftDTO) {
		return adminEventService.create(eventDraftDTO);
	}

	// update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO update(@RequestBody EventUpdateDTO eventDTO) {
		return adminEventService.update(eventDTO);
	}

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminEventService.delete(id);
	}

	@PostMapping("/{id}/backwards")
	public EventDTO moveBackwards(@PathVariable Long id) {
		return adminEventService.moveBackwards(id);
	}

	@PostMapping("/{id}/forward")
	public EventDTO moveForward(@PathVariable Long id) {
		return adminEventService.moveForward(id);
	}

	@PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public EventDTO updateEventImage(@PathVariable Long id, @RequestParam("image") MultipartFile file) {
		return adminEventService.updateEventImage(id, file);
	}

	@DeleteMapping(value = "/{id}/image")
	public EventDTO deleteEventImage(@PathVariable Long id) {
		return adminEventService.deleteEventImage(id);
	}
}
