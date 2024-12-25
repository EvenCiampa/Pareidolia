package com.pareidolia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO implements Serializable {
	private Long id;
    private String message;
    private Long idAccount;
	private LocalDateTime creationTime;
}