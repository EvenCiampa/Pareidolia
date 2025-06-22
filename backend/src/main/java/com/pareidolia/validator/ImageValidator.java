package com.pareidolia.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class ImageValidator {

	@Value("${app.upload.account.max-size}")
	private long accountMaxSize;

	@Value("${app.upload.event.max-size}")
	private long eventMaxSize;

	@Value("${app.upload.account.allowed-extensions}")
	private String accountAllowedExtensions;

	@Value("${app.upload.event.allowed-extensions}")
	private String eventAllowedExtensions;

	/**
	 * Valida un file immagine destinato a essere utilizzato come immagine dell'account.
	 * @param file Il file immagine da validare.
	 */
	public void validateAccountImage(MultipartFile file) {
		validateImage(file, accountMaxSize, Arrays.asList(accountAllowedExtensions.split(",")));
	}

	/**
	 * Valida un file immagine destinato a essere utilizzato come immagine per un evento.
	 * @param file Il file immagine da validare.
	 */
	public void validateEventImage(MultipartFile file) {
		validateImage(file, eventMaxSize, Arrays.asList(eventAllowedExtensions.split(",")));
	}

	/**
	 * Effettua la validazione generale di un'immagine, controllando se:
	 * - Il file non è vuoto.
	 * - La dimensione del file non supera il limite massimo consentito.
	 * - L'estensione del file rientra tra quelle permesse.
	 * - Il contenuto del file corrisponde effettivamente a un formato immagine valido.
	 *
	 * @param file Il file immagine da validare.
	 * @param maxSize La dimensione massima consentita per il file.
	 * @param allowedExtensions Lista delle estensioni di file consentite.
	 * @throws IllegalArgumentException Se il file non rispetta uno dei criteri di validazione sopra elencati.
	 */
	private void validateImage(MultipartFile file, long maxSize, List<String> allowedExtensions) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("File is empty");
		}

		// Check file size
		if (file.getSize() > maxSize) {
			throw new IllegalArgumentException("File size exceeds maximum limit");
		}

		// Check file extension
		String originalFilename = file.getOriginalFilename();
		String extension = originalFilename != null ?
			originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";

		if (!allowedExtensions.contains(extension)) {
			throw new IllegalArgumentException("Invalid file extension. Allowed: " + String.join(", ", allowedExtensions));
		}

		// Validate that it's actually an image
		try {
			BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
			if (bufferedImage == null) {
				throw new IllegalArgumentException("Invalid image file");
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Error processing image file", e);
		}
	}
}