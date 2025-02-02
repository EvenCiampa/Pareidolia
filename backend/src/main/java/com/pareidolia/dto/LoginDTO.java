package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class LoginDTO implements Serializable {
	public String email;
	@ToString.Exclude
	public String password;
}
