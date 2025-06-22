package com.pareidolia.controller.generic;

import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.LoginDTO;
import com.pareidolia.dto.RegistrationDTO;
import com.pareidolia.service.generic.AccessService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(path = "/generic/access")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Public", description = "The Public APIs")
public class AccessController {

	private final AccessService accessService;

	@PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountLoginDTO register(@RequestBody RegistrationDTO registrationDTO) {
		return accessService.register(registrationDTO);
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountLoginDTO login(@RequestBody LoginDTO loginDTO) {
		return accessService.login(loginDTO);
	}

	@PostMapping(value = "/forgotPassword")
	public void forgotPassword(@RequestParam(value = "email") String email) {
		accessService.forgotPassword(email);
	}

}
