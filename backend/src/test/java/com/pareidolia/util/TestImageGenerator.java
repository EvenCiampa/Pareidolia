package com.pareidolia.util;

import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class TestImageGenerator {

	@SneakyThrows
	public static byte[] generateTestImage() {
		// Create a 100x100 test image
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();

		// Fill background with white
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, 100, 100);

		// Draw something to make it unique
		g2d.setColor(Color.BLUE);
		g2d.drawOval(25, 25, 50, 50);

		g2d.dispose();

		// Convert to byte array
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", outputStream);
		return outputStream.toByteArray();
	}
}