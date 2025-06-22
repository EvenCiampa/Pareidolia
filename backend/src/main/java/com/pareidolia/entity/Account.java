package com.pareidolia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Account", uniqueConstraints = {
	@UniqueConstraint(name = "UK_account_email", columnNames = {"email"})
})
public class Account {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Basic
	@Column(name = "email", nullable = false)
	private String email;
	@Basic
	@Column(name = "password", nullable = false)
	private String password;
	@Basic
	@Column(name = "name", nullable = false)
	private String name;
	@Basic
	@Column(name = "surname", nullable = false)
	private String surname;
	@Basic
	@Column(name = "phone", nullable = false)
	private String phone;
	@Enumerated(EnumType.STRING)
	@Column(name = "reference_type", nullable = false)
	private Type referenceType;

	@ColumnDefault("CURRENT_TIMESTAMP(6)")
	@CreationTimestamp(source = SourceType.DB)
	@Column(name = "creation_time", nullable = false, updatable = false, length = 6)
	private LocalDateTime creationTime;
	@ColumnDefault("CURRENT_TIMESTAMP(6)")
	@UpdateTimestamp(source = SourceType.DB)
	@Column(name = "last_update", nullable = false, length = 6)
	private LocalDateTime lastUpdate;

	public Account(String name, String surname, String email, String password, String phone, Type referenceType) {
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
		this.phone = phone;
		this.referenceType = referenceType;
	}

	public enum Type {
		CONSUMER,
		PROMOTER,
		REVIEWER,
		ADMIN
	}
}
