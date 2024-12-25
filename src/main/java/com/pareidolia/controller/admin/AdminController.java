package com.pareidolia.controller.admin;


import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/admin/admin")
public class AdminController {

	private final AdminService adminService;

	//display miei dati
	@GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public AdminDTO getData() {
		return adminService.getData();
	}

	//altri
	@GetMapping(value = "/{idAdmin}/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public AdminDTO getData(@PathVariable("idAdmin") Long idAdmin) {
		return adminService.getData(idAdmin);
	}

	//update
	@PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AdminDTO update(@RequestBody AdminDTO adminDTO) {
		return adminService.update(adminDTO);
	}

	// add
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AdminDTO create(@RequestBody RegistrationDTO registrationDTO) {
		return adminService.create(registrationDTO);
	}

	// delete
	@DeleteMapping(value = "/{id}")
	public void delete(@PathVariable("id") Long id) {
		adminService.delete(id);
	}
}




