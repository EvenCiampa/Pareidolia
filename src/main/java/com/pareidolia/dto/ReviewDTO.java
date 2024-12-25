package com.pareidolia.dto;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO implements Serializable {
    private Long id;
    private String title;
    private String description;
    private Long score;
    private Long idConsumer;
	private Long idEvent;
}
