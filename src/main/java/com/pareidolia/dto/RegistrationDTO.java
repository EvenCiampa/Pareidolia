package com.pareidolia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDTO implements Serializable {

	private String email;
	private String password;
	private String name;
	private String surname;
	private String phone;

}
