package com.pareidolia.controller.admin;


import com.pareidolia.dto.AccountDTO;
import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.service.admin.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/admin")
@SecurityRequirement(name = "JWT_Admin")
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

	// add
	@PostMapping(value = "/create/{referenceType}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountDTO create(@RequestBody RegistrationDTO registrationDTO, @PathVariable("referenceType") Account.Type referenceType) {
		return adminService.createAccount(registrationDTO, referenceType);
	}

	//update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountDTO update(@RequestBody AccountDTO accountDTO) {
		return adminService.updateAccount(accountDTO);
	}

	//update promoter
	@PostMapping(value = "/update/promoter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public PromoterDTO updatePromoter(@RequestBody PromoterDTO promoterDTO) {
		return adminService.updatePromoter(promoterDTO);
	}

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminService.delete(id);
	}
}




