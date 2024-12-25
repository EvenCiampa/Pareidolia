package com.pareidolia.controller.admin;

import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.service.admin.AdminPromoterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/admin/promoter")
public class AdminPromoterController {

	private final AdminPromoterService adminPromoterService;

	//update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO update(@RequestBody PromoterDTO promoterDTO) {
		return adminPromoterService.update(promoterDTO);
	}

	//add
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO create(@RequestBody PromoterDTO promoterDTO) {
		return adminPromoterService.create(promoterDTO);
	}

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminPromoterService.delete(id);
	}
}
