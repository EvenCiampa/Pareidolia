package com.pareidolia.controller.consumer;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.service.consumer.ConsumerEventService;
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
@SecurityRequirement(name = "JWT_Consumer")
@PreAuthorize("hasAnyAuthority('CONSUMER')")
@RequestMapping(path = "/consumer/event")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Consumer", description = "The Consumer APIs")
public class ConsumerEventController {

	private final ConsumerEventService consumerEventService;

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO getEvent(@PathVariable("id") Long id) {
		return consumerEventService.getEvent(id);
	}

	@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDTO> getEvents(@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return consumerEventService.getEvents(page, size);
	}

}
