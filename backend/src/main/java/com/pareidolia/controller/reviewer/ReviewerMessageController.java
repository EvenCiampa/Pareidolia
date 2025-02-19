package com.pareidolia.controller.reviewer;

import com.pareidolia.dto.MessageDTO;
import com.pareidolia.service.reviewer.ReviewerMessageService;
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
@RequestMapping(path = "/reviewer/message")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Reviewer", description = "The Reviewer APIs")
public class ReviewerMessageController {

	private final ReviewerMessageService reviewerMessageService;

	//display
	//ritorna lista di messaggi riferiti al singolo eventDraft
	@GetMapping(value = "/{idEventDraft}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<MessageDTO> getEventDraftMessages(
		@PathVariable("idEventDraft") Long idEventDraft,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return reviewerMessageService.getEventDraftMessages(idEventDraft, page, size);
	}

	//add
	// crea un nuovo messaggio (idEventDraft è contenuto nella requestBody del DTO)
	@PostMapping(value = "/{idEventDraft}/create", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public MessageDTO create(@PathVariable("idEventDraft") Long idEventDraft, @RequestBody String message) {
		return reviewerMessageService.create(idEventDraft, message);
	}
}
