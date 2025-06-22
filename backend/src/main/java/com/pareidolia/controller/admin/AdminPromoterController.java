package com.pareidolia.controller.admin;

import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.service.admin.AdminPromoterService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@SecurityRequirement(name = "JWT_Admin")
@PreAuthorize("hasAnyAuthority('ADMIN')")
@RequestMapping(path = "/admin/promoter")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
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

	@PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public PromoterDTO updateImage(@PathVariable Long id, @RequestParam("image") MultipartFile file) {
		return adminPromoterService.updateImage(id, file);
	}

	@DeleteMapping(value = "/{id}/image")
	public PromoterDTO deleteImage(@PathVariable Long id) {
		return adminPromoterService.deleteImage(id);
	}

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminPromoterService.delete(id);
	}
}
