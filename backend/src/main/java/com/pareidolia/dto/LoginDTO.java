package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO implements Serializable {
	public String email;
	public String password;
}
