package com.pareidolia.controller.generic;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.service.generic.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/generic/service")
public class PublicController {

	private final PublicService publicService;

	// - GET Event (by id or entire page)
	@GetMapping(value = "/event/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public EventDTO getEvent(@PathVariable("id") Long id) {
		return publicService.getEvent(id);
	}

	@GetMapping(value = "/event/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDTO> getEvents(@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return publicService.getEvents(page, size);
	}

	// - GET Promoter (by id or entire page)
	@GetMapping(value = "/promoter/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO getPromoter(@PathVariable("id") Long id) {
		return publicService.getPromoter(id);
	}

	@GetMapping(value = "/promoter/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<PromoterDTO> getPromoters(@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return publicService.getPromoters(page, size);
	}

	// - GET Promoter events (page)
	@GetMapping(value = "/promoter/{idPromoter}/events", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<EventDTO> getPromoterEvents(
		@PathVariable("idPromoter") Long idPromoter,
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return publicService.getPromoterEvents(idPromoter, page, size);
	}


	//      Event DTO data:
	//          - Event minimum data-> (descrizione nell'entity)
	//          - Promoters list with names and ids (Event ha una lista di promoters di tipo PromotersDTO)
	//          - review score (punteggio)

	//      Promoter DTO data:
	//          - Presentation (add to the entity somewhere)

	//      Promoter events (page) DTO data:
	//          - Page of Events, like the 1st api


}
