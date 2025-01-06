package com.pareidolia.controller.admin;

import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.service.admin.AdminPromoterService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/admin/promoter")
@SecurityRequirement(name = "JWT_Admin")
@Tag(name = "Admin", description = "The Admin APIs")
public class AdminPromoterController {

	private final AdminPromoterService adminPromoterService;

	//add
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO create(@RequestBody RegistrationDTO registrationDTO) {
		return adminPromoterService.create(registrationDTO);
	}

	//update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO update(@RequestBody PromoterDTO promoterDTO) {
		return adminPromoterService.update(promoterDTO);
	}

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminPromoterService.delete(id);
	}
}
