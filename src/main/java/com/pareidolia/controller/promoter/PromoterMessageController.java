package com.pareidolia.controller.promoter;


import com.pareidolia.dto.MessageDTO;
import com.pareidolia.service.promoter.PromoterMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/promoter/message")
public class PromoterMessageController {

	private final PromoterMessageService promoterMessageService;

	//display
	//ritorna lista di messaggi riferiti al singolo eventDraft
	@GetMapping(value = "/{idEventDraft}/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<MessageDTO> getEventDraftMessages(
		@PathVariable("idEventDraft") Long idEventDraft,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return promoterMessageService.getEventDraftMessages(idEventDraft, page, size);
	}

	//add
	// crea un nuovo messaggio (idEventDraft è contenuto nella requestBody del DTO)
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public MessageDTO create(@RequestBody MessageDTO createMessageDTO) {
		return promoterMessageService.create(createMessageDTO);
	}

}
