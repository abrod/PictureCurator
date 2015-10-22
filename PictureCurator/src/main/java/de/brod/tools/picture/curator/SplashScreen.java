package de.brod.tools.picture.curator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class SplashScreen {

	@FXML
	public void openImportPage() {
		PictureCurator.openScene(PictureCurator.IMPORTER);
	}

	@FXML
	public void openOrganizePage() {
		PictureCurator.openScene(PictureCurator.ORGANIZER);
	}

	public static void showError(Exception ex) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(ex.toString());
		OutputStream out = new ByteArrayOutputStream();
		ex.printStackTrace(new PrintStream(out));
		BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
		String sLine = "";
		StringBuilder sOut = new StringBuilder();
		try {
			while ((sLine = reader.readLine()) != null) {
				if (sLine.replace("\t", " ").contains(" at "))
					if (!sLine.contains("de.brod.")) {
						// ignore
					} else {
						sOut.append(sLine).append("\n");
					}
			}
		} catch (IOException e) {
			// should not happen on stringreader
		}
		alert.setContentText(sOut.toString());
		alert.show();
	}
}
