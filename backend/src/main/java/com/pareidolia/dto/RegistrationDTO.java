package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class RegistrationDTO implements Serializable {
	public String email;
	@ToString.Exclude
	public String password;
	public String name;
	public String surname;
	public String phone;
}
