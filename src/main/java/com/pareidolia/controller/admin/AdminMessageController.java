package com.pareidolia.controller.admin;


import com.pareidolia.dto.MessageDTO;
import com.pareidolia.service.admin.AdminMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/admin/message")
public class AdminMessageController {

	private final AdminMessageService adminMessageService;

	//display
	//ritorna lista di messaggi riferiti al singolo eventDraft
	@GetMapping(value = "/{idEventDraft}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<MessageDTO> getEventDraftMessages(
		@PathVariable("idEventDraft") Long idEventDraft,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return adminMessageService.getEventDraftMessages(idEventDraft, page, size);
	}

	//add
	// crea un nuovo messaggio (idEventDraft è contenuto nella requestBody del DTO)
	@PostMapping(value = "/{idEventDraft}/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public MessageDTO create(
		@PathVariable("idEventDraft") Long idEventDraft,
		@RequestBody String message) {
		return adminMessageService.create(idEventDraft, message);
	}

	//delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminMessageService.delete(id);
	}
}
