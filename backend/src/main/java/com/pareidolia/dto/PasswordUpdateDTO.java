package com.pareidolia.dto;

import lombok.Data;

@Data
public class PasswordUpdateDTO {
	private String currentPassword;
	private String newPassword;
}