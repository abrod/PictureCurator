package de.brod.tools.picture.curator;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PictureCurator extends Application {

	static Scene IMPORTER;
	static Scene ORGANIZER;

	static Stage stage;
	private double width = -1;
	private double height = -1;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		PictureCurator.stage = stage;
		stage.setTitle("Picture Curator");

		Scene splash = loadScene("SplashScreen");
		openScene(splash);

		width = splash.getWidth();
		height = splash.getHeight();

		IMPORTER = loadScene("PictureImporter");
		ORGANIZER = loadScene("PictureOrganizer");
		// openScene(IMPORTER);

	}

	static void openScene(Scene scene) {

		stage.setScene(scene);

		stage.setMaximized(true);

		stage.show();
	}

	private Scene loadScene(String fxmlFile) throws IOException {
		Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(fxmlFile + ".fxml"));
		Scene scene;
		if (width > 0 && height > 0) {
			scene = new Scene(root, width, height);
		} else {
			scene = new Scene(root);
		}

		scene.getStylesheets().add(getClass().getResource("PictureCurator.css").toString());
		return scene;
	}

}
