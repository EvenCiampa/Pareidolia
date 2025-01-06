package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {
	public Long id;
	public String message;
	public Long idAccount;
	public LocalDateTime creationTime;
}