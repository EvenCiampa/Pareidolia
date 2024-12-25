package com.pareidolia.controller.admin;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.service.admin.AdminEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/admin/event")
public class AdminEventController {

	private final AdminEventService adminEventService;
	//display è nel generic

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
}
