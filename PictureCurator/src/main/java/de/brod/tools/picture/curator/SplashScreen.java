package de.brod.tools.picture.curator;

import javafx.fxml.FXML;

public class SplashScreen {

	@FXML
	public void openImportPage() {
		PictureCurator.openScene(PictureCurator.IMPORTER);
	}

	@FXML
	public void openOrganizePage() {
		PictureCurator.openScene(PictureCurator.ORGANIZER);
	}

}
