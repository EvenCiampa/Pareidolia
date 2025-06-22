package com.pareidolia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

	@Value("${app.upload.dir}")
	private String uploadDir;

	@Value("${app.upload.url-prefix}")
	private String urlPrefix;

	@Value("${app.upload.thumbnail-prefix}")
	private String thumbnailPrefix;

	public String saveImage(MultipartFile file) throws IOException {
		String filename = generateUniqueFilename(file.getOriginalFilename());
		Path uploadPath = Paths.get(uploadDir);

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		Path filePath = uploadPath.resolve(filename);
		Files.copy(file.getInputStream(), filePath);

		if (thumbnailPrefix != null) {
			String thumbnailFileName = createThumbnail(filePath, thumbnailPrefix);
			return urlPrefix + "/" + thumbnailFileName;
		}

		return urlPrefix + "/" + filename;
	}

	private String createThumbnail(Path originalPath, String prefix) throws IOException {
		BufferedImage original = ImageIO.read(originalPath.toFile());

		// Calculate thumbnail dimensions (maintaining aspect ratio)
		int thumbWidth = 200;
		int thumbHeight = (int) ((double) original.getHeight() / original.getWidth() * thumbWidth);

		// Create thumbnail
		BufferedImage thumbnail = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = thumbnail.createGraphics();
		g2d.drawImage(original, 0, 0, thumbWidth, thumbHeight, null);
		g2d.dispose();

		// Save thumbnail
		String thumbnailName = prefix + originalPath.getFileName();
		Path thumbnailPath = originalPath.getParent().resolve(thumbnailName);
		ImageIO.write(thumbnail, getFileExtension(originalPath), thumbnailPath.toFile());

		return thumbnailName;
	}

	private String generateUniqueFilename(String originalFilename) {
		return UUID.randomUUID() + "." + getFileExtension(Paths.get(originalFilename));
	}

	private String getFileExtension(Path path) {
		String filename = path.getFileName().toString();
		return filename.substring(filename.lastIndexOf(".") + 1);
	}
}