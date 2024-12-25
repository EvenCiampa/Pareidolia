package com.pareidolia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "User")
public class Account {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Basic
	@Column(name = "email", nullable = false, unique = true)
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
	@CreationTimestamp
	@Column(name = "creation_time", nullable = false, updatable = false)
	private LocalDateTime creationTime;
	@UpdateTimestamp
	@Column(name = "last_update", nullable = false)
	private LocalDateTime lastUpdate;

	public Account(String name, String surname, String email, String password, String phone, Type referenceType) {
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
		this.phone = phone;
		this.referenceType = referenceType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Account account)) return false;
		return Objects.equals(id, account.id) && Objects.equals(email, account.email) && Objects.equals(password, account.password) && Objects.equals(name, account.name) && Objects.equals(surname, account.surname) && Objects.equals(phone, account.phone) && referenceType == account.referenceType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, email, password, name, surname, phone, referenceType);
	}

	public enum Type {
		CONSUMER,
		PROMOTER,
		ADMIN
	}
}
