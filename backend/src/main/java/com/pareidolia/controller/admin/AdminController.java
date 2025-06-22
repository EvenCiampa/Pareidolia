package com.pareidolia.controller.admin;

import com.pareidolia.dto.*;
import com.pareidolia.entity.Account;
import com.pareidolia.service.admin.AdminService;
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
@RequestMapping(path = "/admin")
@SecurityRequirement(name = "JWT_Admin")
@PreAuthorize("hasAnyAuthority('ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Admin", description = "The Admin APIs")
public class AdminController {
	private final AdminService adminService;

	//display miei dati
	@GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public AdminDTO getData() {
		return adminService.getData();
	}

	//altri
	@GetMapping(value = "/data/{idAccount}", produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountDTO getData(@PathVariable("idAccount") Long idAccount) {
		return adminService.getData(idAccount);
	}

	//list
	@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<AdminDTO> getAdmins(
		@RequestParam(value = "page", required = false) Integer page,
		@RequestParam(value = "size", required = false) Integer size) {
		return adminService.getAdmins(page, size);
	}

	// add
	@PostMapping(value = "/create/{referenceType}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountDTO create(@RequestBody RegistrationDTO registrationDTO, @PathVariable("referenceType") Account.Type referenceType) {
		return adminService.createAccount(registrationDTO, referenceType);
	}

	//update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountLoginDTO update(@RequestBody AccountDTO accountDTO) {
		return adminService.updateAccount(accountDTO);
	}

	//update promoter
	@PostMapping(value = "/update/promoter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO updatePromoter(@RequestBody PromoterDTO promoterDTO) {
		return adminService.updatePromoter(promoterDTO);
	}

	// update password
	@PostMapping(value = "/update/password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountLoginDTO updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
		return adminService.updatePassword(passwordUpdateDTO);
	}

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminService.delete(id);
	}
}



