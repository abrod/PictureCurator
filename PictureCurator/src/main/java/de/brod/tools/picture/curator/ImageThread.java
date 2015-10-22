package de.brod.tools.picture.curator;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

public class ImageThread extends Thread {

	public static void openImage(FileHandler fileHandler, Pane imageBox, String fileName) {
		new ImageThread(fileHandler, imageBox, fileName).start();
	}

	private String sFileName;
	private FileHandler fileHandler;
	private CuratorImage bufferedImage;
	private String lastSize = "";
	private Pane imageBox;

	private ImageThread(FileHandler fileHandler, Pane imageBox, String sFileName) {
		this.sFileName = sFileName;
		this.fileHandler = fileHandler;
		this.imageBox = imageBox;
	}

	public void run() {
		openImportImage(sFileName);
	};

	private synchronized void openImportImage(String sFileName) {
		try {
			if (sFileName == null)
				return;
			int scaledWidth = (int) imageBox.getWidth();
			int scaledHeight = (int) imageBox.getHeight();
			String newSize = String.valueOf(scaledWidth + scaledHeight * 10000) + sFileName;
			if (newSize.equals(lastSize)) {
				return;
			}
			lastSize = newSize;
			File file = fileHandler.getImageFile(sFileName);
			if (file == null)
				return;
			bufferedImage = new CuratorImage(file);

			Image image = bufferedImage.resizeBufferedImage(scaledWidth, scaledHeight);
			BackgroundImage images = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
					BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
			Background background = new Background(images);
			imageBox.setBackground(background);

		} catch (Exception e) {
			// could not open image
			SplashScreen.showError(e);
		}
	}
}
