package com.pareidolia.service.admin;

import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.RegistrationDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminPromoterService {
	private final ImageService imageService;
	private final ImageValidator imageValidator;
	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;
	private final PromoterInfoRepository promoterInfoRepository;

	/**
	 * Crea un nuovo promotore nel sistema a partire dai dati di registrazione forniti.
	 * @param registrationDTO DTO di registrazione contenente le informazioni del promotore.
	 * @return PromoterDTO DTO del promotore appena creato con informazioni dell'account.
	 */
	public PromoterDTO create(RegistrationDTO registrationDTO) {
		accountValidator.createAccountValidator(registrationDTO);

		Account account = AccountMapper.registrationDTOToEntity(registrationDTO, Account.Type.PROMOTER);
		account = accountRepository.save(account);

		PromoterInfo promoterInfo = new PromoterInfo();
		promoterInfo.setIdPromoter(account.getId());
		promoterInfo = promoterInfoRepository.save(promoterInfo);

		return AccountMapper.entityToPromoterDTO(account, promoterInfo);
	}

	/**
	 * Aggiorna le informazioni di un promotore esistente nel sistema.
	 * @param promoterDTO DTO del promotore con le informazioni aggiornate.
	 * @return PromoterDTO DTO del promotore aggiornato.
	 */
	public PromoterDTO update(PromoterDTO promoterDTO) {
		if (promoterDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Pair<Account, PromoterInfo> accountWithInfo = accountValidator.getPromoterAndValidateUpdate(promoterDTO);
		Account account = accountWithInfo.getFirst();
		PromoterInfo promoterInfo = accountWithInfo.getSecond();

		AccountMapper.updateEntitiesWithPromoterDTO(account, promoterInfo, promoterDTO);

		account = accountRepository.save(account);
		promoterInfo = promoterInfoRepository.save(promoterInfo);

		return AccountMapper.entityToPromoterDTO(account, promoterInfo);
	}

	/**
	 * Aggiorna l'immagine di profilo di un promotore specificato dall'ID.
	 * @param id ID del promotore per cui aggiornare l'immagine.
	 * @param file File contenente la nuova immagine da caricare.
	 * @return PromoterDTO DTO del promotore con l'immagine aggiornata.
	 */
	public PromoterDTO updateImage(Long id, MultipartFile file) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		if (account.getReferenceType() != Account.Type.PROMOTER) {
			throw new IllegalArgumentException("Invalid Account Type");
		}

		imageValidator.validateAccountImage(file);

		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(account.getId())
			.orElseThrow(() -> new IllegalArgumentException("Promoter information not found"));

		try {
			String filename = imageService.saveImage(file);
			promoterInfo.setPhoto(filename);
			promoterInfo = promoterInfoRepository.save(promoterInfo);
			return AccountMapper.entityToPromoterDTO(account, promoterInfo);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to save image", e);
		}
	}

	/**
	 * Rimuove l'immagine di profilo di un promotore.
	 * @param id ID del promotore da cui rimuovere l'immagine.
	 * @return PromoterDTO DTO del promotore con l'immagine rimossa.
	 */
	public PromoterDTO deleteImage(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		if (account.getReferenceType() != Account.Type.PROMOTER) {
			throw new IllegalArgumentException("Invalid Account Type");
		}

		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(account.getId())
			.orElseThrow(() -> new IllegalArgumentException("Promoter information not found"));

		if (promoterInfo.getPhoto() != null) {
			promoterInfo.setPhoto(null);
			account = accountRepository.save(account);
		}

		return AccountMapper.entityToPromoterDTO(account, promoterInfo);
	}

	/**
	 * Elimina un promotore dal sistema insieme alle sue informazioni correlate.
	 * @param id ID del promotore da eliminare.
	 */
	public void delete(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Account account = accountRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));

		if (account.getReferenceType() != Account.Type.PROMOTER) {
			throw new IllegalArgumentException("Invalid Account Type");
		}

		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(id)
			.orElseThrow(() -> new IllegalArgumentException("Promoter info not found"));

		promoterInfoRepository.deleteById(promoterInfo.getId());
		accountRepository.deleteById(id);
	}
}
