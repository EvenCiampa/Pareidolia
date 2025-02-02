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

	public void validateAccountImage(MultipartFile file) {
		validateImage(file, accountMaxSize, Arrays.asList(accountAllowedExtensions.split(",")));
	}

	public void validateEventImage(MultipartFile file) {
		validateImage(file, eventMaxSize, Arrays.asList(eventAllowedExtensions.split(",")));
	}

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