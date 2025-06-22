package com.pareidolia.service.promoter;

import com.pareidolia.configuration.security.jwt.JWTService;
import com.pareidolia.dto.AccountLoginDTO;
import com.pareidolia.dto.PasswordUpdateDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.service.ImageService;
import com.pareidolia.validator.AccountValidator;
import com.pareidolia.validator.ImageValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PromoterService {

	private final JWTService jwtService;
	private final ImageService imageService;
	private final ImageValidator imageValidator;
	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;
	private final PromoterInfoRepository promoterInfoRepository;

	private Account getAccountAndValidate() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			User user = ((User) authentication.getPrincipal());

			if (!user.getAuthorities().contains(new SimpleGrantedAuthority(Account.Type.PROMOTER.name()))) {
				throw new JWTService.TokenVerificationException();
			}
			return accountRepository.findByEmail(user.getUsername())
				.orElseThrow(JWTService.TokenVerificationException::new);
		}
		throw new JWTService.TokenVerificationException();
	}

	/**
	 * Ottiene i dati del promoter autenticato.
	 * @return PromoterDTO Il DTO del promoter contenente le informazioni del conto e del promoter.
	 * @throws JWTService.TokenVerificationException Se la verifica del token fallisce o l'account non è trovato.
	 * @throws IllegalArgumentException Se le informazioni del promoter non sono trovate.
	 */
	public PromoterDTO getData() {
		Account account = getAccountAndValidate();
		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(account.getId())
			.orElseThrow(() -> new IllegalArgumentException("Promoter information not found"));
		return AccountMapper.entityToPromoterDTO(account, promoterInfo);
	}

	/**
	 * Aggiorna i dati del promoter autenticato.
	 * @param promoterDTO DTO del promoter con i dati aggiornati.
	 * @return AccountLoginDTO DTO del conto aggiornato, includendo un nuovo token di autenticazione se l'email è stata modificata.
	 * @throws IllegalArgumentException Se l'ID nel DTO non corrisponde all'ID del promoter autenticato o se i dati non sono validi.
	 */
	public AccountLoginDTO update(PromoterDTO promoterDTO) {
		if (promoterDTO.getId() == null || !promoterDTO.getId().equals(getAccountAndValidate().getId())) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Pair<Account, PromoterInfo> accountWithInfo = accountValidator.getPromoterAndValidateUpdate(promoterDTO);
		Account account = accountWithInfo.getFirst();
		PromoterInfo promoterInfo = accountWithInfo.getSecond();
		boolean updateAuthToken = !Objects.equals(account.getEmail(), promoterDTO.getEmail());

		AccountMapper.updateEntitiesWithPromoterDTO(account, promoterInfo, promoterDTO);

		account = accountRepository.save(account);
		promoterInfoRepository.save(promoterInfo);

		String authToken = null;
		if (updateAuthToken) {
			authToken = jwtService.create(account.getReferenceType(), account.getEmail(), account.getPassword());
		}

		return AccountMapper.entityToAccountLoginDTO(account, authToken);
	}

	/**
	 * Aggiorna la password del conto del promoter autenticato.
	 * @param passwordUpdateDTO DTO contenente la password attuale e la nuova password.
	 * @return AccountLoginDTO DTO del conto con il nuovo token di autenticazione.
	 * @throws IllegalArgumentException Se la password attuale non è corretta o se la nuova password non è valida.
	 */
	public AccountLoginDTO updatePassword(PasswordUpdateDTO passwordUpdateDTO) {
		Account account = getAccountAndValidate();

		if (passwordUpdateDTO.getCurrentPassword() == null) {
			throw new IllegalArgumentException("Invalid Current Password");
		}

		String currentPassword = DigestUtils.sha3_256Hex(passwordUpdateDTO.getCurrentPassword());

		if (!Objects.equals(currentPassword, account.getPassword())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}

		if (passwordUpdateDTO.getNewPassword() == null) {
			throw new IllegalArgumentException("Invalid New Password");
		}

		accountValidator.passwordValidation(passwordUpdateDTO.getNewPassword());

		account.setPassword(DigestUtils.sha3_256Hex(passwordUpdateDTO.getNewPassword()));
		account = accountRepository.save(account);

		String authToken = jwtService.create(account.getReferenceType(), account.getEmail(), account.getPassword());

		return AccountMapper.entityToAccountLoginDTO(account, authToken);
	}

	/**
	 * Aggiorna l'immagine del profilo del promoter autenticato.
	 * @param file Il file dell'immagine da caricare.
	 * @return PromoterDTO DTO del promoter con l'immagine aggiornata.
	 * @throws IllegalArgumentException Se il salvataggio dell'immagine fallisce o se il file non è valido.
	 */
	public PromoterDTO updateImage(MultipartFile file) {
		imageValidator.validateAccountImage(file);

		Account account = getAccountAndValidate();
		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(account.getId())
			.orElseThrow(() -> new IllegalArgumentException("Promoter information not found"));

		try {
			String filename = imageService.saveImage(file);
			promoterInfo.setPhoto(filename);
			promoterInfo = promoterInfoRepository.save(promoterInfo);
			return AccountMapper.entityToPromoterDTO(account, promoterInfo);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to save image", e);
		}
	}

	/**
	 * Rimuove l'immagine del profilo del promoter autenticato.
	 * @return PromoterDTO DTO del promoter con l'immagine rimossa.
	 */
	public PromoterDTO deleteImage() {
		Account account = getAccountAndValidate();
		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(account.getId())
			.orElseThrow(() -> new IllegalArgumentException("Promoter information not found"));

		if (promoterInfo.getPhoto() != null) {
			promoterInfo.setPhoto(null);
			account = accountRepository.save(account);
		}

		return AccountMapper.entityToPromoterDTO(account, promoterInfo);
	}
}
